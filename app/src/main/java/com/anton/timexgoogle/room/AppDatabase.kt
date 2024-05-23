package com.anton.timexgoogle.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [JornadaEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jornadaDao(): JornadaDao
}
