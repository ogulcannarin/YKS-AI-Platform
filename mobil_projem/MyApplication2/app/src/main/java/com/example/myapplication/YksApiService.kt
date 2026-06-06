package com.example.myapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface YksApiService {
    @POST("/hesapla")
    fun puanHesapla(@Body request: HesaplaRequest): Call<HesaplaResponse>

    @POST("/calisma-kaydet")
    fun calismaKaydet(@Body request: StudyLogRequest): Call<SimpleResponse>

    @POST("/ai-danis")
    fun yksAiDanis(@Body request: AiDanismanRequest): Call<AiResponse>

    @POST("/soru-coz")
    fun yksSoruCoz(@Body request: SoruCozRequest): Call<SoruCozResponse>

    @GET("/sohbet-oturumlari/{user_id}")
    fun sohbetOturumlariniGetir(@Path("user_id") userId: String): Call<SohbetOturumlariResponse>

    @GET("/sohbet-gecmisi/{user_id}")
    fun sohbetGecmisiniGetir(
        @Path("user_id") userId: String,
        @Query("session_id") sessionId: String? = null
    ): Call<SohbetGecmisiResponse>
}