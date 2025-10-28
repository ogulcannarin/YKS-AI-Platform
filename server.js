require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { StreamsApi, Configuration } = require('@d-id/node-sdk');
const axios = require('axios');
const { OpenAI } = require('openai'); // OpenAI kütüphanesi zaten ekli

const app = express();
const PORT = 3001;

// Middleware
app.use(cors());
app.use(express.json());

// D-ID Configuration
const D_ID_API_KEY = process.env.D_ID_API_KEY;
if (!D_ID_API_KEY || !D_ID_API_KEY.includes(':')) {
  console.error("HATA: D_ID_API_KEY formatı yanlış veya eksik.");
  process.exit(1);
}

const keyParts = D_ID_API_KEY.split(':');
const D_ID_USERNAME = keyParts[0];
const D_ID_PASSWORD = keyParts[1];

const apiConfig = new Configuration({
  username: D_ID_USERNAME,
  password: D_ID_PASSWORD
});

const streamsApi = new StreamsApi(apiConfig);
const authToken = Buffer.from(`${D_ID_USERNAME}:${D_ID_PASSWORD}`).toString('base64');

// --- OpenAI Yapılandırması (Zaten Ekli) ---
const OPENAI_API_KEY = process.env.OPENAI_API_KEY; // .env'den OpenAI anahtarını oku
if (!OPENAI_API_KEY) {
  console.error("HATA: .env dosyasında OPENAI_API_KEY bulunamadı."); // Anahtar yoksa hata ver
  process.exit(1);
}
const openai = new OpenAI({
  apiKey: OPENAI_API_KEY, // OpenAI istemcisini başlat
});
// --- OpenAI Yapılandırması SONU ---

// Active streams map
const activeStreams = new Map();

// API Endpoints

// 1. Stream oluştur
app.post('/api/streams/create', async (req, res) => {
  try {
    console.log('📝 Yeni stream oluşturuluyor...');

    const { sourceUrl } = req.body;
    const createStreamRequest = {
      source_url: sourceUrl || "https://d-id-public-bucket.s3.amazonaws.com/alice.jpg"
    };

    const response = await streamsApi.createStream(createStreamRequest, {});
    const { id, session_id, ice_servers, offer } = response.data;

    activeStreams.set(id, {
      streamId: id,
      sessionId: session_id,
      createdAt: new Date()
    });

    console.log('✅ Stream oluşturuldu:', id);

    res.json({
      success: true,
      streamId: id,
      sessionId: session_id,
      iceServers: ice_servers,
      offer: offer
    });

  } catch (error) {
    console.error('❌ Stream oluşturma hatası:', error.response?.data || error.message);
    res.status(500).json({
      success: false,
      error: error.response?.data || error.message
    });
  }
});

// 2. WebRTC bağlantısını başlat
app.post('/api/streams/:streamId/start', async (req, res) => {
  try {
    const { streamId } = req.params;
    const { answer, sessionId } = req.body;

    console.log('🔗 WebRTC bağlantısı başlatılıyor:', streamId);

    const startConnectionRequest = {
      answer: answer,
      session_id: sessionId
    };

    const response = await streamsApi.startConnection(streamId, startConnectionRequest, {});

    console.log('✅ WebRTC bağlantısı kuruldu');

    res.json({
      success: true,
      status: response.status
    });

  } catch (error) {
    console.error('❌ Bağlantı başlatma hatası:', error.response?.data || error.message);
    res.status(500).json({
      success: false,
      error: error.response?.data || error.message
    });
  }
});

