package com.example.data.database

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val customPacks: Flow<List<CustomPackEntity>> = gameDao.getAllCustomPacks()
    val fullHistory: Flow<List<GameHistoryEntity>> = gameDao.getFullGameHistory()

    suspend fun addCustomPack(pack: CustomPackEntity) {
        gameDao.insertCustomPack(pack)
    }

    suspend fun removeCustomPack(packId: Int) {
        gameDao.deleteCustomPack(packId)
    }

    suspend fun addGameRecord(record: GameHistoryEntity) {
        gameDao.insertGameRecord(record)
    }
}
