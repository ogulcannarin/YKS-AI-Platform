package com.example.focustodo.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUser(
    val id: String, // UUID returned from Auth
    val email: String,
    @SerialName("full_name") val fullName: String?,
    @SerialName("target_rank") val targetRank: Int?,
    @SerialName("created_at") val createdAt: String? = null
)
