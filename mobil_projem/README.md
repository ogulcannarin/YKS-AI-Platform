# 🎓 YKS Asistanım & AI Koç

**YKS Asistanım**, YKS (TYT & AYT) öğrencileri için özel olarak geliştirilmiş, puan hesaplama, sıralama tahmini yapma ve yapay zeka destekli eğitim danışmanlığı sunan kapsamlı bir mobil uygulamadır. Hem modern bir Android arayüzüne hem de güçlü bir Python/FastAPI arka planına sahiptir.

![YKS Asistanım](https://img.shields.io/badge/Status-Active-success) ![Platform](https://img.shields.io/badge/Platform-Android-green) ![Backend](https://img.shields.io/badge/Backend-FastAPI-blue)

---

## 🚀 Özellikler

*   **📊 Puan ve Sıralama Hesaplama:** TYT, Sayısal, Eşit Ağırlık ve Sözel alanlarında netlerinizi girerek güncel katsayılarla puanınızı hesaplar ve makine öğrenmesi modelleri ile tahmini sıralamanızı bulur.
*   **🤖 AI Koç (Yapay Zeka Danışmanı):** Aklınıza takılan soruları sorabileceğiniz, çalışma programı hazırlatabileceğiniz veya motivasyon alabileceğiniz kişisel yapay zeka asistanı.
*   **⏱️ Çalışma Zamanlayıcısı (Kronometre):** Hangi derse ne kadar çalıştığınızı takip edin ve sürelerinizi veritabanına kaydedin.
*   **📸 AI Soru Çözücü:** Çözemediğiniz soruların fotoğrafını çekin, yapay zeka sizin için anında çözsün.
*   **🏫 Üniversite/Bölüm Önerileri:** Tahmini sıralamanıza ve puanınıza göre size en uygun üniversite ve bölümleri listeler.

---

## 🛠️ Kullanılan Teknolojiler

### Mobil Uygulama (Frontend)
*   **Dil:** Kotlin
*   **UI Çerçevesi:** Jetpack Compose (Modern ve Reaktif Arayüz)
*   **Ağ İstekleri:** Retrofit2 & Gson
*   **Mimari:** Modern Android Geliştirme Standartları (Material Design 3)

### Sunucu (Backend)
*   **Dil:** Python
*   **Web Çerçevesi:** FastAPI
*   **Makine Öğrenmesi (Tahmin Motoru):** `scikit-learn`, `pandas` (Sıralama tahmin modelleri için)
*   **Yapay Zeka Entegrasyonu:** OpenAI API (AI Koç ve Soru Çözümü için)

---

## 📱 Ekran Görüntüleri ve Arayüz

Uygulama, **Koyu Tema (Dark Mode)** odaklı, neon renkler (Mor, Yeşil) ve modern gradyanlar kullanılarak "Gamer/Hacker" estetiği ile harmanlanmış şık bir tasarıma sahiptir. 

*   *Sekmeli Yapı:* TYT, SAY, EA, SOZ, ÇALIŞ, AI, SORU sekmeleri arası hızlı geçiş.
*   *Akıcı Animasyonlar:* Jetpack Compose ile pürüzsüz kullanıcı deneyimi.

---

## ⚙️ Kurulum ve Çalıştırma

Projeyi yerel ortamınızda çalıştırmak için aşağıdaki adımları izleyin:

### 1. Backend (FastAPI) Kurulumu

```bash
# Proje dizinine gidin
cd tahmin

# Gerekli kütüphaneleri yükleyin
pip install fastapi uvicorn pydantic pandas scikit-learn

# API sunucusunu başlatın
uvicorn api:app --reload --host 0.0.0.0 --port 8000
```
*Sunucu varsayılan olarak `http://localhost:8000` adresinde çalışacaktır.*

### 2. Mobil Uygulama (Android) Kurulumu

1.  `MyApplication2` klasörünü **Android Studio** ile açın.
2.  Gerekli Gradle bağımlılıklarının yüklenmesini bekleyin.
3.  Bir Android Emülatörü başlatın (API seviyesi 24+ önerilir) veya fiziksel cihazınızı bağlayın.
4.  Run (Çalıştır) tuşuna basarak uygulamayı derleyip başlatın.
*(Not: Emülatör kullanıyorsanız backend adresi otomatik olarak `http://10.0.2.2:8000/` üzerinden bağlanacaktır.)*

---

## 📡 API Uç Noktaları (Endpoints)

| Metot | Uç Nokta | Açıklama |
| :--- | :--- | :--- |
| `GET` | `/` | API'nin çalışıp çalışmadığını kontrol eder. |
| `POST`| `/hesapla` | OBP ve netleri alarak puan ve sıralama tahmini döndürür. |
| `GET` | `/okullar/{alan}/{puan}` | Belirtilen puan ve alana göre gerçek üniversite verilerini getirir. |

---

## 🤝 Katkıda Bulunma

Geliştirmelere her zaman açığız! Katkıda bulunmak isterseniz:
1. Bu depoyu forklayın.
2. Yeni bir özellik dalı oluşturun (`git checkout -b ozellik/YeniHarikaOzellik`).
3. Değişikliklerinizi commit edin (`git commit -m 'Harika bir özellik eklendi'`).
4. Dalınızı gönderin (`git push origin ozellik/YeniHarikaOzellik`).
5. Bir Pull Request oluşturun.

---

## 📜 Lisans

Bu proje MIT Lisansı ile lisanslanmıştır. Daha fazla bilgi için `LICENSE` dosyasına göz atabilirsiniz.

---
*Başarılar dileriz! YKS sürecinde en büyük yardımcınız olmak dileğiyle...* 🎓✨
