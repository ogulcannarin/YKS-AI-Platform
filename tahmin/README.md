<div align="center">
  <h1>🎯 YKS Yapay Zeka Tercih Asistanı & Sıralama Tahmincisi</h1>
  <p>Makine öğrenmesi ve Üretken Yapay Zeka (GenAI) ile güçlendirilmiş, akıllı YKS tercih robotu!</p>
  
  [![Python](https://img.shields.io/badge/Python-3.10+-blue.svg)](https://www.python.org/)
  [![Scikit-Learn](https://img.shields.io/badge/Machine%20Learning-Scikit--Learn-orange.svg)](https://scikit-learn.org/)
  [![OpenAI](https://img.shields.io/badge/AI-OpenAI%20GPT--4o-green.svg)](https://openai.com/)
  [![Pandas](https://img.shields.io/badge/Data-Pandas-150458.svg)](https://pandas.pydata.org/)
</div>

---

## 💡 Proje Hakkında
Bu proje, YKS (Yükseköğretim Kurumları Sınavı) öğrencilerinin sınav stresini ve tercih karmaşasını en aza indirmek için tasarlanmış kapsamlı bir karar destek sistemidir. Öğrencilerin netlerini alarak **4 farklı alanda (TYT, SAY, EA, SÖZ)** puanlarını hesaplar, makine öğrenmesi modelleriyle Türkiye geneli sıralamalarını tahmin eder, gerçek verisetleri üzerinden onlara uygun üniversiteleri süzer ve son olarak **OpenAI (GPT-4o)** entegrasyonu ile bu verileri harmanlayarak kullanıcıya kişiselleştirilmiş bir yapay zeka koçluk deneyimi sunar.

Proje, sadece basit bir puan hesaplayıcı olmanın çok ötesine geçerek; Veri Bilimi (Data Science), Makine Öğrenmesi (Machine Learning) ve Doğal Dil İşleme (NLP) teknolojilerini tek bir çatı altında birleştiren uçtan uca (end-to-end) bir veri projesi mimarisine sahiptir.

---

## 🧠 Kullanılan Teknikler ve Algoritmalar (Detaylı Mimari)

Projeyi geliştirirken verinin alınmasından kullanıcıya tavsiye sunulmasına kadar olan süreçte birden fazla gelişmiş teknik ve algoritma kullanılmıştır. Neler yaptığımızı ve projeye neler kattığımızı adım adım aşağıda görebilirsiniz:

### 1. Veri İşleme (Data Processing) & Özellik Mühendisliği (Feature Engineering)
Projenin temel taşı veridir. Geçmiş yıllara ait YKS taban puanları ve bölümleri içeren ham veriler işlenerek modele hazır hale getirilmiştir.
*   **Veri Temizleme (Data Cleaning):** `pandas` kütüphanesi kullanılarak `lisans_temiz.csv` ve `on_lisans_temiz.csv` dosyalarındaki NaN (eksik) değerli satırlar tespit edilmiş ve `dropna()` fonksiyonu ile veri setinden arındırılmıştır. Sütun isimleri makine öğrenmesi modelinin kolay işleyebileceği formata (score, type vb.) dönüştürülmüştür.
*   **Label Encoding (Etiket Kodlama):** Makine öğrenmesi modelleri doğrudan metin verileri (örneğin: "SAY", "EA", "SÖZ") ile çalışamazlar. `scikit-learn` kütüphanesinden `LabelEncoder` algoritması kullanılarak, kategorik puan türü değişkenleri sayısal (0, 1, 2) vektörlere dönüştürülmüştür.

### 2. Lineer İnterpolasyon (Linear Interpolation) ile Etiket Üretimi
Makine öğrenmesi modelimizi eğitebilmek için veri setimizdeki her bir üniversite bölümünün (taban puanı bilinen) **"hangi sıralamaya"** denk geldiğini bulmamız gerekiyordu. 
*   **Kullanılan Algoritma:** `numpy.interp` (Tek boyutlu parçalı lineer interpolasyon).
*   **Nasıl Çalışır?:** YKS sonuçlarında belirlenmiş referans kilit puanlar ve bu puanlara denk gelen yığılmalı sıralamalar sözlük yapıları (dictionary) şeklinde (`yks_verileri`) sisteme girildi. `siralama_tahmin_et` fonksiyonu, bir bölümün taban puanını alır, referans değerlerin arasına yerleştirir ve o puanın denk geldiği tahmini sıralamayı matematiksel oranlamayla hesaplar. Bu işlem, veri setindeki on binlerce satır (bölüm) için otomatik çalıştırılarak ML modelinin **hedef değişkeni (target label - est_ranking)** oluşturuldu.

### 3. Makine Öğrenmesi ile Sıralama Tahmini (Random Forest Regressor)
Öğrencinin alacağı puanın hangi sıralamaya denk geleceği her sene sınavın zorluğuna göre değişir. Bunu modellemek için regresyon (sürekli sayısal değer tahmini) algoritmalarına başvurduk.
*   **Kullanılan Algoritma:** **Random Forest Regressor (Rassal Orman Regresyonu)**
*   **Neden Random Forest?:** Puanlar ve sıralamalar arasındaki ilişki doğrusal (linear) değildir (örneğin 400 ile 410 puan arası sıralama farkı ile 250 ile 260 arası sıralama farkı uçurumludur). Random Forest, birden fazla karar ağacını (decision tree) bir araya getirerek (ensemble learning) çalıştığı için bu tür lineer olmayan, dengesiz ve varyansı yüksek verileri modellemede son derece başarılıdır. Ayrıca aşırı öğrenmeye (overfitting) karşı dirençlidir.
*   **Modelin Eğitimi:** Sistem her başlatıldığında arka planda `lisans` verisiyle **AYT modeli**, `ön lisans` verisiyle ise **TYT modeli** eğitilir. 100 farklı karar ağacı (`n_estimators=100`) oluşturularak optimal tahmin kapasitesine ulaşılır.

### 4. RAG (Retrieval-Augmented Generation) Esintili Mimari ve Akıllı Filtreleme
Yapay Zeka (LLM) modellerinin en büyük dezavantajı "halüsinasyon" görmeleridir (olmayan bir üniversiteyi ya da bölümü uydurmak). Projede bunu engellemek için **Bağlam Enjeksiyonu (Context Injection / RAG konsepti)** kullanıldı.
*   **Dinamik Veri Filtreleme:** Öğrencinin puanı hesaplandığı an, `pandas` ile binlerce okulluk veri tabanında bir koşul sorgusu çalışır: *(Bölümün Taban Puanı <= Öğrencinin Puanı)* ve *(Bölüm Türü == Öğrencinin Türü)*. Bu filtrelemeden geçen en iyi 15 gerçek okul listelenir.
*   **Context Injection:** Çekilen bu "Gerçek Üniversiteler ve Bölümler" listesi, ChatGPT'ye gönderilen istemin (prompt) içine gömülür. Yapay zekaya "Sadece sana sağladığım bu listedeki okulları baz alarak yorum yap" talimatı verilir. Böylece öğrenci %100 gerçek verilere dayalı, halüsinasyonsuz ve güvenilir bir danışmanlık alır.

### 5. Üretken Yapay Zeka (GenAI) ve Prompt Engineering
*   **Sistem Rolü (System Prompt):** OpenAI `gpt-4o` API'sine "Sen uzman, motive edici ve samimi bir YKS Tercih Koçusun" kimliği kodlanmıştır. Algoritma yuvarlak cevaplardan kaçınması ve nokta atışı tavsiyeler vermesi üzerine yapılandırılmıştır.
*   **Hafıza Yönetimi (Memory Tracking):** Sadece tek seferlik bir soru-cevap değil, bir sohbet ortamı kurguladık. `ai_danisman.py` içerisinde kullanıcının her sorusu ve yapay zekanın her cevabı bir dizi (array) içerisinde tutulur ve her yeni soruda OpenAI sunucularına tüm geçmiş loglar tekrar gönderilir. Bu algoritmik yaklaşım, yapay zekanın **"bağlamı (context)" unutmadan**, bir önceki mesajda ne konuşulduğunu hatırlayarak cevap vermesini sağlar.

### 6. Kural Tabanlı Puan Hesaplama Algoritması
*   Kullanıcının girdiği 10 farklı dersin (TYT Türkçe, AYT Fizik vs.) netleri, ÖSYM'nin belirlediği resmi katsayı matrisleriyle (örneğin SAY için Fizik katsayısı 2.85, Kimya 3.07) çarpılır. Ortaöğretim Başarı Puanı'nın (OBP) %60 etkisi de hesaplamaya dinamik olarak dahil edilerek Ham ve Yerleştirme Puanları son derece hassas (float düzeyinde) elde edilir.

---

## ✨ Öne Çıkan Özellikler (Kullanıcı Deneyimi)
- 🚀 **Tek Ekranda 4 Alan:** Öğrencinin netlerine göre TYT, Sayısal, Eşit Ağırlık ve Sözel yerleştirme puanlarını aynı anda tek bir tuşla hesaplar.
- 🧠 **Dinamik Model Eğitimi:** Modeller önceden eğitilmiş statik dosyalar değildir; kodu her çalıştırdığınızda veri setinden en güncel ağaçları (Random Forest) anlık olarak kurar.
- 🤖 **İnteraktif Terminal Chatbotu:** Terminal üzerinden ChatGPT ile kesintisiz mesajlaşma, doğrudan YKS odaklı akış.

---

## 🛠️ Teknoloji Yığını
- **Python:** Temel geliştirme dili
- **Pandas & NumPy:** Veri analizi, manipülasyonu ve doğrusal hesaplamalar
- **Scikit-Learn:** Random Forest Regressor ile model eğitimi ve Label Encoding
- **OpenAI API:** GPT-4o ile etkileşimli LLM sohbet robotu
- **Dotenv:** Güvenli API anahtarı yönetimi (Ortam değişkenleri)

---

## 🚀 Kurulum Adımları

Projeyi bilgisayarınızda çalıştırmak oldukça basittir.

### 1. Gerekli Kütüphaneleri Kurun
Terminal veya Komut Satırı üzerinden projenin bulunduğu klasöre gidin ve şu komutu çalıştırın:
```bash
pip install -r requirements.txt
```

### 2. OpenAI API Anahtarınızı Ekleyin
Proje dizininde (birlestir.py ile aynı yerde) gizli bir `.env` dosyası oluşturun ve içerisine API anahtarınızı yapıştırın:
```env
OPENAI_API_KEY=sk-kendi_api_anahtarinizi_buraya_yaziniz
```

---

## 🎮 Nasıl Kullanılır?

Terminal üzerinden ana programı başlatın:
```bash
python birlestir.py
```

1. Ekrana çıkan yönergeleri takip ederek **OBP**'nizi ve **Netlerinizi** girin (Boş bırakmak istediklerinizi direkt `Enter`'a basarak geçebilirsiniz).
2. Sistem size **Hesaplanan Puanlarınızı ve Tahmini Sıralamanızı (Makine Öğrenmesi Modeli ile)** sunacaktır.
3. Hangi alanda (TYT/SAY/EA/SOZ) destek almak istediğinizi yazın.
4. Yapay Zeka Koçunuz devreye girecek! Model arka planda uygun gerçek üniversiteleri bulup sisteme yükledi. Artık ona tercih stratejilerinizi sorabilirsiniz. (Sohbetten çıkmak için `cikis` yazmanız yeterlidir).

---

## 🔮 Gelecek Hedefleri (Roadmap)
- [ ] Web arayüzü (Streamlit / Gradio / React) entegrasyonu eklenecek.
- [ ] Daha büyük veri setleri ile derin öğrenme (Deep Learning) modelleri kurulacak.
- [ ] Kullanıcı sohbeti sonrası yapay zekanın derlediği tercih listesini PDF olarak indirebilme özelliği gelecek.

<div align="center">
  <br>
  <p><i>Bu proje açık kaynaktır ve geliştirilmeye açıktır! Kodları inceleyebilir, geliştirebilir ve Pull Request gönderebilirsiniz. 🌟</i></p>
</div>
