import requests
import base64
import json

def test_soru_cozucu():
    url = "http://127.0.0.1:8000/soru-coz"
    image_path = "soru.jpg" # Test edeceğin fotoğrafın adı

    # 1. Fotoğrafı Base64'e çevir
    with open(image_path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read()).decode('utf-8')

    # 2. İsteği hazırla
    payload = {
        "user_id": 123,
        "image_base64": encoded_string,
        "soru_metni": "Bu soruyu adım adım çözer misin?"
    }

    print("🚀 Soru sunucuya gönderiliyor, AI düşünmeye başladı...")

    # 3. İsteği gönder
    try:
        response = requests.post(url, json=payload)
        
        if response.status_code == 200:
            result = response.json()
            print("\n✅ AI ÇÖZÜMÜ:\n")
            print(result["cozum"])
        else:
            print(f"❌ Hata oluştu: {response.status_code}")
            print(response.text)
            
    except Exception as e:
        print(f"📡 Sunucuya bağlanılamadı: {e}")

if __name__ == "__main__":
    test_soru_cozucu()