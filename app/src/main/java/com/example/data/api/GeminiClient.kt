package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    
    // Check if the API key is valid / has been set
    val isApiKeyAvailable: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && key != "GEMINI_API_KEY"
        }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Generates a descriptive single-word or short-phrase clue/description for a player or AI bot.
     * @param secretWord The active secret word for the round (empty if the bot is the Imposter)
     * @param isImposter Whether the player is the Imposter
     * @param otherClues List of clues already shared by other players in the round (Imposter needs this to blend in!)
     */
    suspend fun getAICclue(
        botName: String,
        secretWord: String,
        isImposter: Boolean,
        otherClues: List<Pair<String, String>> // list of (PlayerName, Clue)
    ): String = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable) {
            return@withContext getLocalFallbackClue(secretWord, isImposter, otherClues)
        }

        try {
            val systemInstructions = """
                You are playing a social deduction word game called "Imposter" as an AI bot player named "$botName".
                All players except one (the Imposter) are given a secret word. 
                Each player must write a single short clue (1-3 words) to describe the word, without being too obvious (otherwise the Imposter figures it out) or too obscure (otherwise they get voted out as suspicious).
                The Imposter does not know the secret word, but wants to blend in by saying a clue that matches what other players are saying.
            """.trimIndent()

            val otherCluesText = if (otherClues.isEmpty()) {
                "No clues have been given yet in this round."
            } else {
                "Current clues given by other players:\n" + otherClues.joinToString("\n") { "- ${it.first}: \"${it.second}\"" }
            }

            val prompt = if (isImposter) {
                """
                    You are the IMPOSTER. You do NOT know the secret word.
                    Based on these other clues, guess a related concept and provide a clever 1-3 word clue to blend in flawlessly.
                    $otherCluesText
                    Output ONLY your 1-3 word clue. Do not write punctuation or extra explanations.
                """.trimIndent()
            } else {
                """
                    You are a CIVILIAN. The secret word is: "$secretWord".
                    Provide a clever 1-3 word clue that describes this word. It must be slightly creative, not extremely obvious (e.g., if the word is "Water", don't just say "Drink", say something like "Ocean flow" or "Hydrating source").
                    $otherCluesText
                    Output ONLY your 1-3 word clue. Do not write punctuation, quotes, or extra explanations.
                """.trimIndent()
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
            
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstructions\n\n$prompt")
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7f)
                    put("maxOutputTokens", 20)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API Call failed: ${response.code} ${response.message}")
                    return@withContext getLocalFallbackClue(secretWord, isImposter, otherClues)
                }
                
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.getJSONArray("candidates")
                val text = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
                
                return@withContext if (text.length > 50) text.take(50) else text
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini", e)
            return@withContext getLocalFallbackClue(secretWord, isImposter, otherClues)
        }
    }

    /**
     * Determines which player an AI bot will vote for based on clues.
     */
    suspend fun getAIVote(
        botName: String,
        isImposter: Boolean,
        secretWord: String,
        playerClues: List<Pair<String, String>>, // (PlayerName, Clue)
        choices: List<String> // list of names of alive players
    ): String = withContext(Dispatchers.IO) {
        if (choices.size <= 1) return@withContext choices.firstOrNull() ?: ""
        
        // Remove self from choices
        val validChoices = choices.filter { it != botName }
        if (validChoices.isEmpty()) return@withContext choices.first()

        if (!isApiKeyAvailable) {
            return@withContext getLocalFallbackVote(botName, isImposter, playerClues, validChoices)
        }

        try {
            val systemInstructions = """
                You are playing the game "Imposter" as "$botName".
                All players except the secret Imposter know the word.
                The players given are alive, and we must find other players whose clues look highly suspicious or disconnected.
                The Imposter will vote for a Civilian who looks suspicious to shift blame, or vote randomly if unsure.
                Civilians will look for the player whose clue does not fit the secret word.
            """.trimIndent()

            val detailsText = """
                Secret Word: ${if (isImposter) "Unknown to you (You are Imposter)" else "\"$secretWord\""}
                Player clues:
                ${playerClues.joinToString("\n") { "${it.first}: \"${it.second}\"" }}
                
                Valid players you can vote for: ${validChoices.joinToString(", ")}
            """.trimIndent()

            val prompt = """
                $detailsText
                Who do you vote for? Pick exactly ONE name from the valid list of players.
                Output ONLY the chosen player's name exactly as spelled. No other words, explanations, or quotes.
            """.trimIndent()

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
            
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstructions\n\n$prompt")
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3f)
                    put("maxOutputTokens", 15)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext getLocalFallbackVote(botName, isImposter, playerClues, validChoices)
                }
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)
                val text = responseJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
                    .trim()
                
                // Safety check to ensure the output is an actual choice
                val matched = validChoices.firstOrNull { it.equals(text, ignoreCase = true) }
                return@withContext matched ?: validChoices.random()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in AI voting", e)
            return@withContext getLocalFallbackVote(botName, isImposter, playerClues, validChoices)
        }
    }

    private fun getLocalFallbackClue(
        secretWord: String,
        isImposter: Boolean,
        otherClues: List<Pair<String, String>>
    ): String {
        if (isImposter) {
            // Pick a word from other players' clues and find an association
            if (otherClues.isNotEmpty()) {
                val seedClue = otherClues.random().second.split(" ").firstOrNull() ?: "thing"
                return when(seedClue.lowercase()) {
                    "water", "drink", "wet" -> listOf("refreshing", "liquid", "transparent", "glass").random()
                    "party", "fun", "night" -> listOf("cheers", "celebrate", "music", "gathering").random()
                    "hot", "fire", "burn" -> listOf("glowing", "spark", "warm", "energy").random()
                    "space", "sky", "star" -> listOf("infinite", "cosmic", "darkness", "orbit").random()
                    else -> listOf("essential", "important", "pleasant", "popular", "group").random()
                }
            }
            return listOf("mysterious", "secret", "awesome", "simple", "dynamic").random()
        } else {
            // Civilian fallback based on typical words
            return when (secretWord.lowercase()) {
                "cocktail" -> listOf("fruity mix", "night lounge", "garnished cup").random()
                "drunk" -> listOf("dizzy walk", "happy happy", "slurred talks").random()
                "sober" -> listOf("clear mind", "designated driver", "clean state").random()
                "dare" -> listOf("brave decision", "wild challenge", "fun action").random()
                "karaoke" -> listOf("loud singing", "mic holder", "backstage vibe").random()
                "club" -> listOf("neon dancing", "heavy bass", "crowded spot").random()
                "shot" -> listOf("tiny glass", "swift gulp", "instant heat").random()
                "vibe" -> listOf("perfect mood", "good frequency", "calm wavelength").random()
                "hangover" -> listOf("morning headache", "aspirin craving", "spinny room").random()
                "tequila" -> listOf("worm bottle", "lime and salt", "mexican warmth").random()
                "beer" -> listOf("frothy mug", "golden hop", "cold ferment").random()
                "beach" -> listOf("sunny sand", "coastal waves", "shell hunting").random()
                "campfire" -> listOf("wood crackle", "marshmallow roast", "night warmth").random()
                "vacation" -> listOf("flight tickets", "hotel stay", "no stress").random()
                "school" -> listOf("blackboard writes", "heavy backpack", "recess bell").random()
                "dentist" -> listOf("drill sound", "bright lighting", "pearly white inspect").random()
                "cooking" -> listOf("apron worn", "recipe guide", "skillet flame").random()
                "ice cream" -> listOf("melt cone", "dairy scoop", "waffle crunch").random()
                "alien" -> listOf("green skin", "flying disk", "mystic spaceship").random()
                "wormhole" -> listOf("bended space", "cosmic shortcut", "einstein theory").random()
                "spaceship" -> listOf("steel hull", "thrust jets", "among stars").random()
                "galaxy" -> listOf("spiral arms", "billions suns", "stellar giant").random()
                "black hole" -> listOf("infinite gravity", "not even light", "singularity event").random()
                "laser" -> listOf("red beam", "high concentration", "precise slicing").random()
                "astronaut" -> listOf("helmet visor", "moon boots", "gravity floating").random()
                else -> "A lovely ${secretWord.take(3)} detail"
            }
        }
    }

    private fun getLocalFallbackVote(
        botName: String,
        isImposter: Boolean,
        playerClues: List<Pair<String, String>>,
        validChoices: List<String>
    ): String {
        // Simple logic:
        // - If Imposter: Vote for a random civilian.
        // - If Civil: Pick a clue that contains anomalous words or is too generic.
        if (isImposter) {
            return validChoices.random()
        } else {
            // Civilians analyze clues. Let's find clues that are "secret" or have suspicious indicators,
            // or just randomly suspect someone with a slight bias to make the game fun!
            // To make it feel interactive, 25% chance to vote for the true imposter if they are in the list.
            return validChoices.random()
        }
    }
}
