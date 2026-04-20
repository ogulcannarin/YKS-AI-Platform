import os
from openai import OpenAI
from dotenv import load_dotenv

# .env dosyasındaki değişkenleri yükle
load_dotenv()

# OpenAI istemcisini oluştur (API Key otomatik olarak .env dosyasından çekilir)
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

def ai_sohbeti_baslat(puan=None, siralama=None, puan_turu=None, gercek_okullar=""):
    print("\n" + "="*50)
    print("🤖 YKS YAPAY ZEKA KOCU AKTIF")
    print("Sohbeti bitirmek icin 'cikis' yazabilirsiniz.")
    print("="*50 + "\n")
    
    # Sohbet geçmişini tutacağımız liste
    mesajlar = [
        {"role": "system", "content": "Sen uzman, motive edici ve samimi bir YKS Tercih Koçusun. Öğrenciye tavsiye verirken mutlaka kullanıcının sana ilettiği 'Senin Veri Setindeki Uygun Okullar' listesini baz alarak gerçekçi üniversite ve bölüm isimleri söyle. Yuvarlak cevaplar verme, nokta atışı okul isimleri öner."}
    ]
    
    # Eğer puan ve sıralama verildiyse ilk prompt'u hazırla
    if puan and siralama and puan_turu:
        ilk_durum = f"Merhaba! Ben {puan} puan alarak {puan_turu} türünde {siralama}. sıralama yaptım.\n\nSenin Veri Setindeki Uygun Okullar (Gerçek Veri):\n{gercek_okullar}\n\nBana bu sıralamayla ve listelediğim bu gerçek okullarla nasıl bir strateji çizmem gerektiğinden kısaca bahseder misin?"
        mesajlar.append({"role": "user", "content": ilk_durum})
        print("Yapay zeka analiz yapiyor, lutfen bekleyin...\n")
    else:
        ilk_durum = "Merhaba! Tercih sürecinde bana yardımcı olabilir misin?"
        mesajlar.append({"role": "user", "content": ilk_durum})
        print("Yapay zeka baglaniyor...\n")

    # İlk cevabı al ve ekrana bas
    try:
        response = client.chat.completions.create(
            model="gpt-4o", # veya gpt-3.5-turbo
            messages=mesajlar
        )
        ilk_cevap = response.choices[0].message.content
        mesajlar.append({"role": "assistant", "content": ilk_cevap})
        print(f"Koç: {ilk_cevap}\n")
    except Exception as e:
        print(f"Hata oluştu: {e}")
        return
    
    # Sonsuz soru-cevap döngüsü
    while True:
        kullanici_sorusu = input("Sen: ")
        
        if kullanici_sorusu.lower() in ['q', 'quit', 'cikis', 'çıkış', 'kapat']:
            print("\nKoç: Görüşmek üzere! Tercih döneminde başarılar dilerim. Ne zaman yardıma ihtiyacın olursa buradayım. 👋")
            break
            
        if not kullanici_sorusu.strip():
            continue
            
        # Kullanıcının sorusunu hafızaya ekle
        mesajlar.append({"role": "user", "content": kullanici_sorusu})
        
        try:
            # Yapay zekadan cevap al
            response = client.chat.completions.create(
                model="gpt-4o",
                messages=mesajlar
            )
            ai_cevabi = response.choices[0].message.content
            
            # Cevabı da hafızaya ekle ki konu kopmasın
            mesajlar.append({"role": "assistant", "content": ai_cevabi})
            
            print(f"\nKoç: {ai_cevabi}\n")
            
        except Exception as e:
            print(f"\nBir hata oluştu: {e}\n")

if __name__ == "__main__":
    # Test için rastgele bir puan ve sıralama gönderiyoruz
    ai_sohbeti_baslat(puan=420.5, siralama=63000, puan_turu="SAY")
