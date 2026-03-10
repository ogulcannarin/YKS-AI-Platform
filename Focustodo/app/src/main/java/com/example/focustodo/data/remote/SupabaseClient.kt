package com.example.focustodo.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    // BURAYA KENDİ URL'NİZİ YAZACAKSINIZ
    // Örnek format: "https://<rastgele-harfler>.supabase.co"
    const val SUPABASE_URL = "https://tylpqcjspowyvcjnjqdz.supabase.co"
    
    // BURAYI, SUPABASE PANELİNDEKİ "anon public" YAZAN KEY'İNİZ İLE DEĞİŞTİRİN
    const val SUPABASE_KEY = "BURAYA_ANON_PUBLIC_KEYINIZI_YAZIN"
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth) {
            flowType = FlowType.PKCE
            scheme = "app"
            host = "supabase.com"
        }
    }
}
