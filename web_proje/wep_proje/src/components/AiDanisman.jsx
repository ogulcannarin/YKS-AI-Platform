import { useState, useEffect, useRef } from 'react';
import { Send, Terminal } from 'lucide-react';

export default function AiDanisman({ session, results }) {
    const [soru, setSoru] = useState('');
    const [mesajlar, setMesajlar] = useState([
        { role: 'ai', content: "Sistem başlatıldı. Bağlantı güvenli. Ben senin yapay zeka eğitim koçunum. Hangi hedefi hacklemek istersin?" }
    ]);
    const [yukleniyor, setYukleniyor] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [mesajlar]);

    const handleDanis = async () => {
        if (!soru.trim()) return;
        
        const yeniMesaj = { role: 'user', content: soru };
        setMesajlar(prev => [...prev, yeniMesaj]);
        setSoru('');
        setYukleniyor(true);
        
        try {
            const payload = {
                user_id: 123,
                soru: soru,
                puan: results?.SAY?.puan || 0,
                siralama: results?.SAY?.siralama || 0,
                puan_turu: 'SAY'
            };
            const res = await fetch('http://127.0.0.1:8000/ai-danis', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            
            if (res.ok && data.basarili) {
                setMesajlar(prev => [...prev, { role: 'ai', content: data.cevap }]);
            } else {
                setMesajlar(prev => [...prev, { role: 'ai', content: "Sistem hatası: " + (data.detail || "Bağlantı koptu.") }]);
            }
        } catch (e) {
            setMesajlar(prev => [...prev, { role: 'ai', content: "Sunucu yanıt vermiyor. Lütfen 'tahmin' dizinindeki backend'in çalıştığını doğrula." }]);
        } finally {
            setYukleniyor(false);
        }
    };

    return (
        <div style={{ display: 'flex', height: 'calc(100vh - 120px)', maxWidth: '1200px', margin: '0 auto', gap: '1rem' }} className="stagger-container">
            {/* Sidebar (History) */}
            <div className="glass-panel" style={{ width: '250px', display: 'flex', flexDirection: 'column', padding: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '2rem', borderBottom: '1px solid var(--border-glow)', paddingBottom: '1rem' }}>
                    <Terminal className="text-neon-green" />
                    <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1rem' }}>Oturumlar</h3>
                </div>
                <div style={{ flex: '1', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <div style={{ padding: '0.5rem', borderLeft: '2px solid var(--neon-green)', background: 'rgba(29, 158, 117, 0.1)', cursor: 'pointer', fontFamily: 'var(--font-body)', fontSize: '0.8rem' }}>
                        &gt; Tıp Fakültesi Hedefi
                    </div>
                    <div style={{ padding: '0.5rem', borderLeft: '2px solid transparent', cursor: 'pointer', color: 'var(--text-muted)', fontFamily: 'var(--font-body)', fontSize: '0.8rem' }}>
                        &gt; TYT Matematik Netleri
                    </div>
                </div>
            </div>

            {/* Chat Area */}
            <div className="glass-panel" style={{ flex: '1', display: 'flex', flexDirection: 'column', padding: '0', position: 'relative', overflow: 'hidden' }}>
                <div style={{ padding: '1rem 1.5rem', borderBottom: '1px solid var(--border-glow)', background: 'rgba(0,0,0,0.3)' }}>
                    <h2 style={{ fontFamily: 'var(--font-display)', fontSize: '1.2rem', color: 'var(--neon-purple)' }}>AI_KOÇ_v2.0</h2>
                </div>

                <div style={{ flex: '1', overflowY: 'auto', padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                    {mesajlar.map((msg, idx) => (
                        <div key={idx} style={{ 
                            display: 'flex', 
                            justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start' 
                        }}>
                            <div style={{
                                maxWidth: '80%',
                                padding: '1rem',
                                borderRadius: '12px',
                                background: msg.role === 'user' ? 'var(--neon-purple)' : 'var(--bg-secondary)',
                                color: msg.role === 'user' ? '#000' : 'var(--text-main)',
                                border: msg.role === 'ai' ? '1px solid var(--border-glow)' : 'none',
                                borderLeft: msg.role === 'ai' ? '4px solid var(--neon-green)' : 'none',
                                fontFamily: msg.role === 'ai' ? 'var(--font-body)' : 'inherit',
                                fontSize: '0.95rem',
                                whiteSpace: 'pre-wrap',
                                boxShadow: msg.role === 'user' ? '0 5px 15px rgba(127,119,221,0.3)' : 'none'
                            }}>
                                {msg.content}
                            </div>
                        </div>
                    ))}
                    
                    {yukleniyor && (
                        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
                            <div style={{ padding: '1rem', color: 'var(--neon-green)', fontFamily: 'var(--font-body)' }}>
                                <span className="typewriter" style={{ borderRight: '10px solid var(--neon-green)', paddingRight: '2px', animation: 'blink-caret .75s step-end infinite' }}>
                                    &gt; Analiz ediliyor
                                </span>
                            </div>
                        </div>
                    )}
                    <div ref={messagesEndRef} />
                </div>

                {/* Input Area */}
                <div style={{ 
                    padding: '1.5rem', 
                    background: 'rgba(10, 10, 15, 0.8)', 
                    backdropFilter: 'blur(10px)',
                    borderTop: '1px solid var(--border-glow)'
                }}>
                    <div style={{ display: 'flex', gap: '1rem', position: 'relative' }}>
                        <input 
                            type="text" 
                            value={soru}
                            onChange={(e) => setSoru(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && handleDanis()}
                            placeholder="Komut girin..."
                            style={{ 
                                flex: '1', 
                                background: 'rgba(0,0,0,0.5)', 
                                border: '1px solid var(--neon-purple)',
                                padding: '1rem 3rem 1rem 1.5rem',
                                borderRadius: '100px',
                                fontFamily: 'var(--font-body)',
                                outline: 'none',
                                color: 'var(--text-main)'
                            }}
                        />
                        <button 
                            onClick={handleDanis}
                            disabled={yukleniyor}
                            style={{
                                position: 'absolute',
                                right: '10px',
                                top: '50%',
                                transform: 'translateY(-50%)',
                                background: 'var(--neon-purple)',
                                color: '#000',
                                border: 'none',
                                width: '40px',
                                height: '40px',
                                borderRadius: '50%',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                cursor: 'pointer',
                                transition: 'all 0.3s ease',
                                boxShadow: '0 0 15px rgba(127,119,221,0.5)'
                            }}
                        >
                            <Send size={18} />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
