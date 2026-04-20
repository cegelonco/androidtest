package com.studenthousing.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY lastSyncedAt DESC")
    suspend fun getAll(): List<PropertyEntity>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PropertyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PropertyEntity>)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY lastSyncedAt DESC")
    suspend fun getAll(): List<BookingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BookingEntity>)
}
