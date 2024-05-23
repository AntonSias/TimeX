package com.anton.timexgoogle.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface JornadaDao {

    @Insert
    suspend fun insertJornada(jornada: JornadaEntity)

    @Query("SELECT * FROM jornada_table WHERE endTime IS NULL")
    suspend fun getCurrentJornada(): JornadaEntity?

    @Query("UPDATE jornada_table SET endTime = :endTime, restTimeInSeconds = :restTime WHERE id = :jornadaId")
    suspend fun updateJornadaEndTime(jornadaId: Int, endTime: Long, restTime: Long)

    @Query("DELETE FROM jornada_table WHERE id = :jornadaId")
    suspend fun deleteJornada(jornadaId: Int)

    @Query("UPDATE jornada_table SET syncStatus = :status WHERE id = :jornadaId")
    suspend fun updateJornadaSyncStatus(jornadaId: Int, status: Int)

    @Query("SELECT * FROM jornada_table WHERE syncStatus = 0")
    suspend fun getJornadasNoSincronizadas(): List<JornadaEntity>

    // Add this method to get all jornadas
    @Query("SELECT * FROM jornada_table")
    suspend fun getAllJornadas(): List<JornadaEntity>
}