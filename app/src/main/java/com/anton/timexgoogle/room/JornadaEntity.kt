package com.anton.timexgoogle.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jornada_table")
data class JornadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long?,
    val restTimeInSeconds: Long,
    val syncStatus: Int // 0: no sincronizado, 1: sincronizado
)