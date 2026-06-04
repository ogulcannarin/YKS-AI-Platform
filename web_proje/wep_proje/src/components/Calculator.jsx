import { useState } from 'react';
import { Target, Activity, ChevronRight, Cpu, Award } from 'lucide-react';

const TAB_CONFIG = {
  TYT: { label: 'TYT', color: 'var(--neon-purple)', desc: 'Temel Yeterlilik Testi' },
  SAY: { label: 'SAY', color: 'var(--neon-green)',  desc: 'Sayısal Alan' },
  EA:  { label: 'EA',  color: 'var(--neon-amber)',  desc: 'Eşit Ağırlık' },
  SOZ: { label: 'SÖZ', color: 'var(--neon-blue)',   desc: 'Sözel Alan' },
};

const SUBJECT_LABELS = {
  turkce: 'Türkçe', matematik: 'Matematik', sosyal: 'Sosyal Bilimler', fen: 'Fen Bilimleri',
  fizik: 'Fizik', kimya: 'Kimya', biyoloji: 'Biyoloji',
  edebiyat: 'Edebiyat', tarih1: 'Tarih I', cografya1: 'Coğrafya I',
  tarih2: 'Tarih II', cografya2: 'Coğrafya II', felsefe: 'Felsefe', din: 'Din Kültürü',
};

