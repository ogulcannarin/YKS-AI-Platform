# soru_cozucu.py
from ai_danisman import client # Mevcut OpenAI istemcini kullanıyoruz

def soruyu_analiz_et(image_base64, ek_not="Bu soruyu adım adım açıklar mısın?"):
    try:
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {
                    "role": "system",
                    "content": "Sen profesyonel bir YKS öğretmenisin. Soruları adım adım, mantığını anlatarak çözersin."
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