import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
from ai_danisman import ai_sohbeti_baslat

# --- EKSİK FONKSİYONLAR VE VERİLER EKLENDİ ---
yks_verileri = {
    'SAY': {550: 1, 500: 5000, 450: 30000, 400: 85000, 350: 180000, 300: 320000, 250: 550000, 200: 900000},
    'EA': {550: 1, 500: 1500, 450: 12000, 400: 55000, 350: 160000, 300: 400000, 250: 850000, 200: 1500000},
    'SOZ': {550: 1, 500: 500, 450: 5000, 400: 25000, 350: 90000, 300: 250000, 250: 600000, 200: 1200000},
    'TYT': {550: 1, 500: 10000, 450: 60000, 400: 180000, 350: 450000, 300: 950000, 250: 1800000, 200: 2800000}
}

def siralama_tahmin_et(puan, tur):
    if tur not in yks_verileri:
        return np.nan
    tablo = yks_verileri[tur]
    puanlar = sorted(list(tablo.keys()))
    siralamalar = [tablo[p] for p in puanlar]
    return int(np.interp(puan, puanlar, siralamalar))

tyt_yiginsal = {
    550: 1, 500: 10000, 450: 60000, 400: 180000,
    350: 450000, 300: 950000, 250: 1800000, 200: 2800000
}

def tyt_sira_bul(puan):
    puanlar = sorted(list(tyt_yiginsal.keys()))
    siralar = [tyt_yiginsal[p] for p in puanlar]
    return int(np.interp(puan, puanlar, siralar))
# ---------------------------------------------

# --- 1. AYT MODELİ EĞİTİMİ ---
df_ayt = pd.read_csv('lisans_temiz.csv')
df_ayt = df_ayt.rename(columns={'min_score': 'score', 'score_type': 'type'})
df_ayt = df_ayt.dropna(subset=['score'])

# Senin ayt.py'deki siralama_tahmin_et fonksiyonunu buraya uygula
df_ayt['est_ranking'] = df_ayt.apply(lambda row: siralama_tahmin_et(row['score'], row['type']), axis=1)
df_ayt = df_ayt.dropna(subset=['est_ranking'])

le = LabelEncoder()
df_ayt['type_encoded'] = le.fit_transform(df_ayt['type'])
model_ayt = RandomForestRegressor(n_estimators=100, random_state=42)
model_ayt.fit(df_ayt[['score', 'type_encoded']], df_ayt['est_ranking'])

# --- 2. TYT MODELİ EĞİTİMİ ---
df_tyt = pd.read_csv('on_lisans_temiz.csv')
df_tyt = df_tyt.rename(columns={'min_score': 'score', 'score_type': 'type'})
df_tyt = df_tyt.dropna(subset=['score'])

# Senin tyt.py'deki tyt_sira_bul fonksiyonunu buraya uygula
df_tyt['est_ranking'] = df_tyt['score'].apply(tyt_sira_bul)

model_tyt = RandomForestRegressor(n_estimators=100, random_state=42)
model_tyt.fit(df_tyt[['score']], df_tyt['est_ranking'])

print("Iki model de basariyla birlestirildi ve bellege yuklendi!")

def tek_pencere_tahmin(puan, tur):
    """
    tur: 'SAY', 'EA', 'SOZ' (AYT için) veya 'TYT'
    """
    if tur == 'TYT':
        # TYT modeline sadece puan gönderiyoruz
        sonuc = model_tyt.predict([[puan]])
        return int(sonuc[0])
    else:
        # 'SOZ' için veri setindeki gerçek isim olan 'SÖZ'ü kullanalım
        tur_model = 'SÖZ' if tur == 'SOZ' else tur
        
        try:
            # AYT modeline puan ve encode edilmiş tür gönderiyoruz
            tur_kod = le.transform([tur_model])[0]
            sonuc = model_ayt.predict([[puan, tur_kod]])
            return int(sonuc[0])
        except ValueError:
            return 0

def gercek_okullari_getir(puan, tur):
    """
    Veri setinden (df_ayt veya df_tyt) puanın yettiği en iyi okulları çeker.
    """
    try:
        if tur == 'TYT':
            uygunlar = df_tyt[(df_tyt['score'] <= puan)].sort_values(by='score', ascending=False).head(15)
        else:
            # Sözel için SÖZ dönüşümü
            tur_ara = 'SÖZ' if tur == 'SOZ' else tur
            uygunlar = df_ayt[(df_ayt['type'] == tur_ara) & (df_ayt['score'] <= puan)].sort_values(by='score', ascending=False).head(15)
            
        liste_str = ""
        for idx, row in uygunlar.iterrows():
            liste_str += f"- {row['university_name']} / {row['program_name']} (Taban Puan: {row['score']})\n"
        return liste_str if liste_str else "Uygun bolum bulunamadi."
    except Exception as e:
        return "Veri okuma hatasi."

