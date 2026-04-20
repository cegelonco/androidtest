package com.studenthousing.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val address: String,
    val price: Double,
    val description: String?,
    val type: String?,
    val city: String?,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?,
    val lastSyncedAt: Long
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val status: String,
    val finalPrice: Double?,
    val propertyTitle: String?,
    val lastSyncedAt: Long
)