// --- BURASI GÜNCELLENDİ: OpenAI Entegrasyonu ---
// 3. Avatar'a konuştur (OpenAI Entegrasyonu ile GÜNCELLENDİ)
app.post('/api/streams/:streamId/talk', async (req, res) => {
  try {
    const { streamId } = req.params;
    const { text: userInput, voiceId, sessionId } = req.body; // Gelen metin artık 'userInput'

    console.log(`💬 Kullanıcıdan Gelen: "${userInput}"`);

    const streamData = activeStreams.get(streamId);
    if (!streamData) {
      return res.status(404).json({ success: false, error: 'Stream bulunamadı' });
    }

    // --- YENİ ADIM: OpenAI'ye Sor ---
    console.log('🧠 OpenAI\'ye cevap üretmesi için gönderiliyor...');
    let aiResponseText = "Üzgünüm, bir hata oluştu ve cevap üretemedim."; // Hata durumu için varsayılan cevap
    try {
        const completion = await openai.chat.completions.create({
          model: "gpt-3.5-turbo", // İsterseniz "gpt-4" gibi daha gelişmiş bir model kullanabilirsiniz
          messages: [
            // Avatarın kişiliğini burada tanımlayabilirsiniz
            { role: "system", content: "Sen D-ID tarafından canlandırılan yardımsever bir yapay zeka asistanısın. Cevapların kısa, net ve samimi olsun." },
            // Kullanıcının mesajı
            { role: "user", content: userInput }
          ],
          max_tokens: 70, // Cevapların maksimum uzunluğu
          temperature: 0.7, // Cevabın ne kadar yaratıcı olacağı (0.0 - 2.0)
        });

        // Cevabı al ve boşlukları temizle
        if (completion.choices && completion.choices.length > 0 && completion.choices[0].message?.content) {
           aiResponseText = completion.choices[0].message.content.trim();
        } else {
           console.error('OpenAI\'den beklenen formatta cevap alınamadı:', completion);
           // Hata durumunda varsayılan cevap kullanılacak
        }
    } catch (openaiError) {
        console.error('❌ OpenAI API hatası:', openaiError.response ? openaiError.response.data : openaiError.message);
        // OpenAI hatası durumunda varsayılan cevap kullanılacak
    }
    console.log(`🤖 OpenAI Cevabı: "${aiResponseText}"`);
    // --- YENİ ADIM SONU ---


    // --- D-ID'ye GÖNDERME KISMI (aiResponseText kullanılıyor) ---
    console.log('📢 Avatar\'a OpenAI cevabını konuşturma komutu gönderiliyor...');

    const talkPayload = {
      script: {
        type: "text",
        input: aiResponseText, // OpenAI'nin ürettiği cevabı D-ID'ye gönderiyoruz
        provider: {
          type: "microsoft", // Ses sağlayıcısı
          voice_id: voiceId || "tr-TR-AhmetNeural" // Frontend'den gelen sesi veya varsayılan Türkçeyi kullan
        }
      },
      config: {
        stitch: true // Birden fazla konuşma komutu gönderilirse videoları birleştirir
      },
      // Oturumu devam ettirmek için session_id göndermek önemli
      session_id: sessionId || streamData.sessionId
    };

    // D-ID REST API'sine isteği gönderiyoruz
    const response = await axios.post(
      `https://api.d-id.com/talks/streams/${streamId}`,
      talkPayload,
      {
        headers: {
          'Authorization': `Basic ${authToken}`, // Kimlik doğrulama
          'Content-Type': 'application/json'
          // 'Cookie' başlığına gerek yok, session_id payload içinde yeterli
        }
      }
    );

    console.log('✅ Konuşma komutu gönderildi (OpenAI Cevabı ile)');

    // Başarılı yanıtı frontend'e gönderiyoruz
    res.json({
      success: true,
      status: response.data.status // D-ID'den gelen durum (örn: "started")
    });
    // --- D-ID'ye GÖNDERME KISMI SONU ---

  } catch (error) {
    // Genel hata yakalama (OpenAI veya D-ID'den gelen hatalar için)
    console.error('❌ Konuşma endpoint hatası:', error.response?.data || error.message);
    if (error.config?.headers) {
        // Güvenlik için Authorization başlığını loglamıyoruz
        const safeHeaders = { ...error.config.headers };
        delete safeHeaders.Authorization;
        console.error('İstek Başlıkları (Auth Hariç):', safeHeaders);
    }
    // Frontend'e hata mesajını gönderiyoruz
    res.status(500).json({
      success: false,
      error: error.response?.data?.description || error.response?.data?.message || error.message || 'Bilinmeyen bir sunucu hatası oluştu.'
    });
  }
});
// --- GÜNCELLENEN BÖLÜMÜN SONU ---

// 4. Stream'i sonlandır
app.delete('/api/streams/:streamId', async (req, res) => {
  // ... (Bu ve sonraki endpointler aynı kaldı) ...
  try {
    const { streamId } = req.params;
    const { sessionId } = req.body;

    console.log('🛑 Stream sonlandırılıyor:', streamId);

    const streamData = activeStreams.get(streamId);

    const deleteRequest = {
      session_id: sessionId || streamData?.sessionId
    };

    await streamsApi.deleteStream(streamId, deleteRequest, {});

    activeStreams.delete(streamId);

    console.log('✅ Stream sonlandırıldı');

    res.json({
      success: true,
      message: 'Stream sonlandırıldı'
    });

  } catch (error) {
    console.error('❌ Stream sonlandırma hatası:', error.response?.data || error.message);
    res.status(500).json({
      success: false,
      error: error.response?.data || error.message
    });
  }
});

// 5. ICE candidate ekle
app.post('/api/streams/:streamId/ice', async (req, res) => {
  try {
    const { streamId } = req.params;
    const { candidate, sdpMLineIndex, sdpMid, sessionId } = req.body;

    const iceRequest = {
      candidate: candidate,
      sdpMLineIndex: sdpMLineIndex,
      sdpMid: sdpMid,
      session_id: sessionId
    };

    await streamsApi.addIceCandidate(streamId, iceRequest, {});

    res.json({ success: true });

  } catch (error) {
    console.error('❌ ICE candidate hatası:', error.response?.data || error.message);
    res.status(500).json({
      success: false,
      error: error.response?.data || error.message
    });
  }
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({
    status: 'ok',
    activeStreams: activeStreams.size,
    timestamp: new Date().toISOString()
  });
});

// Sunucuyu başlat
app.listen(PORT, () => {
  console.log(`🚀 Avatar Backend sunucusu çalışıyor: http://localhost:${PORT}`);
  console.log(`📊 Health check: http://localhost:${PORT}/api/health`);
});

// Graceful shutdown
process.on('SIGINT', async () => {
  console.log('\n🛑 Sunucu kapatılıyor...');

  // Tüm aktif stream'leri kapat
  for (const [streamId, streamData] of activeStreams.entries()) {
    try {
      await streamsApi.deleteStream(streamId, { session_id: streamData.sessionId }, {});
      console.log(`✅ Stream kapatıldı: ${streamId}`);
    } catch (error) {
      console.error(`⚠️ Stream kapatma hatası: ${streamId}`, error.message);
    }
  }

  process.exit(0);
});