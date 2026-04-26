package com.example.myapplication.network

import com.google.gson.annotations.SerializedName

// --- PUAN MODELLERİ ---
data class TytNetleri(
    @SerializedName("turkce") val turkce: Double = 0.0,
    @SerializedName("matematik") val matematik: Double = 0.0,
    @SerializedName("sosyal") val sosyal: Double = 0.0,
    @SerializedName("fen") val fen: Double = 0.0
)

data class AytSayisalNetleri(
    @SerializedName("matematik") val matematik: Double = 0.0,
    @SerializedName("fizik") val fizik: Double = 0.0,
    @SerializedName("kimya") val kimya: Double = 0.0,
    @SerializedName("biyoloji") val biyoloji: Double = 0.0
)

data class AytEaNetleri(
    @SerializedName("matematik") val matematik: Double = 0.0,
    @SerializedName("edebiyat") val edebiyat: Double = 0.0,
    @SerializedName("tarih1") val tarih1: Double = 0.0,
    @SerializedName("cografya1") val cografya1: Double = 0.0
)

data class AytSozelNetleri(
    @SerializedName("edebiyat") val edebiyat: Double = 0.0,
    @SerializedName("tarih1") val tarih1: Double = 0.0,
    @SerializedName("cografya1") val cografya1: Double = 0.0,
    @SerializedName("tarih2") val tarih2: Double = 0.0,
    @SerializedName("cografya2") val cografya2: Double = 0.0,
    @SerializedName("felsefe") val felsefe: Double = 0.0,
    @SerializedName("din") val din: Double = 0.0
)

// --- REQUEST VE RESPONSE MODELLERİ ---
data class HesaplaRequest(
    @SerializedName("obp") val obp: Double,
    @SerializedName("tyt") val tyt: TytNetleri,
    @SerializedName("ayt_say") val ayt_say: AytSayisalNetleri? = null,
    @SerializedName("ayt_ea") val ayt_ea: AytEaNetleri? = null,
    @SerializedName("ayt_soz") val ayt_soz: AytSozelNetleri? = null
)

data class HesaplaResponse(
    @SerializedName("basarili") val basarili: Boolean,
    @SerializedName("sonuclar") val sonuclar: Map<String, SonucDetay>?
)

data class SonucDetay(
    @SerializedName("puan") val puan: Double,
    @SerializedName("siralama") val siralama: Int
)

// --- ÇALIŞMA TAKİBİ MODELLERİ (TEK SEFER TANIMLANDI) ---
data class StudyLogRequest(
    @SerializedName("ders_adi") val ders_adi: String,
    @SerializedName("sure") val sure: Int,
    @SerializedName("user_id") val user_id: Int = 123
)

data class SimpleResponse(
    @SerializedName("basarili") val basarili: Boolean,
    @SerializedName("mesaj") val mesaj: String
)

// --- AI DANIŞMAN MODELLERİ ---
data class AiDanismanRequest(
    @SerializedName("user_id") val user_id: Int = 123,
    @SerializedName("soru") val soru: String,
    @SerializedName("puan") val puan: Double? = 400.0,
    @SerializedName("siralama") val siralama: Int? = 50000,
    @SerializedName("puan_turu") val puan_turu: String? = "SAY"
)
data class SoruCozRequest(
    @SerializedName("user_id") val user_id: Int = 123,
    @SerializedName("image_base64") val image_base64: String,
    @SerializedName("soru_metni") val soru_metni: String = "Bu soruyu adım adım açıklar mısın?"
)

data class SoruCozResponse(
    @SerializedName("basarili") val basarili: Boolean,
    @SerializedName("cozum") val cozum: String
)

data class AiResponse(
    @SerializedName("basarili") val basarili: Boolean,
    @SerializedName("cevap") val cevap: String
)