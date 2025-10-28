require('dotenv').config(); 
const { StreamsApi, Configuration } = require('@d-id/node-sdk'); 
const axios = require('axios');

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

// Basic Auth token oluştur
const authToken = Buffer.from(`${D_ID_USERNAME}:${D_ID_PASSWORD}`).toString('base64');

async function main() {
  console.log('🤖 Avatar projesi (Node.js) başlıyor...');
  console.log('Kimlik doğrulaması yapılıyor...');
  
  let streamId = null;
  let sessionId = null;
  
  try {
    // ADIM 1: Stream oturumu oluştur
    console.log('Stream oturumu oluşturuluyor...');
    
    const createStreamRequest = {
      source_url: "https://d-id-public-bucket.s3.amazonaws.com/alice.jpg"
    };

    const createResponse = await streamsApi.createStream(createStreamRequest, {});
    streamId = createResponse.data.id;
    sessionId = createResponse.data.session_id;
    const iceServers = createResponse.data.ice_servers;
    const offerSdp = createResponse.data.offer;

    console.log('✅ Stream oluşturuldu!');
    console.log('Stream ID:', streamId);
    console.log('Session ID:', sessionId.substring(0, 50) + '...');
    console.log('ICE Servers:', iceServers ? `✓ ${iceServers.length} sunucu` : '✗ Yok');
    console.log('Offer SDP:', offerSdp ? '✓ Mevcut' : '✗ Yok');

    // ADIM 2: WebRTC bağlantısını başlat
    console.log('\n🔗 WebRTC bağlantısı başlatılıyor...');
    
    const answerSdp = "v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\na=group:BUNDLE 0\r\na=msid-semantic: WMS\r\nm=video 9 UDP/TLS/RTP/SAVPF 96\r\nc=IN IP4 0.0.0.0\r\na=rtcp:9 IN IP4 0.0.0.0\r\na=ice-ufrag:test\r\na=ice-pwd:test\r\na=ice-options:trickle\r\na=fingerprint:sha-256 00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00\r\na=setup:active\r\na=mid:0\r\na=sendrecv\r\na=rtcp-mux\r\na=rtcp-rsize\r\na=rtpmap:96 VP8/90000\r\n";
    
    const startConnectionRequest = {
      answer: {
        type: "answer",
        sdp: answerSdp
      },
      session_id: sessionId
    };

    const connectionResponse = await streamsApi.startConnection(streamId, startConnectionRequest, {});
    console.log('✅ WebRTC bağlantısı kuruldu!');
    console.log('Connection Status:', connectionResponse.status);

    // ADIM 3: Avatar'a konuşma komutu gönder (DOĞRUDAN REST API)
    console.log('\n📢 Avatar\'a konuşma komutu gönderiliyor (REST API ile)...');
    
    const talkPayload = {
      script: {
        type: "text",
        input: "Merhaba! Ben D-ID avatarıyım. Bu bir test mesajıdır.",
        provider: {
          type: "microsoft",
          voice_id: "tr-TR-AhmetNeural"
        }
      },
      config: {
        stitch: true
      }
    };

    // ✅ Doğrudan REST API çağrısı
    const talkResponse = await axios.post(
      `https://api.d-id.com/talks/streams/${streamId}`,
      talkPayload,
      {
        headers: {
          'Authorization': `Basic ${authToken}`,
          'Content-Type': 'application/json',
          'Cookie': sessionId
        }
      }
    );

    console.log('✅ Konuşma komutu gönderildi!');
    console.log('Talk Response Status:', talkResponse.status);
    console.log('Talk Response:', JSON.stringify(talkResponse.data, null, 2));

    // Konuşmanın bitmesini bekle
    console.log('\n⏳ 5 saniye bekleniyor (konuşma süresi)...');
    await new Promise(resolve => setTimeout(resolve, 5000));

    // ADIM 4: Stream'i sonlandır
    console.log('\n🛑 Stream sonlandırılıyor...');
    
    const deleteRequest = {
      session_id: sessionId
    };
    
    await streamsApi.deleteStream(streamId, deleteRequest, {});
    console.log('✅ Stream başarıyla sonlandırıldı!');

    console.log('\n🎉🎉🎉 TEST TAMAMEN BAŞARILI! 🎉🎉🎉');
    console.log('✅ Stream oluşturuldu');
    console.log('✅ WebRTC bağlantısı kuruldu');
    console.log('✅ Avatar\'a konuşma komutu gönderildi');
    console.log('✅ Stream temiz bir şekilde sonlandırıldı');

  } catch (error) {
    console.error('\n❌ Hata oluştu:');
    
    if (error.response) {
      console.error('HTTP Status:', error.response.status);
      console.error('Hata Detayı:', JSON.stringify(error.response.data, null, 2));
      console.error('Request Headers:', error.config?.headers);
    } else {
      console.error('Genel Hata:', error.message);
      console.error('Stack:', error.stack);
    }
    
    // Cleanup
    if (streamId && sessionId) {
      console.log('\n🧹 Cleanup: Stream sonlandırılıyor...');
      try {
        await streamsApi.deleteStream(streamId, { session_id: sessionId }, {});
        console.log('✅ Cleanup tamamlandı.');
      } catch (e) {
        console.error('⚠️ Cleanup hatası:', e.message);
      }
    }
    
    process.exit(1);
  }
}

main();