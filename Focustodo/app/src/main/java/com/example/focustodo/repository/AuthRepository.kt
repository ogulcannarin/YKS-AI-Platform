package com.example.focustodo.repository

import com.example.focustodo.data.remote.SupabaseClient
import com.example.focustodo.data.remote.SupabaseUser
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {
    
    suspend fun register(email: String, fullName: String, passwordHash: String, targetRank: Int?): Result<SupabaseUser> {
        return try {
            // 1. Create Supabase Auth User
            val userResponse = SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = passwordHash
            }
            
            val userId = userResponse?.id ?: throw Exception("Auth succeeded but ID was null")
            
            // 2. Insert into the public users table created in PostgreSQL
            val userRecord = SupabaseUser(
                id = userId,
                email = email,
                fullName = fullName,
                targetRank = targetRank
            )
            
            SupabaseClient.client.postgrest["users"].insert(userRecord)
            
            Result.success(userRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, passwordHash: String): Result<SupabaseUser> {
        return try {
            // 1. Sign in via Supabase Auth
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = passwordHash
            }
            
            val session = SupabaseClient.client.auth.currentSessionOrNull()
            
            val userId = session?.user?.id ?: throw Exception("Auth failed.")
            
            // 3. Find the profile data from public users table
            val userRecord = SupabaseClient.client.postgrest["users"]
                .select { filter { eq("id", userId) } }
                .decodeSingle<SupabaseUser>()
                
            Result.success(userRecord)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
