package com.example.focustodo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val fullName: String,
    val targetRank: Int?,
    val passwordHash: String // Basic storage for demonstration of MVP auth local
)
