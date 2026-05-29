package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM custom_packs")
    fun getAllCustomPacks(): Flow<List<CustomPackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPack(pack: CustomPackEntity)

    @Query("DELETE FROM custom_packs WHERE id = :packId")
    suspend fun deleteCustomPack(packId: Int)

    @Query("SELECT * FROM game_history ORDER BY dateTimestamp DESC")
    fun getFullGameHistory(): Flow<List<GameHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameRecord(record: GameHistoryEntity)
}
