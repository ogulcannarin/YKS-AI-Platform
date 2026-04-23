import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder
import warnings

# Gereksiz uyarıları gizleyelim
warnings.filterwarnings('ignore')

# --- SABIT VERILER ---
YKS_VERILERI = {
    'SAY': {550: 1, 500: 5000, 450: 30000, 400: 85000, 350: 180000, 300: 320000, 250: 550000, 200: 900000},
    'EA': {550: 1, 500: 1500, 450: 12000, 400: 55000, 350: 160000, 300: 400000, 250: 850000, 200: 1500000},
    'SOZ': {550: 1, 500: 500, 450: 5000, 400: 25000, 350: 90000, 300: 250000, 250: 600000, 200: 1200000},
    'TYT': {550: 1, 500: 10000, 450: 60000, 400: 180000, 350: 450000, 300: 950000, 250: 1800000, 200: 2800000}
}

TYT_YIGINSAL = {
    550: 1, 500: 10000, 450: 60000, 400: 180000,
    350: 450000, 300: 950000, 250: 1800000, 200: 2800000
}

def siralama_tahmin_et(puan, tur):
    if tur not in YKS_VERILERI:
        return np.nan
    tablo = YKS_VERILERI[tur]
    puanlar = sorted(list(tablo.keys()))
    siralamalar = [tablo[p] for p in puanlar]
    return int(np.interp(puan, puanlar, siralamalar))

def tyt_sira_bul(puan):
    puanlar = sorted(list(TYT_YIGINSAL.keys()))
    siralar = [TYT_YIGINSAL[p] for p in puanlar]
    return int(np.interp(puan, puanlar, siralar))


class ModelYoneticisi:
    """Veri okuma, makine öğrenmesi modellerini eğitme ve tahmin yapma sınıfı."""
    def __init__(self):
        self.model_ayt = None
        self.model_tyt = None
        self.le = LabelEncoder()
        self.df_ayt = None
        self.df_tyt = None

    def modelleri_egit(self, ayt_csv='lisans_temiz.csv', tyt_csv='on_lisans_temiz.csv'):
        # --- AYT MODELİ ---
        self.df_ayt = pd.read_csv(ayt_csv)
        self.df_ayt = self.df_ayt.rename(columns={'min_score': 'score', 'score_type': 'type'})
        self.df_ayt = self.df_ayt.dropna(subset=['score'])
        self.df_ayt['est_ranking'] = self.df_ayt.apply(lambda row: siralama_tahmin_et(row['score'], row['type']), axis=1)
        self.df_ayt = self.df_ayt.dropna(subset=['est_ranking'])
        
        self.df_ayt['type_encoded'] = self.le.fit_transform(self.df_ayt['type'])
        self.model_ayt = RandomForestRegressor(n_estimators=100, random_state=42)
        self.model_ayt.fit(self.df_ayt[['score', 'type_encoded']], self.df_ayt['est_ranking'])

        # --- TYT MODELİ ---
        self.df_tyt = pd.read_csv(tyt_csv)
        self.df_tyt = self.df_tyt.rename(columns={'min_score': 'score', 'score_type': 'type'})
        self.df_tyt = self.df_tyt.dropna(subset=['score'])
        self.df_tyt['est_ranking'] = self.df_tyt['score'].apply(tyt_sira_bul)

        self.model_tyt = RandomForestRegressor(n_estimators=100, random_state=42)
        self.model_tyt.fit(self.df_tyt[['score']], self.df_tyt['est_ranking'])

        print("✅ Makine Öğrenmesi Modelleri başarıyla eğitildi ve belleğe yüklendi!")

    def tahmin_et(self, puan, tur):
        """Puan ve tür bilgisine göre sıralama tahmini yapar."""
        if tur == 'TYT':
            sonuc = self.model_tyt.predict([[puan]])
            return int(sonuc[0])
        else:
            tur_model = 'SÖZ' if tur == 'SOZ' else tur
            try:
                tur_kod = self.le.transform([tur_model])[0]
                sonuc = self.model_ayt.predict([[puan, tur_kod]])
                return int(sonuc[0])
            except ValueError:
                return 0

    def gercek_okullari_getir(self, puan, tur):
        """Veri setinden puana uygun en iyi 15 okulu getirir."""
        try:
            if tur == 'TYT':
                uygunlar = self.df_tyt[(self.df_tyt['score'] <= puan)].sort_values(by='score', ascending=False).head(15)
            else:
                tur_ara = 'SÖZ' if tur == 'SOZ' else tur
                uygunlar = self.df_ayt[(self.df_ayt['type'] == tur_ara) & (self.df_ayt['score'] <= puan)].sort_values(by='score', ascending=False).head(15)
                
            liste_str = ""
            for idx, row in uygunlar.iterrows():
                liste_str += f"- {row['university_name']} / {row['program_name']} (Taban Puan: {row['score']})\n"
            return liste_str if liste_str else "Bu puana uygun bölüm bulunamadı."
        except Exception as e:
            return "Veri okuma hatası."