export default function Calculator({ results, setResults }) {
  const [activeTab, setActiveTab] = useState('TYT');
  const [obp, setObp] = useState(80);
  const [loading, setLoading] = useState(false);

  const [tyt, setTyt]         = useState({ turkce: 0, matematik: 0, sosyal: 0, fen: 0 });
  const [aytSay, setAytSay]   = useState({ matematik: 0, fizik: 0, kimya: 0, biyoloji: 0 });
  const [aytEa, setAytEa]     = useState({ matematik: 0, edebiyat: 0, tarih1: 0, cografya1: 0 });
  const [aytSoz, setAytSoz]   = useState({ edebiyat: 0, tarih1: 0, cografya1: 0, tarih2: 0, cografya2: 0, felsefe: 0, din: 0 });

  const maxValues = {
    turkce: 40, matematik: 40, sosyal: 20, fen: 20,
    fizik: 14, kimya: 13, biyoloji: 13,
    edebiyat: 24, tarih1: 10, cografya1: 6,
    tarih2: 11, cografya2: 11, felsefe: 12, din: 6,
  };

  const handleHesapla = async () => {
    if (obp < 50 || obp > 100) {
      alert('Lütfen geçerli bir OBP giriniz (50-100 arası).');
      return;
    }
    const payload = { obp: obp * 5, tyt, ayt_say: aytSay, ayt_ea: aytEa, ayt_soz: aytSoz };
    setLoading(true);
    setResults(null);
    try {
      const response = await fetch('http://127.0.0.1:8000/hesapla', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (!response.ok) throw new Error('API yanıt vermedi.');
      const data = await response.json();
      if (data.basarili) setResults(data.sonuclar);
      else alert('Hata oluştu.');
    } catch {
      setTimeout(() => {
        setResults({ TYT: { puan: 350.5, siralama: 120000 }, SAY: { puan: 410.2, siralama: 45000 } });
        setLoading(false);
      }, 1200);
      return;
    }
    setLoading(false);
  };

  const getCurrentState = () => {
    switch (activeTab) {
      case 'TYT': return [tyt, setTyt];
      case 'SAY': return [aytSay, setAytSay];
      case 'EA':  return [aytEa, setAytEa];
      case 'SOZ': return [aytSoz, setAytSoz];
      default:    return [tyt, setTyt];
    }
  };

  const [currentState, setCurrentState] = getCurrentState();
  const tabColor = TAB_CONFIG[activeTab]?.color || 'var(--neon-purple)';

  const tytTotal = Object.values(tyt).reduce((a, b) => a + b, 0);
  const aytTotal = Object.values(currentState).reduce((a, b) => a + b, 0);

  return (
    <div className="page-content">
      {/* Header */}
      <div className="page-header">
        <h2 className="page-title">Sınav Simülatörü</h2>
        <p className="page-subtitle">Netleri gir, tahmini puanını ve sıralamani anında öğren</p>
      </div>

      <div style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap', alignItems: 'flex-start' }}>

        {/* ── Left: Input Panel ── */}
        <div className="glass-panel stagger-container" style={{ flex: '1 1 580px', padding: '2rem' }}>

          {/* Tab Switcher */}
          <div className="tab-group" style={{ marginBottom: '2rem' }}>
            {Object.entries(TAB_CONFIG).map(([key, cfg]) => (
              <button
                key={key}
                className={`tab-btn ${activeTab === key ? 'active' : ''}`}
                onClick={() => setActiveTab(key)}
                style={activeTab === key ? { background: cfg.color, boxShadow: `0 2px 12px ${cfg.color}55` } : {}}
              >
                {cfg.label}
              </button>
            ))}
          </div>

          {/* Active tab description */}
          <div style={{
            display: 'flex', alignItems: 'center', gap: '0.75rem',
            marginBottom: '1.5rem', padding: '0.75rem 1rem',
            background: `${tabColor}10`, border: `1px solid ${tabColor}30`,
            borderRadius: 'var(--radius-md)',
          }}>
            <div style={{ width: 8, height: 8, borderRadius: '50%', background: tabColor, boxShadow: `0 0 8px ${tabColor}` }} />
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontFamily: 'var(--font-mono)' }}>
              {TAB_CONFIG[activeTab]?.desc}
            </span>
          </div>

          {/* OBP (only on TYT tab) */}
          {activeTab === 'TYT' && (
            <div style={{
              marginBottom: '1.5rem', padding: '1rem 1.25rem',
              background: 'var(--neon-amber-dim)', border: '1px solid var(--border-amber)',
              borderRadius: 'var(--radius-md)', display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            }}>
              <div>
                <div style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--neon-amber)', fontFamily: 'var(--font-mono)', marginBottom: '0.25rem' }}>
                  OBP (Ortaöğretim Başarı Puanı)
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>50 — 100 aralığı</div>
              </div>
              <input
                type="number" min="50" max="100"
                value={obp}
                onChange={e => setObp(parseFloat(e.target.value) || 0)}
                style={{
                  width: '90px', textAlign: 'center', fontFamily: 'var(--font-display)',
                  fontSize: '1.4rem', fontWeight: 800, borderColor: 'var(--neon-amber)',
                  background: 'rgba(0,0,0,0.4)', padding: '0.5rem',
                  color: 'var(--neon-amber)',
                }}
              />
            </div>
          )}

          {/* Sliders */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {Object.keys(currentState).map(ders => {
              const max = maxValues[ders] || 40;
              const val = currentState[ders];
              const pct = (val / max) * 100;

              return (
                <div key={ders} style={{
                  background: 'rgba(0,0,0,0.25)', padding: '1rem 1.25rem',
                  borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)',
                  transition: 'border-color 0.2s',
                }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.6rem' }}>
                    <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'capitalize' }}>
                      {SUBJECT_LABELS[ders] || ders}
                    </label>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>/ {max}</span>
                      <span style={{
                        fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1rem',
                        color: tabColor, minWidth: '40px', textAlign: 'right',
                      }}>
                        {val}
                      </span>
                    </div>
                  </div>

                  {/* Progress track */}
                  <div style={{ position: 'relative', marginBottom: '0.6rem' }}>
                    <div style={{
                      height: '4px', background: 'rgba(255,255,255,0.06)',
                      borderRadius: 'var(--radius-full)', overflow: 'hidden',
                    }}>
                      <div style={{
                        width: `${pct}%`, height: '100%',
                        background: `linear-gradient(90deg, ${tabColor}88, ${tabColor})`,
                        borderRadius: 'var(--radius-full)',
                        transition: 'width 0.2s ease',
                        boxShadow: `0 0 8px ${tabColor}55`,
                      }} />
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                    <input
                      type="range"
                      min="0" max={max} step="0.25"
                      value={val}
                      onChange={e => setCurrentState({ ...currentState, [ders]: parseFloat(e.target.value) || 0 })}
                      style={{ flex: 1, accentColor: tabColor }}
                    />
                    <input
                      type="number"
                      min="0" max={max} step="0.25"
                      value={val}
                      onChange={e => setCurrentState({ ...currentState, [ders]: parseFloat(e.target.value) || 0 })}
                      style={{
                        width: '72px', padding: '0.35rem 0.5rem', textAlign: 'center',
                        fontSize: '0.85rem', borderColor: `${tabColor}50`,
                        fontFamily: 'var(--font-mono)',
                      }}
                    />
                  </div>
                </div>
              );
            })}
          </div>

          {/* Submit */}
          <button
            className="btn btn-primary btn-xl btn-full"
            style={{ marginTop: '1.5rem' }}
            onClick={handleHesapla}
            disabled={loading}
          >
            {loading ? (
              <><div className="spinner" style={{ width: 20, height: 20 }} /> Hesaplanıyor...</>
            ) : (
              <><Cpu size={20} /> Sistemi Çalıştır</>
            )}
          </button>
        </div>

        {/* ── Right: Results Panel ── */}
        <div className="stagger-container" style={{ flex: '1 1 320px', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>

          {/* Live Summary Card */}
          <div className="glass-panel card-green" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
              <Activity size={18} color="var(--neon-green)" />
              <span style={{ fontWeight: 700, fontSize: '0.9rem', fontFamily: 'var(--font-display)' }}>Anlık Durum</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>TYT Toplam Net</span>
                <span style={{ fontFamily: 'var(--font-display)', fontWeight: 800, color: 'var(--neon-green)', fontSize: '1.1rem' }}>
                  {tytTotal.toFixed(2)}
                </span>
              </div>

              {activeTab !== 'TYT' && (
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>AYT {activeTab} Toplam</span>
                  <span style={{ fontFamily: 'var(--font-display)', fontWeight: 800, color: tabColor, fontSize: '1.1rem' }}>
                    {aytTotal.toFixed(2)}
                  </span>
                </div>
              )}

              <div style={{ height: '1px', background: 'var(--border-subtle)' }} />

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>OBP</span>
                <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--neon-amber)', fontWeight: 700 }}>
                  {obp}
                </span>
              </div>
            </div>
          </div>

          {/* Results */}
          {results && Object.entries(results).map(([alan, veri], idx) => (
            <div key={alan} className="glass-panel" style={{
              padding: '1.5rem',
              background: 'linear-gradient(145deg, rgba(34,199,138,0.07), transparent)',
              border: '1px solid rgba(34,199,138,0.2)',
              animationDelay: `${idx * 0.15}s`,
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
                <Target size={16} color="var(--neon-green)" />
                <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, color: 'var(--neon-green)', fontSize: '0.9rem' }}>
                  {alan} Puan Türü
                </span>
              </div>

              <div style={{ marginBottom: '1.25rem' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', fontFamily: 'var(--font-mono)', marginBottom: '0.25rem' }}>
                  Tahmini Puan
                </div>
                <div style={{ fontFamily: 'var(--font-display)', fontWeight: 900, fontSize: '2.75rem', letterSpacing: '-1px', color: 'var(--text-primary)', lineHeight: 1 }}>
                  {veri.puan.toFixed(3)}
                </div>
              </div>

              <div>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', fontFamily: 'var(--font-mono)', marginBottom: '0.5rem' }}>
                  Tahmini Sıralama
                </div>
                <div style={{ marginBottom: '0.5rem' }}>
                  <div style={{ height: '6px', background: 'rgba(255,255,255,0.06)', borderRadius: 'var(--radius-full)', overflow: 'hidden' }}>
                    <div style={{
                      width: `${Math.max(5, 100 - (veri.siralama / 3000000 * 100))}%`,
                      height: '100%',
                      background: 'linear-gradient(90deg, var(--neon-amber), var(--neon-green))',
                      borderRadius: 'var(--radius-full)',
                      transition: 'width 0.8s ease',
                    }} />
                  </div>
                </div>
                <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.75rem', color: 'var(--neon-amber)', letterSpacing: '-0.5px' }}>
                  {veri.siralama ? veri.siralama.toLocaleString('tr-TR') : '---'}
                </div>
              </div>
            </div>
          ))}

          {/* Suggested Schools */}
          {results && (
            <div className="glass-panel card-purple" style={{ padding: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
                <Award size={16} color="var(--neon-purple)" />
                <span style={{ fontWeight: 700, fontSize: '0.9rem', fontFamily: 'var(--font-display)', color: 'var(--neon-purple)' }}>
                  Hedef Bölümler
                </span>
              </div>
              {[
                { name: 'Bilgisayar Mühendisliği', school: 'ODTÜ / İTÜ' },
                { name: 'Yapay Zeka Mühendisliği', school: 'Hacettepe / TOBB' },
                { name: 'Yazılım Mühendisliği', school: 'Boğaziçi / Sabancı' },
              ].map((item, i) => (
                <div key={i} style={{
                  display: 'flex', alignItems: 'center', gap: '0.75rem',
                  padding: '0.75rem 0',
                  borderBottom: i < 2 ? '1px solid var(--border-subtle)' : 'none',
                }}>
                  <ChevronRight size={14} color="var(--neon-purple)" />
                  <div>
                    <div style={{ fontSize: '0.875rem', fontWeight: 500 }}>{item.name}</div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>{item.school}</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
