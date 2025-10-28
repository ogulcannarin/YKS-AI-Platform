# 🤖 Canlı AI Avatar Projesi

Bu proje, D-ID platformu aracılığıyla gerçek zamanlı olarak etkileşim kurabileceğiniz, OpenAI tarafından desteklenen bir yapay zeka avatarı oluşturmayı amaçlamaktadır. Kullanıcılar avatar ile sesli veya yazılı olarak iletişim kurabilir ve kendi kamera görüntülerini de ekranda görebilirler.

## ✨ Özellikler

* **Gerçek Zamanlı Avatar Akışı:** D-ID API'si kullanılarak canlı video akışı.
* **Yapay Zeka Sohbeti:** Kullanıcı girdilerine OpenAI (GPT-3.5 Turbo varsayılan olarak) kullanarak anlamlı cevaplar üretme.
* **Sesli Komut:** Tarayıcının Web Speech API'si ve `react-speech-recognition` kütüphanesi ile kullanıcının sesini metne çevirme.
* **Görüntülü Görüşme:** Kullanıcının kamerasını (`getUserMedia` API'si ile) açıp ekranda gösterme.
* **Ayrık Backend ve Frontend:** Node.js/Express backend ve React/Vite frontend mimarisi.

## 🛠️ Kullanılan Teknolojiler

### Backend (`AvatarProjem` klasörü)

* **Runtime:** Node.js
* **Framework:** Express.js (API sunucusu için)
* **API/SDK İstemcileri:**
    * `@d-id/node-sdk`: D-ID stream oluşturma ve yönetme işlemleri için.
    * `axios`: D-ID `/talks/streams` endpoint'ine doğrudan REST API çağrıları yapmak için (konuşma komutları).
    * `openai`: OpenAI API'sine (GPT modelleri) bağlanmak için.
* **Yardımcı Kütüphaneler:**
    * `dotenv`: Ortam değişkenlerini (`.env` dosyası) yönetmek için.
    * `cors`: Farklı portlarda çalışan frontend ve backend arasındaki iletişime izin vermek için.

### Frontend (`AvatarProjem/frontend` klasörü)

* **Framework/Kütüphane:** React
* **Build Aracı:** Vite
* **Önemli Kütüphaneler:**
    * `react-speech-recognition`: Sesten metne dönüştürme (Web Speech API kullanır).
    * `lucide-react`: İkonlar için.
* **Tarayıcı API'ları:**
    * WebRTC (`RTCPeerConnection`): D-ID'den gelen canlı video akışını almak için.
    * `navigator.mediaDevices.getUserMedia`: Kullanıcı kamerasına erişmek için.

## 🚀 Kurulum ve Çalıştırma

### Gereksinimler

* Node.js (LTS sürümü önerilir)
* npm (Node.js ile birlikte gelir)
* D-ID API Anahtarı
* OpenAI API Anahtarı

### Kurulum Adımları

1.  **Ana Proje Klasörü Kurulumu:**
    * Terminali `AvatarProjem` klasöründe açın.
    * Gerekli backend kütüphanelerini yükleyin:
        ```bash
        npm install
        ```
    * Ana klasörde (`AvatarProjem`) `.env` adında bir dosya oluşturun ve içine API anahtarlarınızı aşağıdaki formatta ekleyin:
        ```env
        D_ID_API_KEY=SIZIN_D_ID_EMAILINIZIN_BASE64_HALI:SIZIN_D_ID_SIFRENIZ
        OPENAI_API_KEY=sk-PROJ-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        ```
        *Not: `D_ID_API_KEY` formatı önemlidir, iki nokta üst üste ile ayrılmış olmalıdır.*

2.  **Frontend Projesi Kurulumu:**
    * Terminalde `AvatarProjem` klasöründeyken `frontend` klasörüne girin:
        ```bash
        cd frontend
        ```
    * Gerekli frontend kütüphanelerini yükleyin:
        ```bash
        npm install
        ```
    * (Eğer `lucide-react` veya `react-speech-recognition` eksikse `npm install lucide-react react-speech-recognition regenerator-runtime` komutunu çalıştırın).

### Projeyi Çalıştırma

Projeyi çalıştırmak için **iki ayrı terminale** ihtiyacınız vardır:

1.  **Terminal 1 (Backend Sunucusu):**
    * `AvatarProjem` klasöründe olun.
    * Sunucuyu başlatın:
        ```bash
        node server.js
        ```
    * `🚀 Avatar Backend sunucusu çalışıyor...` mesajını görmelisiniz. Bu terminali açık bırakın.

2.  **Terminal 2 (Frontend Sunucusu):**
    * `AvatarProjem/frontend` klasöründe olun.
    * Frontend geliştirme sunucusunu başlatın:
        ```bash
        npm run dev
        ```
    * Size `http://localhost:XXXX` gibi bir adres verilecektir (genellikle 5173). Bu terminali de açık bırakın.

3.  **Tarayıcı:**
    * Web tarayıcınızı açın ve Terminal 2'nin verdiği adrese (örn: `http://localhost:5173/`) gidin.
    * "Bağlan" butonuna basın ve mikrofon/kamera izinlerini verin.

## 📝 Yapılanlar (Özet)

1.  Node.js ve Express ile bir backend API sunucusu (`server.js`) oluşturuldu.
2.  `.env` dosyasından D-ID ve OpenAI API anahtarları okundu.
3.  Backend'de D-ID stream oluşturma (`/create`), WebRTC bağlantısı kurma (`/start`, `/ice`) ve stream sonlandırma (`/delete`) endpoint'leri yazıldı.
4.  `/talk` endpoint'i, kullanıcı girdisini alıp OpenAI'ye göndererek cevap alacak ve bu cevabı D-ID'ye konuşma komutu olarak iletecek şekilde güncellendi.
5.  React ve Vite kullanılarak bir frontend arayüzü (`App.jsx`) oluşturuldu.
6.  Frontend'de WebRTC bağlantı mantığı kurularak D-ID'den gelen video akışı `<video>` elementine bağlandı.
7.  `react-speech-recognition` kullanılarak sesli komut özelliği eklendi.
8.  `getUserMedia` API'si ile kullanıcı kamera görüntüsü arayüze eklendi ve aç/kapat butonu eklendi.
9.  Backend ve Frontend arasındaki iletişim `fetch` API'si ile sağlandı.
