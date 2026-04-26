from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

# Kendi modüllerin
from soru_cozucu import soruyu_analiz_et
from puan_hesaplama import tyt_puan_hesapla, ayt_say_puan_hesapla, ayt_ea_puan_hesapla, ayt_soz_puan_hesapla
from siralama_motoru import ModelYoneticisi
from veritabani import veriyi_buluta_kaydet, calisma_kaydet, ai_yorumu_kaydet
from ai_danisman import client 

app = FastAPI(title="YKS Master API - Vision Edition")

# Android emülatör ve dış erişim için CORS ayarı
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- VERİ MODELLERİ ---
class TytNetleri(BaseModel):
    turkce: float = 0.0
    matematik: float = 0.0
    sosyal: float = 0.0
    fen: float = 0.0

class AytSayisalNetleri(BaseModel):
    matematik: float = 0.0
    fizik: float = 0.0
    kimya: float = 0.0
    biyoloji: float = 0.0

class AytEaNetleri(BaseModel):
    matematik: float = 0.0
    edebiyat: float = 0.0
    tarih1: float = 0.0
    cografya1: float = 0.0

class AytSozelNetleri(BaseModel):
    edebiyat: float = 0.0
    tarih1: float = 0.0
    cografya1: float = 0.0
    tarih2: float = 0.0
    cografya2: float = 0.0
    felsefe: float = 0.0
    din: float = 0.0

class HesaplaRequest(BaseModel):
    obp: float = 0.0
    tyt: TytNetleri
    ayt_say: Optional[AytSayisalNetleri] = None
    ayt_ea: Optional[AytEaNetleri] = None
    ayt_soz: Optional[AytSozelNetleri] = None

class StudyLogRequest(BaseModel):
    user_id: int = 123
    ders_adi: str
    sure: int

class AiDanismanRequest(BaseModel):
    user_id: int = 123
    soru: str
    puan: Optional[float] = 400.0
    siralama: Optional[int] = 50000
    puan_turu: Optional[str] = "SAY"

# YENİ: Soru Çözme Modeli
class SoruCozRequest(BaseModel):
    user_id: int = 123
    image_base64: str
    soru_metni: Optional[str] = "Bu soruyu adım adım açıklar mısın?"

# --- SİSTEMİ BAŞLAT ---
motor = ModelYoneticisi()
motor.modelleri_egit()

# --- ENDPOINTLER ---

@app.post("/hesapla")
async def puan_ve_siralama_hesapla(veri: HesaplaRequest):
    try:
        sonuclar = {}
        tyt_h, tyt_y = tyt_puan_hesapla(veri.tyt.turkce, veri.tyt.matematik, veri.tyt.sosyal, veri.tyt.fen, veri.obp)
        tyt_s = int(motor.tahmin_et(tyt_y, 'TYT'))
        sonuclar['TYT'] = {"puan": round(tyt_y, 2), "siralama": tyt_s}

        say_p, ea_p, soz_p, ana_sira = 0.0, 0.0, 0.0, tyt_s

        if veri.ayt_say:
            _, say_y = ayt_say_puan_hesapla(tyt_h, veri.ayt_say.matematik, veri.ayt_say.fizik, veri.ayt_say.kimya, veri.ayt_say.biyoloji, veri.obp)
            say_s = int(motor.tahmin_et(say_y, 'SAY'))
            say_p, ana_sira = round(say_y, 2), say_s
            sonuclar['SAY'] = {"puan": say_p, "siralama": say_s}

        if veri.ayt_ea:
            _, ea_y = ayt_ea_puan_hesapla(tyt_h, veri.ayt_ea.matematik, veri.ayt_ea.edebiyat, veri.ayt_ea.tarih1, veri.ayt_ea.cografya1, veri.obp)
            ea_p = round(ea_y, 2)
            sonuclar['EA'] = {"puan": ea_p, "siralama": int(motor.tahmin_et(ea_y, 'EA'))}
            if not veri.ayt_say: ana_sira = sonuclar['EA']['siralama']

        if veri.ayt_soz:
            _, soz_y = ayt_soz_puan_hesapla(tyt_h, veri.ayt_soz.edebiyat, veri.ayt_soz.tarih1, veri.ayt_soz.cografya1, veri.ayt_soz.tarih2, veri.ayt_soz.cografya2, veri.ayt_soz.felsefe, veri.ayt_soz.din, veri.obp)
            soz_p = round(soz_y, 2)
            sonuclar['SOZ'] = {"puan": soz_p, "siralama": int(motor.tahmin_et(soz_y, 'SOZ'))}
            if not veri.ayt_say and not veri.ayt_ea: ana_sira = sonuclar['SOZ']['siralama']

        veriyi_buluta_kaydet(123, round(tyt_y, 2), say_p, ea_p, soz_p, ana_sira)
        return {"basarili": True, "sonuclar": sonuclar}
    except Exception as e:
        print(f"Hata: {e}")
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/calisma-kaydet")
async def calisma_ekle(veri: StudyLogRequest):
    calisma_kaydet(veri.user_id, veri.ders_adi, veri.sure)
    return {"basarili": True}

@app.post("/ai-danis")
async def ai_danisman_cevapla(veri: AiDanismanRequest):
    try:
        gercek_okullar = motor.gercek_okullari_getir(veri.puan, veri.puan_turu)
        mesajlar = [
            {"role": "system", "content": "Sen uzman bir YKS koçusun."},
            {"role": "user", "content": f"Sorum: {veri.soru}\nPuanım: {veri.puan}\nOkullar: {gercek_okullar}"}
        ]
        response = client.chat.completions.create(model="gpt-4o", messages=mesajlar)
        ai_cevap = response.choices[0].message.content
        ai_yorumu_kaydet(veri.user_id, ai_cevap)
        return {"basarili": True, "cevap": ai_cevap}
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

# YENİ: Soru Çözme Endpoint'i
@app.post("/soru-coz")
async def soru_coz_endpoint(veri: SoruCozRequest):
    try:
        cozum = soruyu_analiz_et(veri.image_base64, veri.soru_metni)
        if cozum:
            ai_yorumu_kaydet(veri.user_id, f"Görsel Soru Çözüldü")
            return {"basarili": True, "cozum": cozum}
        else:
            raise HTTPException(status_code=500, detail="AI analiz başarısız.")
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)