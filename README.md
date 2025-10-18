# 🎯 KariyerinOlsun - AI Destekli Mülakat Hazırlık Sistemi

> **Yapay zeka destekli mülakat hazırlık platformu ile kariyerinizi bir üst seviyeye taşıyın!**

## 📋 Proje Hakkında

KariyerinOlsun, öğrencilerin ve profesyonellerin mülakatlara daha iyi hazırlanmasını sağlayan kapsamlı bir yapay zeka destekli platformdur. Sistem, kullanıcıların CV bilgilerini analiz ederek eksik beceri ve bilgileri tespit eder, kişiselleştirilmiş mülakat deneyimi sunar ve detaylı performans analizi sağlar.

## ✨ Özellikler

### 🔍 **CV Analiz Sistemi**
- CV yükleme ve otomatik analiz
- İlgili sektördeki eksik bilgi ve becerilerin tespiti
- Kişiselleştirilmiş gelişim önerileri
- Sektörel bilgi boşluklarının belirlenmesi

### 🎤 **AI Destekli Mülakat Sistemi**
- Gerçek zamanlı mülakat simülasyonu
- Sektörel ve pozisyon bazlı soru havuzu
- Doğal dil işleme ile akıllı soru üretimi
- Çok dilli destek

### 📊 **Çok Boyutlu Analiz**
- **Metin Analizi**: Cevap kalitesi ve içerik değerlendirmesi
- **Ses Analizi**: Konuşma hızı, tonlama ve telaffuz analizi
- **Görüntü Analizi**: Mimik, beden dili ve görsel sunum değerlendirmesi
- **Duygu Analizi**: Stres seviyesi, güven ve motivasyon ölçümü

### 📈 **Detaylı Raporlama**
- Kapsamlı performans raporu
- Güçlü yönler ve gelişim alanları
- Kişiselleştirilmiş öneriler
- İlerleme takibi ve geçmiş analizler

## 🛠️ Teknoloji Stack

### Frontend
- **React.js** - Modern kullanıcı arayüzü
- **TypeScript** - Tip güvenliği
- **Tailwind CSS** - Responsive tasarım
- **Framer Motion** - Animasyonlar
- **Chart.js** - Veri görselleştirme

### Backend
- **Node.js** - Sunucu tarafı
- **Express.js** - Web framework
- **MongoDB** - Veritabanı
- **JWT** - Kimlik doğrulama
- **Multer** - Dosya yükleme

### AI/ML Servisleri
- **OpenAI GPT** - Doğal dil işleme
- **Google Cloud Vision** - Görüntü analizi
- **Azure Cognitive Services** - Ses analizi
- **TensorFlow** - Makine öğrenmesi modelleri

### DevOps & Deployment
- **Docker** - Containerization
- **AWS/GCP** - Cloud deployment
- **GitHub Actions** - CI/CD
- **Nginx** - Reverse proxy

## 🚀 Kurulum

### Gereksinimler
- Node.js (v18+)
- MongoDB (v6+)
- Python (v3.9+)
- Git

### Adım Adım Kurulum

1. **Repository'yi klonlayın**
```bash
git clone https://github.com/kullaniciadi/kariyerin-olsun.git
cd kariyerin-olsun
```

2. **Bağımlılıkları yükleyin**
```bash
# Frontend bağımlılıkları
cd frontend
npm install

# Backend bağımlılıkları
cd ../backend
npm install

# Python ML bağımlılıkları
cd ../ml-services
pip install -r requirements.txt
```

3. **Çevre değişkenlerini ayarlayın**
```bash
cp .env.example .env
# .env dosyasını düzenleyin
```

4. **Veritabanını başlatın**
```bash
mongod
```

5. **Uygulamayı çalıştırın**
```bash
# Backend
cd backend
npm run dev

# Frontend (yeni terminal)
cd frontend
npm start

# ML Services (yeni terminal)
cd ml-services
python app.py
```

## 📁 Proje Yapısı

```
kariyerin-olsun/
├── 📁 frontend/                 # React.js frontend
│   ├── 📁 src/
│   │   ├── 📁 components/      # UI bileşenleri
│   │   ├── 📁 pages/          # Sayfa bileşenleri
│   │   ├── 📁 services/       # API servisleri
│   │   └── 📁 utils/          # Yardımcı fonksiyonlar
│   └── package.json
├── 📁 backend/                 # Node.js backend
│   ├── 📁 src/
│   │   ├── 📁 controllers/    # API kontrolcüleri
│   │   ├── 📁 models/         # Veritabanı modelleri
│   │   ├── 📁 routes/         # API rotaları
│   │   └── 📁 middleware/     # Ara yazılımlar
│   └── package.json
├── 📁 ml-services/             # Python ML servisleri
│   ├── 📁 models/             # ML modelleri
│   ├── 📁 services/           # Analiz servisleri
│   └── requirements.txt
├── 📁 docs/                   # Dokümantasyon
├── 📁 tests/                  # Test dosyaları
├── docker-compose.yml         # Docker konfigürasyonu
└── README.md
```

## 🎯 Kullanım Senaryoları

### 1. CV Analizi
- Kullanıcı CV'sini yükler
- Sistem CV'yi analiz eder
- Eksik beceri ve bilgiler tespit edilir
- Kişiselleştirilmiş gelişim planı sunulur

### 2. Mülakat Simülasyonu
- Kullanıcı hedef pozisyonu seçer
- AI destekli sorular üretilir
- Gerçek zamanlı mülakat gerçekleştirilir
- Çok boyutlu analiz yapılır

### 3. Performans Analizi
- Detaylı rapor oluşturulur
- Güçlü yönler ve gelişim alanları belirlenir
- Öneriler sunulur
- İlerleme takibi yapılır

## 📊 API Dokümantasyonu

### Kimlik Doğrulama
```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
```

### CV Analizi
```http
POST /api/cv/upload
GET /api/cv/analysis/:id
PUT /api/cv/update/:id
```

### Mülakat Sistemi
```http
POST /api/interview/start
GET /api/interview/questions/:sessionId
POST /api/interview/answer
GET /api/interview/analysis/:sessionId
```

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/AmazingFeature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some AmazingFeature'`)
4. Branch'inizi push edin (`git push origin feature/AmazingFeature`)
5. Pull Request oluşturun

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

## 👥 Takım

- **Proje Lideri**: [İsim](https://github.com/kullaniciadi)
- **Frontend Developer**: [İsim](https://github.com/kullaniciadi)
- **Backend Developer**: [İsim](https://github.com/kullaniciadi)
- **ML Engineer**: [İsim](https://github.com/kullaniciadi)

## 📞 İletişim

- **Email**: info@kariyerinolsun.com
- **Website**: [www.kariyerinolsun.com](https://www.kariyerinolsun.com)
- **LinkedIn**: [KariyerinOlsun](https://linkedin.com/company/kariyerinolsun)

## 🙏 Teşekkürler

Bu projeyi mümkün kılan tüm açık kaynak kütüphanelere ve topluluklara teşekkürler!

---

<div align="center">
  <p>⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!</p>
  <p>🚀 Kariyerinizi bir üst seviyeye taşıyın!</p>
</div>