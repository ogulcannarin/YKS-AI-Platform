# 🤖 Canlı AI Avatar - Frontend Arayüzü

Bu proje, Canlı AI Avatar'ın kullanıcı arayüzünü (frontend) içerir. React, Vite ve WebRTC teknolojilerini kullanarak D-ID avatarı ile gerçek zamanlı, görüntülü ve sesli bir sohbet deneyimi sunar.

Kullanıcı arayüzü, arka planda çalışan `server.js` sunucusuna bağlanarak D-ID ve OpenAI servislerini kullanır.

## ⚠️ Önemli Bağımlılık

Bu frontend projesi, tek başına **çalışmaz**. Arka planda, bir üst klasörde (`AvatarProjem`) bulunan `server.js` sunucusunun çalışıyor olması gerekmektedir.

* Tüm D-ID API çağrıları, OpenAI (GPT) istekleri ve kimlik doğrulama işlemleri o sunucu tarafından yönetilir.
* Frontend, backend sunucusunun varsayılan olarak `http://localhost:3001` adresinde çalışmasını bekler.

## ✨ Temel Özellikler

* **Gerçek Zamanlı Avatar Akışı:** D-ID'den gelen WebRTC video akışını ekranda gösterir.
* **Görüntülü Konuşma:** Kullanıcının kendi kamerasını (`getUserMedia`) açar ve ekranda gösterir.
* **Sesli Komut:** `react-speech-recognition` kullanarak tarayıcı üzerinden sesinizi algılar ve metne dönüştürür.
* **Akıllı Cevaplar:** Gönderilen metin, backend üzerinden OpenAI'ye iletilir ve avatar, yapay zekanın ürettiği cevabı konuşur.
* **Kontroller:** Kamera aç/kapat, avatarı sessize al/aç ve bağlantıyı kesme butonları.

## 🛠️ Kullanılan Teknolojiler

* **React:** Kullanıcı arayüzü kütüphanesi.
* **Vite:** Hızlı geliştirme ve build aracı.
* **`react-speech-recognition`:** Ses tanıma (Web Speech API) için.
* **`lucide-react`:** Arayüz ikonları için.
* **WebRTC (`RTCPeerConnection`):** Tarayıcı üzerinden canlı video akışı için.
* **`getUserMedia`:** Tarayıcı kamera erişimi için.
* **`fetch` API:** Backend sunucusu (`http://localhost:3001`) ile iletişim kurmak için.

## 🚀 Kurulum ve Çalıştırma

### 1. Adım: Backend Sunucusunu Başlatın

Bu arayüzün çalışması için **Terminal 1**'de backend sunucusunun çalışıyor olması gerekir.

1.  Bir terminal açın ve projenin ana klasörüne (`AvatarProjem`) gidin.
2.  Sunucuyu başlatın:
    ```bash
    node server.js
    ```
3.  `🚀 Avatar Backend sunucusu çalışıyor: http://localhost:3001` mesajını gördüğünüzde bu terminali açık bırakın.

### 2. Adım: Frontend Arayüzünü Başlatın

1.  **YENİ BİR** terminal açın (Terminal 2).
2.  Bu terminalde `frontend` klasörünün içine girin:
    ```bash
    cd frontend
    ```
3.  Gerekli kütüphaneleri yükleyin:
    ```bash
    npm install
    ```
4.  Frontend geliştirme sunucusunu başlatın:
    ```bash
    npm run dev
    ```
5.  Terminalde `➜ Local: http://localhost:5173/` (veya benzeri) bir adres göreceksiniz.
6.  Bu adresi tarayıcınızda açarak uygulamayı kullanmaya başlayabilirsiniz.
