from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict
import uuid

# Projenizdeki mevcut modülleri içe aktarıyoruz
from puan_hesaplama import tyt_puan_hesapla, ayt_say_puan_hesapla, ayt_ea_puan_hesapla, ayt_soz_puan_hesapla
from siralama_motoru import ModelYoneticisi
from soru_cozucu import soruyu_analiz_et
from veritabani import veriyi_buluta_kaydet, calisma_kaydet, ai_yorumu_kaydet, mesaj_kaydet, sohbet_gecmisi_getir, sohbet_oturumlarini_getir
from ai_danisman import client

# Uygulama ve modelleri başlatıyoruz
app = FastAPI(title="YKS Tahmin API", description="YKS Puan ve Sıralama Tahmin API'si", version="1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

print("Modeller yükleniyor...")
motor = ModelYoneticisi()
motor.modelleri_egit()
print("Modeller hazır!")

# İstek (Request) gövdeleri için veri modelleri
class TytNetleri(BaseModel):
    turkce: float = 0
    matematik: float = 0
    sosyal: float = 0
    fen: float = 0

class AytSayisalNetleri(BaseModel):
    matematik: float = 0
    fizik: float = 0
    kimya: float = 0
    biyoloji: float = 0

class AytEsitAgirlikNetleri(BaseModel):
    matematik: float = 0
    edebiyat: float = 0
    tarih1: float = 0
    cografya1: float = 0

class AytSozelNetleri(BaseModel):
    edebiyat: float = 0
    tarih1: float = 0
    cografya1: float = 0
    tarih2: float = 0
    cografya2: float = 0
    felsefe: float = 0
    din: float = 0

class HesaplaRequest(BaseModel):
    obp: float
    tyt: TytNetleri
    ayt_say: Optional[AytSayisalNetleri] = None
    ayt_ea: Optional[AytEsitAgirlikNetleri] = None
    ayt_soz: Optional[AytSozelNetleri] = None

class StudyLogRequest(BaseModel):
    ders_adi: str
    sure: int

class AiDanismanRequest(BaseModel):
    soru: str
    puan: Optional[float] = 400.0
    siralama: Optional[int] = 50000
    puan_turu: Optional[str] = "SAY"
    user_id: str = "123"
    session_id: Optional[str] = None  # None ise yeni oturum başlatılır

class SoruCozRequest(BaseModel):
    image_base64: str
    soru_metni: Optional[str] = "Bu soruyu adım adım açıklar mısın?"
    user_id: str = "123"

# API Bitiş Noktaları (Endpoints)
@app.get("/")
def ana_sayfa():
    return {"mesaj": "YKS Tahmin API'sine Hoş Geldiniz!"}

@app.post("/hesapla")
def puan_ve_siralama_hesapla(veri: HesaplaRequest):
    try:
        sonuclar = {}
        
        # TYT Hesaplama
        tyt_ham, tyt_yerlestirme = tyt_puan_hesapla(
            veri.tyt.turkce, veri.tyt.matematik, veri.tyt.sosyal, veri.tyt.fen, veri.obp
        )
        tyt_sira = motor.tahmin_et(tyt_yerlestirme, 'TYT')
        
        sonuclar['TYT'] = {
            "puan": round(tyt_yerlestirme, 2),
            "siralama": tyt_sira
        }

        # Sayısal Hesaplama
        if veri.ayt_say:
            say_ham, say_yerlestirme = ayt_say_puan_hesapla(
                tyt_ham, veri.ayt_say.matematik, veri.ayt_say.fizik, veri.ayt_say.kimya, veri.ayt_say.biyoloji, veri.obp
            )
            say_sira = motor.tahmin_et(say_yerlestirme, 'SAY')
            sonuclar['SAY'] = {
                "puan": round(say_yerlestirme, 2),
                "siralama": say_sira
            }

        # Eşit Ağırlık Hesaplama
        if veri.ayt_ea:
            ea_ham, ea_yerlestirme = ayt_ea_puan_hesapla(
                tyt_ham, veri.ayt_ea.matematik, veri.ayt_ea.edebiyat, veri.ayt_ea.tarih1, veri.ayt_ea.cografya1, veri.obp
            )
            ea_sira = motor.tahmin_et(ea_yerlestirme, 'EA')
            sonuclar['EA'] = {
                "puan": round(ea_yerlestirme, 2),
                "siralama": ea_sira
            }

        # Sözel Hesaplama
        if veri.ayt_soz:
            soz_ham, soz_yerlestirme = ayt_soz_puan_hesapla(
                tyt_ham, veri.ayt_soz.edebiyat, veri.ayt_soz.tarih1, veri.ayt_soz.cografya1, veri.ayt_soz.tarih2, veri.ayt_soz.cografya2, veri.ayt_soz.felsefe, veri.ayt_soz.din, veri.obp
            )
            soz_sira = motor.tahmin_et(soz_yerlestirme, 'SOZ')
            sonuclar['SOZ'] = {
                "puan": round(soz_yerlestirme, 2),
                "siralama": soz_sira
            }

        return {"basarili": True, "sonuclar": sonuclar}
    
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/okullar/{alan}/{puan}")
def okullari_getir(alan: str, puan: float):
    if alan.upper() not in ['TYT', 'SAY', 'EA', 'SOZ']:
        raise HTTPException(status_code=400, detail="Geçersiz alan tipi. TYT, SAY, EA veya SOZ olmalıdır.")
    
    okullar = motor.gercek_okullari_getir(puan, alan.upper())
    return {"basarili": True, "alan": alan.upper(), "okullar": okullar}

@app.post("/calisma-kaydet")
async def calisma_ekle(veri: StudyLogRequest):
    calisma_kaydet(veri.user_id, veri.ders_adi, veri.sure)
    return {"basarili": True}

@app.post("/ai-danis")
async def ai_danisman_cevapla(veri: AiDanismanRequest):
    try:
        # Oturum yönetimi: session_id yoksa yeni oturum başlat
        session_id = veri.session_id or str(uuid.uuid4())

        # Kullanıcının bu oturumdaki geçmiş mesajlarını çek (bağlam için)
        gecmis = sohbet_gecmisi_getir(veri.user_id, session_id)

        # Mesaj geçmişini OpenAI formatına çevir
        mesaj_listesi = [
            {"role": "system", "content": "Sen 'Oracle' adında gelişmiş, cyberpunk temalı bir YKS Yapay Zeka Koçusun. Bir hacker/sistem yöneticisi dili kullanarak (örneğin: 'Sistem analizi tamamlandı', 'Açıklarını hackle', 'Veri ağlarına bağlanıldı') ama aynı zamanda öğrenciyi kesinlikle motive ederek ve YKS tercih/çalışma sürecinde gerçekçi verilerle yardımcı olan elit bir asistansın. Sana öğrencinin puanı, sıralaması ve gidebileceği okullar verilecek. Yanıtlarında Markdown kullan ve havalı, fütüristik bir üslup takın."}
        ]

        # Geçmiş mesajları sisteme ekle (bağlam sürekliliği)
        for m in gecmis:
            mesaj_listesi.append({
                "role": "user" if m["role"] == "user" else "assistant",
                "content": m["content"]
            })

        # Yeni soruyu ekle (okul bilgisiyle zenginleştirilmiş)
        gercek_okullar = motor.gercek_okullari_getir(veri.puan, veri.puan_turu)
        mesaj_listesi.append({
            "role": "user",
            "content": f"Sorum: {veri.soru}\nPuan Türüm: {veri.puan_turu}\nPuanım: {veri.puan}\nTahmini Sıralamam: {veri.siralama}\nKazanabileceğim Bazı Okullar: {gercek_okullar}"
        })

        # OpenAI'dan cevap al
        response = client.chat.completions.create(model="gpt-4o", messages=mesaj_listesi)
        ai_cevap = response.choices[0].message.content

        # Her iki mesajı da Supabase'e kaydet
        user_ok = mesaj_kaydet(veri.user_id, session_id, "user", veri.soru)
        ai_ok   = mesaj_kaydet(veri.user_id, session_id, "ai", ai_cevap)
        ai_yorumu_kaydet(veri.user_id, ai_cevap)

        if not user_ok or not ai_ok:
            print("[UYARI] Mesajlar Supabase'e kaydedilemedi! RLS politikasını kontrol edin.")

        return {
            "basarili": True,
            "cevap": ai_cevap,
            "session_id": session_id,
            "kaydedildi": user_ok and ai_ok  # Frontend'e bilgi ver
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/sohbet-gecmisi/{user_id}")
async def sohbet_gecmisi_endpoint(user_id: str, session_id: Optional[str] = None):
    """Kullanıcının sohbet geçmişini getirir."""
    mesajlar = sohbet_gecmisi_getir(user_id, session_id)
    return {"basarili": True, "mesajlar": mesajlar}

@app.get("/sohbet-oturumlari/{user_id}")
async def sohbet_oturumlari_endpoint(user_id: str):
    """Kullanıcının tüm geçmiş sohbet oturumlarını listeler."""
    oturumlar = sohbet_oturumlarini_getir(user_id)
    return {"basarili": True, "oturumlar": oturumlar}

@app.post("/soru-coz")
async def soru_coz_endpoint(veri: SoruCozRequest):
    try:
        cozum = soruyu_analiz_et(veri.image_base64, veri.soru_metni)
        if cozum:
            ai_yorumu_kaydet(veri.user_id, "Görsel Soru Çözüldü")
            return {"basarili": True, "cozum": cozum}
        else:
            raise HTTPException(status_code=500, detail="AI analiz başarısız.")
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
