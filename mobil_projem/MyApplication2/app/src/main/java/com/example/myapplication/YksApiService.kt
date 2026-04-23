package com.example.myapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface YksApiService {
    @POST("/hesapla")
    fun puanHesapla(@Body request: HesaplaRequest): Call<HesaplaResponse>
    @POST("/calisma-kaydet")
    fun calismaKaydet(@Body request: StudyLogRequest): Call<SimpleResponse>
    @POST("/ai-danis")
    fun yksAiDanis(@Body request: AiDanismanRequest): Call<AiResponse>
}