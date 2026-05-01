package com.example.myapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class AuthRequest(val email: String, val password: String)
data class AuthResponse(val access_token: String?, val error_description: String?, val msg: String?)
data class ResetPasswordRequest(val email: String)

interface SupabaseApiService {
    @POST("auth/v1/signup")
    fun signup(
        @Header("apikey") apiKey: String,
        @Body request: AuthRequest
    ): Call<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    fun login(
        @Header("apikey") apiKey: String,
        @Body request: AuthRequest
    ): Call<AuthResponse>

    // Kendi oluşturduğun "kullanicilar" tablosuna kayıt eklemek için
    @POST("rest/v1/kullanicilar")
    fun kayitEkle(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body request: KullaniciKayitRequest
    ): Call<Void>

    // Google Sign-In için
    @POST("auth/v1/token?grant_type=id_token")
    fun loginWithIdToken(
        @Header("apikey") apiKey: String,
        @Body request: IdTokenRequest
    ): Call<AuthResponse>

    @retrofit2.http.GET("rest/v1/kullanicilar")
    fun getKullanici(
        @Header("apikey") apiKey: String,
        @retrofit2.http.Query("email") emailEq: String
    ): Call<List<KullaniciKayitRequest>>

    // Şifremi Unuttum için
    @POST("auth/v1/recover")
    fun resetPassword(
        @Header("apikey") apiKey: String,
        @Body request: ResetPasswordRequest
    ): Call<Void>
}

data class IdTokenRequest(
    val id_token: String,
    val provider: String = "google"
)

// Yeni tabloya gönderilecek veri modeli (Şifre yok!)
data class KullaniciKayitRequest(
    val email: String,
    val kullanici_adi: String
)