def interaktif_test():
    print("\n" + "="*50)
    print("YKS NET HESAPLAMA VE SIRALAMA TAHMINI (TUM ALANLAR)")
    print("="*50)
    try:
        obp = float(input("OBP Puaninizi girin (Orn: 85): ") or 0)
        
        print("\n--- TYT NETLERI ---")
        tyt_tur = float(input("Turkce (40): ") or 0)
        tyt_mat = float(input("Matematik (40): ") or 0)
        tyt_sos = float(input("Sosyal (20): ") or 0)
        tyt_fen = float(input("Fen (20): ") or 0)
        
        print("\n--- AYT MATEMATIK & FEN (SAYISAL ICIN) ---")
        ayt_mat  = float(input("Matematik (40): ") or 0)
        ayt_fiz  = float(input("Fizik (14): ") or 0)
        ayt_kim  = float(input("Kimya (13): ") or 0)
        ayt_biyo = float(input("Biyoloji (13): ") or 0)
        
        print("\n--- AYT EDEBIYAT & SOSYAL-1 (ESIT AGIRLIK ICIN) ---")
        ayt_edb  = float(input("Edebiyat (24): ") or 0)
        ayt_tar1 = float(input("Tarih-1 (10): ") or 0)
        ayt_cog1 = float(input("Cografya-1 (6): ") or 0)
        
        print("\n--- AYT SOSYAL-2 (SOZEL ICIN) ---")
        ayt_tar2 = float(input("Tarih-2 (11): ") or 0)
        ayt_cog2 = float(input("Cografya-2 (11): ") or 0)
        ayt_fel  = float(input("Felsefe (12): ") or 0)
        ayt_din  = float(input("Din (6): ") or 0)
        
        # Puan Hesaplamaları
        tyt_ham = 100 + (tyt_tur*3.3 + tyt_mat*3.3 + tyt_sos*3.4 + tyt_fen*3.4)
        tyt_yerlestirme = tyt_ham + (obp * 0.6)
        
        # AYT Ham Puanlar (TYT %40 etkisiyle Base Puan)
        ayt_base = 100 + (tyt_ham * 0.4)
        
        say_ham = ayt_base + (ayt_mat*3.0 + ayt_fiz*2.85 + ayt_kim*3.07 + ayt_biyo*3.07)
        say_yerlestirme = say_ham + (obp * 0.6)
        
        ea_ham = ayt_base + (ayt_mat*3.0 + ayt_edb*3.0 + ayt_tar1*2.8 + ayt_cog1*3.33)
        ea_yerlestirme = ea_ham + (obp * 0.6)
        
        soz_ham = ayt_base + (ayt_edb*3.0 + ayt_tar1*2.8 + ayt_cog1*3.33 + ayt_tar2*2.91 + ayt_cog2*2.91 + ayt_fel*3.0 + ayt_din*3.33)
        soz_yerlestirme = soz_ham + (obp * 0.6)
        
        print("\n" + "="*50)
        print("HESAPLANAN YERLESTIRME PUANLARI VE SIRALAMALAR")
        print("-" * 50)
        
        # Tahminleri model üzerinden alıyoruz
        tyt_sira = tek_pencere_tahmin(tyt_yerlestirme, 'TYT')
        say_sira = tek_pencere_tahmin(say_yerlestirme, 'SAY')
        ea_sira  = tek_pencere_tahmin(ea_yerlestirme, 'EA')
        soz_sira = tek_pencere_tahmin(soz_yerlestirme, 'SOZ')
        
        print(f"TYT Puaniniz: {tyt_yerlestirme:.2f} | Tahmini Siralama: {tyt_sira:,}")
        print(f"SAY Puaniniz: {say_yerlestirme:.2f} | Tahmini Siralama: {say_sira:,}")
        print(f"EA  Puaniniz: {ea_yerlestirme:.2f}  | Tahmini Siralama: {ea_sira:,}")
        print(f"SOZ Puaniniz: {soz_yerlestirme:.2f}  | Tahmini Siralama: {soz_sira:,}")
        print("="*50 + "\n")
        
        # Kullanıcıya hangi alandan yapay zeka tavsiyesi istediğini soralım
        hedef = ""
        while hedef not in ['TYT', 'SAY', 'EA', 'SOZ']:
            hedef = input("Hangi alanda Yapay Zeka Tercih Danismanligi almak istiyorsun? (TYT/SAY/EA/SOZ): ").strip().upper()
            
        print("\nSonuclarin alindi! Gercek veriler yapay zekaya aktariliyor...\n")
        
        # Hedefe göre değişkenleri ayarla
        hedef_dict = {
            'TYT': (tyt_yerlestirme, tyt_sira),
            'SAY': (say_yerlestirme, say_sira),
            'EA':  (ea_yerlestirme, ea_sira),
            'SOZ': (soz_yerlestirme, soz_sira)
        }
        secilen_puan, secilen_sira = hedef_dict[hedef]
        
        # Öğrencinin puanına yeten okulları çek
        okullar = gercek_okullari_getir(secilen_puan, hedef)
        
        ai_sohbeti_baslat(
            puan=round(secilen_puan, 2), 
            siralama=secilen_sira, 
            puan_turu=hedef, 
            gercek_okullar=okullar
        )
        
    except ValueError:
        print("Hata: Lutfen sadece gecerli sayilar giriniz (Orn: 15.5 veya 20)!")

if __name__ == "__main__":
    interaktif_test()