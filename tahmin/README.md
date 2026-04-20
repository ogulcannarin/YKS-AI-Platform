<div align="center">
  <h1>🎯 YKS Yapay Zeka Tercih Asistanı & Sıralama Tahmincisi</h1>
  <p>Makine öğrenmesi ve Üretken Yapay Zeka (GenAI) ile güçlendirilmiş, akıllı YKS tercih robotu!</p>
  
  [![Python](https://img.shields.io/badge/Python-3.10+-blue.svg)](https://www.python.org/)
  [![Scikit-Learn](https://img.shields.io/badge/Machine%20Learning-Scikit--Learn-orange.svg)](https://scikit-learn.org/)
  [![OpenAI](https://img.shields.io/badge/AI-OpenAI%20GPT--4o-green.svg)](https://openai.com/)
</div>

---

## 💡 Proje Hakkında
Bu proje, YKS (Yükseköğretim Kurumları Sınavı) öğrencilerinin sınav stresini ve tercih karmaşasını azaltmak için geliştirilmiştir. Öğrencilerin netlerini alarak **4 farklı alanda (TYT, SAY, EA, SÖZ)** sıralamalarını tahmin eder. Ardından güncel üniversite veri setinden bu sıralamaya uygun üniversiteleri süzer ve **ChatGPT (OpenAI)** entegrasyonu sayesinde kişiselleştirilmiş bir yapay zeka koçluk hizmeti sunar.

## ✨ Öne Çıkan Özellikler
- 🚀 **Tek Ekranda 4 Alan:** Öğrencinin netlerine göre TYT, Sayısal, Eşit Ağırlık ve Sözel yerleştirme puanlarını aynı anda hesaplar.
- 🧠 **Random Forest Algoritması:** Klasik katsayı hesaplamalarının ötesinde, geçmiş verilerle eğitilmiş bir **Makine Öğrenmesi** modeli kullanarak gerçekçi sıralama tahminleri yapar.
- 📊 **Gerçek Veri Filtreleme:** Puanınız hesaplandığı an, arka planda binlerce satırlık güncel taban puanları dosyasından (`lisans_temiz.csv`) size uygun okullar anında filtrelenir.
- 🤖 **Yapay Zeka Koçluğu:** Sıralamanız ve kazanabileceğiniz *gerçek okullar* ChatGPT'ye gönderilir. Size özel bir koç gibi tavsiyelerde bulunur ve sizinle karşılıklı mesajlaşır!

---

## 🛠️ Teknoloji Yığını
- **Python:** Temel geliştirme dili
- **Pandas & NumPy:** Veri analizi ve işleme
- **Scikit-Learn:** Random Forest Regressor ile model eğitimi
- **OpenAI API:** GPT-4o ile etkileşimli sohbet robotu
- **Dotenv:** Güvenli API anahtarı yönetimi

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
2. Sistem size **Hesaplanan Puanlarınızı ve Tahmini Sıralamanızı** sunacaktır.
3. Hangi alanda (TYT/SAY/EA/SOZ) destek almak istediğinizi yazın.
4. Yapay Zeka Koçunuz devreye girecek! Artık ona tercih stratejilerinizi sorabilirsiniz. (Sohbetten çıkmak için `cikis` yazmanız yeterlidir).

---

## 🔮 Gelecek Hedefleri (Roadmap)
- [ ] Web arayüzü (Streamlit / Gradio) entegrasyonu eklenecek.
- [ ] Daha büyük veri setleri ile derin öğrenme (Deep Learning) modelleri kurulacak.
- [ ] Kullanıcı sohbeti sonrası tercih listesini PDF olarak indirebilme özelliği gelecek.

<div align="center">
  <br>
  <p><i>Bu proje açık kaynaktır ve geliştirilmeye açıktır! Kodları inceleyebilir, geliştirebilir ve Pull Request gönderebilirsiniz. 🌟</i></p>
</div>
