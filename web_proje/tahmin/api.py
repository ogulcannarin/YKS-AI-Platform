from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional, List, Dict

# Projenizdeki mevcut modülleri içe aktarıyoruz
from puan_hesaplama import tyt_puan_hesapla, ayt_say_puan_hesapla, ayt_ea_puan_hesapla, ayt_soz_puan_hesapla
from siralama_motoru import ModelYoneticisi

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
    return {"basarili": True, "alan": alan.upper(), "okullar": okullar.to_dict('records') if not okullar.empty else []}
