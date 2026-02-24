# Web Tasarımı ve Programlama - LAB-2

Bu proje, Web Tasarımı ve Programlama dersi kapsamında, **Semantik HTML5**, **Erişilebilirlik (a11y)** ve **Form Temelleri** ilkeleri uygulanarak geliştirilmiş profesyonel bir Kişisel Portföy sayfasıdır.

## 👤 Bilgilerim
- **İsim:** Oğulcan Narin
- **Ünvan:** Yazılım Mühendisi Öğrencisi
- **LinkedIn:** [Profilim](https://www.linkedin.com/feed/)
- **GitHub:** [Projelerim](https://github.com/)

## 🚀 Proje Özellikleri

### 1. Semantik HTML5 Yapısı
Sayfa, içeriğin sadece nasıl göründüğünü değil, ne anlama geldiğini tanımlayan modern HTML5 etiketleri kullanılarak inşa edilmiştir:
- `<header>`, `<nav>`, `<main>`, `<section>`, `<article>`, `<footer>`

### 2. Erişilebilirlik (Accessibility - a11y)
Görme veya motor engelli kullanıcılar dahil herkesin siteyi kullanabilmesi için şu özellikler eklenmiştir:
- **Skip Link:** Sayfanın en başında, klavye kullanıcıları için navigasyonu atlayan gizli bağlantı.
- **Heading Hiyerarşisi:** Doğru sıralanmış başlıklar (`h1`'den `h3`'e).
- **Alt Metinler:** Tüm görseller için açıklayıcı `alt` öznitelikleri ve `figcaption` etiketleri.
- **Tab Navigasyonu:** Fare olmadan sadece klavye (`Tab`) ile tam kontrol.
- **Focus Indicators:** Odaklanılan elemanlar için belirgin görsel geri bildirim.

### 3. Erişilebilir İletişim Formu
- Tüm form alanları `label` ile ilişkilendirilmiştir.
- HTML5 yerleşik doğrulama (`required`, `minlength`, `type="email"`) kullanılmıştır.
- Ekran okuyucular için dinamik hata mesajı alanları (`role="alert"`) eklenmiştir.

## 📊 Lighthouse Raporu
Projenin erişilebilirliği Google Lighthouse ile test edilmiş ve mükemmel skor elde edilmiştir.

**Skorlar:**
- 🟢 **Accessibility:** 100
- 🟢 **Best Practices:** 96
- 🟢 **SEO:** 91

![Lighthouse Raporu](lighthouse-report.png)

## 🛠️ Çalıştırma Talimatları

1. Proje dosyalarını indirin.
2. Terminal üzerinden bir yerel sunucu başlatın (Lighthouse testleri için gereklidir):
   ```bash
   python -m http.server 8000
   ```
3. Tarayıcınızdan `http://localhost:8000` adresine gidin.

---
*Bu çalışma Web Tasarımı ve Programlama LAB-2 föyü kriterlerine göre hazırlanmıştır.*
