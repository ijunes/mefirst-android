package com.ijunes.today.data

import com.ijunes.mefirst.database.entity.WorkTodayEntity
import kotlinx.coroutines.flow.Flow

interface WorkTodayRepository {

    suspend fun getAllNotes(): Flow<List<WorkTodayEntity>>

    suspend fun insertNote(note: WorkTodayEntity)

    suspend fun flushTodayEntries()
}