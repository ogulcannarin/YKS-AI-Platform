import { useState, useEffect, useRef } from 'react';
import { Send, Bot, User, Plus, Sparkles } from 'lucide-react';
import ReactMarkdown from 'react-markdown';

const INITIAL_MSG = {
  role: 'ai',
  content: 'Merhaba! Ben senin YKS AI koçunum 🎯\n\nPuan hesaplama sonuçlarını analiz edebilir, zayıf konularını belirleyebilir ve sana özel çalışma stratejisi oluşturabilirim.\n\nNe öğrenmek istersin?',
};

const SUGGESTED_QUESTIONS = [
  'Hangi konulara odaklanmalıyım?',
  'Günlük çalışma planı oluştur',
  'TYT matematik stratejisi',
  'Sıralamamı nasıl yükseltirim?',
];

export default function AiDanisman({ session, results }) {
  const [soru, setSoru]         = useState('');
  const [mesajlar, setMesajlar] = useState([INITIAL_MSG]);
  const [yukleniyor, setYukleniyor] = useState(false);
  const messagesEndRef = useRef(null);
  const inputRef       = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [mesajlar]);

  const handleDanis = async (sorguText) => {
    const q = sorguText ?? soru;
    if (!q.trim()) return;

    const yeniMesaj = { role: 'user', content: q };
    setMesajlar(prev => [...prev, yeniMesaj]);
    setSoru('');
    setYukleniyor(true);

    try {
      const payload = {
        user_id: 123,
        soru: q,
        puan: results?.SAY?.puan || 0,
        siralama: results?.SAY?.siralama || 0,
        puan_turu: 'SAY',
      };
      const res  = await fetch('http://127.0.0.1:8000/ai-danis', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const data = await res.json();

      if (res.ok && data.basarili) {
        setMesajlar(prev => [...prev, { role: 'ai', content: data.cevap }]);
      } else {
        setMesajlar(prev => [...prev, { role: 'ai', content: 'Sistem hatası: ' + (data.detail || 'Bağlantı koptu.') }]);
      }
    } catch {
      setMesajlar(prev => [...prev, {
        role: 'ai',
        content: 'Backend\'e bağlanılamadı. Lütfen `tahmin/` dizinindeki `api.py` dosyasını çalıştırdığından emin ol.',
      }]);
    } finally {
      setYukleniyor(false);
      setTimeout(() => inputRef.current?.focus(), 100);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleDanis();
    }
  };

  return (
    <div style={{
      display: 'flex',
      height: 'calc(100vh - 0px)',
      maxHeight: '100vh',
      overflow: 'hidden',
    }}>

      {/* ── Sidebar ── */}
      <div style={{
        width: '220px', flexShrink: 0,
        borderRight: '1px solid var(--border-subtle)',
        display: 'flex', flexDirection: 'column',
        background: 'rgba(0,0,0,0.2)',
        padding: '1.25rem 0.75rem',
        gap: '0.5rem',
      }}>
        <button
          className="btn btn-primary btn-sm btn-full"
          style={{ marginBottom: '0.75rem', justifyContent: 'flex-start' }}
          onClick={() => setMesajlar([INITIAL_MSG])}
        >
          <Plus size={14} /> Yeni Sohbet
        </button>

        <div style={{ fontSize: '0.65rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.1em', color: 'var(--text-muted)', padding: '0 0.5rem', fontFamily: 'var(--font-mono)' }}>
          Son Oturumlar
        </div>

        {[
          { label: 'Tıp Fakültesi Hedefi', active: true },
          { label: 'TYT Matematik Netleri', active: false },
          { label: 'Haftalık Plan', active: false },
        ].map((s, i) => (
          <button key={i} style={{
            background: s.active ? 'var(--neon-purple-dim)' : 'transparent',
            border: `1px solid ${s.active ? 'rgba(139,127,232,0.25)' : 'transparent'}`,
            borderRadius: 'var(--radius-md)',
            padding: '0.55rem 0.75rem',
            color: s.active ? 'var(--neon-purple)' : 'var(--text-muted)',
            fontSize: '0.8rem',
            textAlign: 'left',
            cursor: 'pointer',
            fontFamily: 'var(--font-body)',
            transition: 'all 0.2s',
            width: '100%',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
          }}>
            {s.label}
          </button>
        ))}

        {/* Context info */}
        {results && (
          <div style={{
            marginTop: 'auto', padding: '0.875rem', background: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)',
          }}>
            <div style={{ fontSize: '0.65rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginBottom: '0.5rem' }}>
              Aktif Bağlam
            </div>
            {Object.entries(results).map(([alan, veri]) => (
              <div key={alan} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '0.25rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>{alan}</span>
                <span style={{ color: 'var(--neon-green)', fontFamily: 'var(--font-mono)', fontWeight: 700 }}>
                  {veri.puan?.toFixed(1)}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* ── Chat Area ── */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>

        {/* Chat Header */}
        <div style={{
          padding: '1rem 1.5rem',
          borderBottom: '1px solid var(--border-subtle)',
          display: 'flex', alignItems: 'center', gap: '0.75rem',
          background: 'rgba(0,0,0,0.15)',
          flexShrink: 0,
        }}>
          <div style={{
            width: 38, height: 38, borderRadius: 'var(--radius-md)',
            background: 'linear-gradient(135deg, var(--neon-purple), #5B4FD4)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: 'var(--glow-purple)',
          }}>
            <Bot size={18} color="#fff" />
          </div>
          <div>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '0.95rem' }}>AI Koç</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.72rem', color: 'var(--neon-green)' }}>
              <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--neon-green)', display: 'inline-block', boxShadow: '0 0 6px var(--neon-green)' }} />
              Çevrimiçi · YKS Uzmanı
            </div>
          </div>

          <div style={{ marginLeft: 'auto' }}>
            <span className="badge badge-purple">
              <Sparkles size={10} /> Gemini AI
            </span>
          </div>
        </div>

        {/* Messages */}
        <div style={{
          flex: 1, overflowY: 'auto', padding: '1.5rem',
          display: 'flex', flexDirection: 'column', gap: '1.25rem',
        }}
          className="scrollbar-hidden"
        >
          {/* Suggested questions (only at start) */}
          {mesajlar.length === 1 && (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '0.5rem' }}>
              {SUGGESTED_QUESTIONS.map(q => (
                <button
                  key={q}
                  onClick={() => handleDanis(q)}
                  style={{
                    background: 'var(--bg-card)', border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-full)', padding: '0.4rem 0.875rem',
                    fontSize: '0.8rem', color: 'var(--text-secondary)',
                    cursor: 'pointer', fontFamily: 'var(--font-body)',
                    transition: 'all 0.2s',
                  }}
                  onMouseEnter={e => { e.target.style.borderColor = 'var(--border-glow)'; e.target.style.color = 'var(--neon-purple)'; }}
                  onMouseLeave={e => { e.target.style.borderColor = 'var(--border-subtle)'; e.target.style.color = 'var(--text-secondary)'; }}
                >
                  {q}
                </button>
              ))}
            </div>
          )}

          {mesajlar.map((msg, idx) => (
            <div key={idx} style={{
              display: 'flex',
              justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
              gap: '0.75rem',
              alignItems: 'flex-end',
            }}>
              {/* AI avatar */}
              {msg.role === 'ai' && (
                <div style={{
                  width: 32, height: 32, borderRadius: 'var(--radius-md)', flexShrink: 0,
                  background: 'linear-gradient(135deg, var(--neon-purple), #5B4FD4)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  marginBottom: '2px',
                }}>
                  <Bot size={15} color="#fff" />
                </div>
              )}

              {/* Message bubble */}
              <div style={{
                maxWidth: '72%',
                padding: '0.875rem 1.125rem',
                borderRadius: msg.role === 'user' ? '18px 18px 4px 18px' : '4px 18px 18px 18px',
                background: msg.role === 'user'
                  ? 'linear-gradient(135deg, var(--neon-purple), #6B5FD4)'
                  : 'var(--bg-secondary)',
                color: msg.role === 'user' ? '#fff' : 'var(--text-primary)',
                border: msg.role === 'ai' ? '1px solid var(--border-subtle)' : 'none',
                borderLeft: msg.role === 'ai' ? '2px solid var(--neon-green)' : undefined,
                fontSize: '0.9rem',
                lineHeight: 1.65,
                boxShadow: msg.role === 'user' ? '0 4px 16px rgba(139,127,232,0.35)' : 'var(--shadow-sm)',
              }}>
                {msg.role === 'ai' ? (
                  <ReactMarkdown
                    components={{
                      p: ({ children }) => <p style={{ margin: '0 0 0.5rem', lineHeight: 1.65 }}>{children}</p>,
                      strong: ({ children }) => <strong style={{ color: 'var(--neon-purple)', fontWeight: 700 }}>{children}</strong>,
                      ul: ({ children }) => <ul style={{ paddingLeft: '1.25rem', marginTop: '0.5rem' }}>{children}</ul>,
                      li: ({ children }) => <li style={{ marginBottom: '0.25rem' }}>{children}</li>,
                      code: ({ children }) => <code style={{ fontFamily: 'var(--font-mono)', fontSize: '0.85em', background: 'rgba(139,127,232,0.15)', padding: '0.1em 0.4em', borderRadius: '4px' }}>{children}</code>,
                    }}
                  >
                    {msg.content}
                  </ReactMarkdown>
                ) : (
                  msg.content
                )}
              </div>

              {/* User avatar */}
              {msg.role === 'user' && (
                <div style={{
                  width: 32, height: 32, borderRadius: 'var(--radius-md)', flexShrink: 0,
                  background: 'linear-gradient(135deg, var(--neon-green), #16A374)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  marginBottom: '2px',
                }}>
                  <User size={15} color="#000" />
                </div>
              )}
            </div>
          ))}

          {/* Loading (dot animation) */}
          {yukleniyor && (
            <div style={{ display: 'flex', alignItems: 'flex-end', gap: '0.75rem' }}>
              <div style={{
                width: 32, height: 32, borderRadius: 'var(--radius-md)', flexShrink: 0,
                background: 'linear-gradient(135deg, var(--neon-purple), #5B4FD4)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>
                <Bot size={15} color="#fff" />
              </div>
              <div style={{
                padding: '0.875rem 1.25rem',
                borderRadius: '4px 18px 18px 18px',
                background: 'var(--bg-secondary)',
                border: '1px solid var(--border-subtle)',
                borderLeft: '2px solid var(--neon-green)',
              }}>
                <div className="dot-loader">
                  <span /><span /><span />
                </div>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Input Area */}
        <div style={{
          padding: '1rem 1.5rem',
          borderTop: '1px solid var(--border-subtle)',
          background: 'rgba(8,8,16,0.8)',
          backdropFilter: 'blur(12px)',
          flexShrink: 0,
        }}>
          <div style={{ position: 'relative', display: 'flex', gap: '0.75rem', alignItems: 'flex-end' }}>
            <div style={{ flex: 1, position: 'relative' }}>
              <textarea
                ref={inputRef}
                value={soru}
                onChange={e => setSoru(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Sorunuzu yazın... (Enter ile gönder, Shift+Enter yeni satır)"
                rows={1}
                style={{
                  width: '100%',
                  background: 'rgba(0,0,0,0.5)',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-lg)',
                  padding: '0.875rem 1.25rem',
                  paddingRight: '1rem',
                  color: 'var(--text-primary)',
                  fontFamily: 'var(--font-body)',
                  fontSize: '0.9rem',
                  resize: 'none',
                  outline: 'none',
                  lineHeight: 1.5,
                  transition: 'border-color 0.2s, box-shadow 0.2s',
                  minHeight: '48px',
                  maxHeight: '160px',
                  overflow: 'auto',
                }}
                onFocus={e => {
                  e.target.style.borderColor = 'rgba(139,127,232,0.5)';
                  e.target.style.boxShadow = '0 0 0 3px rgba(139,127,232,0.1)';
                }}
                onBlur={e => {
                  e.target.style.borderColor = 'var(--border-subtle)';
                  e.target.style.boxShadow = 'none';
                }}
              />
            </div>

            <button
              onClick={() => handleDanis()}
              disabled={yukleniyor || !soru.trim()}
              style={{
                width: 48, height: 48, borderRadius: 'var(--radius-lg)', flexShrink: 0,
                background: soru.trim()
                  ? 'linear-gradient(135deg, var(--neon-purple), #6B5FD4)'
                  : 'var(--bg-card)',
                border: `1px solid ${soru.trim() ? 'transparent' : 'var(--border-subtle)'}`,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                cursor: soru.trim() ? 'pointer' : 'not-allowed',
                transition: 'all 0.2s',
                boxShadow: soru.trim() ? '0 4px 16px rgba(139,127,232,0.4)' : 'none',
                color: soru.trim() ? '#fff' : 'var(--text-muted)',
              }}
            >
              <Send size={18} />
            </button>
          </div>

          <div style={{ display: 'flex', justifyContent: 'center', marginTop: '0.5rem' }}>
            <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
              AI yanıtları hatalı olabilir. Önemli kararlar için uzman görüşü alın.
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
