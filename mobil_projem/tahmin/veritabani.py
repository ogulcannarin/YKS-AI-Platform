import os
from dotenv import load_dotenv
from supabase import create_client, Client

# .env dosyasındaki gizli değişkenleri yüklüyoruz
load_dotenv()

# URL ve Key bilgilerini ortam değişkenlerinden güvenli bir şekilde çekiyoruz
url: str = os.getenv("SUPABASE_URL")
key: str = os.getenv("SUPABASE_KEY")

# Eğer .env dosyasında eksik bilgi varsa programı uyarması için küçük bir kontrol
if not url or not key:
    raise ValueError("Supabase URL veya Key bulunamadı! Lütfen .env dosyanızı kontrol edin.")

# Supabase istemcisini (client) oluşturuyoruz
supabase: Client = create_client(url, key)

def veriyi_buluta_kaydet(user_id, tyt_puan, say_puan, ea_puan, soz_puan, siralama):
    data = {
        "user_id": user_id,
        "tyt_puan": int(tyt_puan), # Veritabanında bigint (tam sayı) olduğu için tam sayıya çevirdik
        "say_puan": int(say_puan), # Veritabanında bigint (tam sayı) olduğu için tam sayıya çevirdik
        "ea_puan": int(ea_puan),   # EA puanını da tam sayı olarak gönderiyoruz
        "soz_puan": int(soz_puan), # SÖZ puanını da tam sayı olarak gönderiyoruz
        "tahmini_sira": int(siralama)
    }
    
    try:
        # Supabase'deki 'scores' tablosuna ekle
        response = supabase.table("scores").insert(data).execute()
        
        # response.data objesi boş değilse (yani veri eklendiyse) başarılı sayıyoruz
        if response.data:
            print("🚀 Veriler başarıyla buluta yedeklendi!")
        else:
            print("❌ Kayıt sırasında bir hata oluştu.")
    except Exception as e:
        print(f"❌ Kayıt sırasında bir hata oluştu: {e}")

def en_iyileri_getir():
    try:
        # 'scores' tablosundan tahmini_sira'ya göre küçükten büyüğe ilk 10 kişiyi çek
        response = supabase.table("scores") \
            .select("user_id, tahmini_sira, say_puan") \
            .order("tahmini_sira", ascending=True) \
            .limit(10) \
            .execute()
        
        print("\n--- 🏆 GÜNÜN ŞAMPİYONLARI (TOP 10) ---")
        for i, user in enumerate(response.data, 1):
            print(f"{i}. Kullanıcı: {user['user_id']} | Sıralama: {user['tahmini_sira']} | Puan: {user['say_puan']}")
    except Exception as e:
        print(f"Liderlik tablosu alınamadı: {e}")

def ai_yorumu_kaydet(user_id, yorum):
    try:
        response = supabase.table("scores").update({"ai_yorum": yorum}).eq("user_id", user_id).execute()
        if response.data:
            print("💡 AI Tavsiyesi başarıyla veritabanına eklendi!")
    except Exception as e:
        print(f"⚠️ AI Yorumu kaydedilemedi (Supabase 'scores' tablosuna 'ai_yorum' adında bir 'text' sütunu eklemeyi unutma!): {e}")

def calisma_kaydet(user_id, ders_adi, calisma_dakikasi):
    data = {
        "user_id": user_id,
        "ders_adi": ders_adi,
        "duration_minutes": int(calisma_dakikasi)  # Veritabanında duration_minutes olarak eklendiği için güncellendi
    }
    try:
        response = supabase.table("study_logs").insert(data).execute()
        if response.data:
            print(f"⏱️ {ders_adi} dersi için {calisma_dakikasi} dakika çalışma süresi buluta kaydedildi!")
    except Exception as e:
        print(f"❌ Çalışma süresi kaydedilemedi (Supabase'de 'study_logs' tablosu oluşturduğundan emin ol): {e}")

def calisma_liderlik_tablosu():
    try:
        response = supabase.table("study_logs").select("user_id, duration_minutes").execute()
        
        if response.data:
            toplamlar = {}
            for row in response.data:
                uid = row["user_id"]
                # Eğer duration_minutes boş gelirse 0 say
                dk = row.get("duration_minutes") or 0
                toplamlar[uid] = toplamlar.get(uid, 0) + dk
            
            sirali = sorted(toplamlar.items(), key=lambda x: x[1], reverse=True)
            
            print("\n--- 📚 ÇALIŞMA LİDERLİK TABLOSU (TOP 10) ---")
            for i, (uid, dk) in enumerate(sirali[:10], 1):
                saat = dk // 60
                dakika = dk % 60
                print(f"{i}. Kullaıcı: {uid} | Toplam Çalışma: {saat} saat {dakika} dakika")
    except Exception as e:
        print(f"Çalışma liderlik tablosu alınamadı: {e}")

if __name__ == "__main__":
    # Bu dosya doğrudan çalıştırıldığında test amaçlı bu mesajı verir
    print("[BASARILI] Supabase baglantisi basariyla kuruldu!")
    
    # Test amaçlı örnek kullanım:
    # veriyi_buluta_kaydet("user_123", 424.50, 501.16, 4894)
