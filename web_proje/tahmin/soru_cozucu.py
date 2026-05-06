# soru_cozucu.py
from ai_danisman import client # Mevcut OpenAI istemcini kullanıyoruz

def soruyu_analiz_et(image_base64, ek_not="Bu soruyu adım adım açıklar mısın?"):
    try:
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {
                    "role": "system",
                    "content": "Sen 'DeepSolve' adında, fütüristik ve cyberpunk temalı bir görsel analiz yapay zekasısın. Soruları sanki bir 'Sistem Taraması' (System Scan) yapıyormuş gibi analiz eder, adım adım ve mantığını sökerek öğrenciye anlatırsın. Açıklamalarında 'Sistem taraması başlatıldı', 'Veri işleniyor', 'Çözüm algoritması oluşturuldu' gibi havalı bir terminal dili kullan ama asıl önceliğin öğrenciye konuyu net ve doğru öğretmek olsun. Markdown kullanarak yanıt ver."
                },
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": ek_not},
                        {
                            "type": "image_url",
                            "image_url": {"url": f"data:image/jpeg;base64,{image_base64}"}
                        },
                    ],
                }
            ],
            max_tokens=1000
        )
        return response.choices[0].message.content
    except Exception as e:
        print(f"Soru Çözme Hatası: {e}")
        return None