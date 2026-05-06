import { useState, useEffect } from 'react';
import { Target, Activity, ChevronRight } from 'lucide-react';

export default function Calculator({ results, setResults }) {
  const [activeTab, setActiveTab] = useState('TYT');
  const [obp, setObp] = useState(80); // 50-100 scale
  const [loading, setLoading] = useState(false);

  const [tyt, setTyt] = useState({ turkce: 0, matematik: 0, sosyal: 0, fen: 0 });
  const [aytSay, setAytSay] = useState({ matematik: 0, fizik: 0, kimya: 0, biyoloji: 0 });
  const [aytEa, setAytEa] = useState({ matematik: 0, edebiyat: 0, tarih1: 0, cografya1: 0 });
  const [aytSoz, setAytSoz] = useState({ edebiyat: 0, tarih1: 0, cografya1: 0, tarih2: 0, cografya2: 0, felsefe: 0, din: 0 });

  const handleHesapla = async () => {
    if (obp < 50 || obp > 100) {
      alert("Lütfen geçerli bir OBP giriniz (50-100 arası).");
      return;
    }

    const payload = { obp: obp * 5, tyt, ayt_say: aytSay, ayt_ea: aytEa, ayt_soz: aytSoz };

    setLoading(true);
    setResults(null);

    try {
      const response = await fetch('http://127.0.0.1:8000/hesapla', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) throw new Error('API yanıt vermedi.');

      const data = await response.json();
      if (data.basarili) setResults(data.sonuclar);
      else alert("Hata oluştu.");
    } catch (error) {
      // Mocking results for preview if backend is off
      setTimeout(() => {
          setResults({
              'TYT': { puan: 350.5, siralama: 120000 },
              'SAY': { puan: 410.2, siralama: 45000 }
          });
          setLoading(false);
      }, 1000);
    } finally {
      // setLoading(false); handled in try/catch for mock
    }
  };

  const maxValues = {
      turkce: 40, matematik: 40, sosyal: 20, fen: 20,
      fizik: 14, kimya: 13, biyoloji: 13,
      edebiyat: 24, tarih1: 10, cografya1: 6,
      tarih2: 11, cografya2: 11, felsefe: 12, din: 6
  };

  const renderSliders = (state, setState) => {
    return Object.keys(state).map(ders => {
        const max = maxValues[ders] || 40;
        return (
            <div key={ders} style={{ marginBottom: '1.5rem', background: 'rgba(0,0,0,0.3)', padding: '1rem', borderRadius: '12px', border: '1px solid rgba(127,119,221,0.1)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <label style={{ textTransform: 'uppercase', fontSize: '0.8rem', color: 'var(--text-muted)' }}>{ders}</label>
                    <span style={{ fontFamily: 'var(--font-display)', color: 'var(--neon-purple)' }}>{state[ders]} / {max}</span>
                </div>
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    <input 
                        type="range" 
                        min="0" max={max} step="0.25"
                        value={state[ders]} 
                        onChange={(e) => setState({...state, [ders]: parseFloat(e.target.value) || 0})}
                        style={{ flex: 1, accentColor: 'var(--neon-purple)' }}
                    />
                    <input 
                        type="number" 
                        min="0" max={max} step="0.25"
                        value={state[ders]} 
                        onChange={(e) => setState({...state, [ders]: parseFloat(e.target.value) || 0})}
                        style={{ width: '80px', padding: '0.5rem', textAlign: 'center', background: 'rgba(0,0,0,0.5)', border: '1px solid var(--neon-purple)' }}
                    />
                </div>
            </div>
        );
    });
  };

  return (
    <div className="page-content" style={{ maxWidth: '1200px', margin: '0 auto', display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
      
      {/* Sol Taraf - Girdi Alanı */}
      <div className="glass-panel stagger-container" style={{ flex: '1 1 600px', padding: '2rem' }}>
        <h2 className="page-title" style={{ fontSize: '1.8rem', marginBottom: '2rem' }}>Sınav Simülatörü</h2>
        
        <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem', background: 'rgba(0,0,0,0.4)', padding: '0.5rem', borderRadius: '100px' }}>
            {['TYT', 'SAY', 'EA', 'SOZ'].map(tab => (
            <button 
                key={tab} 
                onClick={() => setActiveTab(tab)}
                style={{
                    flex: 1,
                    background: activeTab === tab ? 'var(--neon-purple)' : 'transparent',
                    color: activeTab === tab ? '#000' : 'var(--text-muted)',
                    border: 'none',
                    padding: '0.8rem',
                    borderRadius: '100px',
                    fontFamily: 'var(--font-display)',
                    fontWeight: 'bold',
                    cursor: 'pointer',
                    transition: 'all 0.3s ease'
                }}
            >
                {tab}
            </button>
            ))}
        </div>

        <div style={{ marginBottom: '2rem', borderLeft: '4px solid var(--neon-amber)', paddingLeft: '1rem' }}>
            <label style={{ display: 'block', color: 'var(--text-muted)', marginBottom: '0.5rem', fontSize: '0.8rem' }}>OBP (50-100)</label>
            <input 
                type="number" min="50" max="100" 
                value={obp} 
                onChange={e => setObp(parseFloat(e.target.value) || 0)} 
                style={{ width: '100px', fontSize: '1.2rem', textAlign: 'center', borderColor: 'var(--neon-amber)' }}
            />
        </div>

        <div className="stagger-container">
            {activeTab === 'TYT' && renderSliders(tyt, setTyt)}
            {activeTab === 'SAY' && renderSliders(aytSay, setAytSay)}
            {activeTab === 'EA' && renderSliders(aytEa, setAytEa)}
            {activeTab === 'SOZ' && renderSliders(aytSoz, setAytSoz)}
        </div>

        <button 
            className="btn-primary" 
            style={{ width: '100%', marginTop: '2rem', padding: '1.2rem', fontSize: '1.2rem' }}
            onClick={handleHesapla}
        >
            {loading ? 'HESAPLANIYOR...' : 'SİSTEMİ ÇALIŞTIR'}
        </button>
      </div>

      {/* Sağ Taraf - Sonuç / Özet Alanı */}
      <div className="stagger-container" style={{ flex: '1 1 350px', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        
        {/* Canlı Özet Sidebar */}
        <div className="glass-panel" style={{ borderTop: '4px solid var(--neon-green)', padding: '1.5rem' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', color: 'var(--text-main)' }}>
                <Activity size={20} className="text-neon-green" /> Anlık Durum
            </h3>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '0.5rem' }}>
                <span className="text-muted">TYT Toplam Net</span>
                <strong style={{ color: 'var(--neon-green)' }}>{(Object.values(tyt).reduce((a,b)=>a+b,0)).toFixed(2)}</strong>
            </div>
            {(activeTab !== 'TYT') && (
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '0.5rem' }}>
                    <span className="text-muted">AYT {activeTab} Toplam</span>
                    <strong style={{ color: 'var(--neon-green)' }}>
                        {(Object.values(activeTab === 'SAY' ? aytSay : activeTab === 'EA' ? aytEa : aytSoz).reduce((a,b)=>a+b,0)).toFixed(2)}
                    </strong>
                </div>
            )}
        </div>

        {/* Sonuçlar (Hesaplandıktan Sonra) */}
        {results && (
            <div className="stagger-container">
                {Object.entries(results).map(([alan, veri], index) => (
                    <div key={alan} className="glass-panel" style={{ 
                        marginBottom: '1rem', 
                        padding: '1.5rem',
                        background: 'linear-gradient(145deg, rgba(29,158,117,0.1), rgba(0,0,0,0))',
                        border: '1px solid rgba(29,158,117,0.3)',
                        animationDelay: `${index * 0.2}s`
                    }}>
                        <h4 style={{ color: 'var(--neon-green)', marginBottom: '1rem', fontFamily: 'var(--font-display)', fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <Target size={18} /> {alan} PUAN TÜRÜ
                        </h4>
                        
                        <div style={{ marginBottom: '1rem' }}>
                            <div className="text-muted" style={{ fontSize: '0.8rem', marginBottom: '0.2rem' }}>Tahmini Puan</div>
                            <div style={{ fontSize: '2.5rem', fontFamily: 'var(--font-display)', fontWeight: 'bold' }}>
                                {veri.puan.toFixed(3)}
                            </div>
                        </div>

                        <div>
                            <div className="text-muted" style={{ fontSize: '0.8rem', marginBottom: '0.5rem' }}>Tahmini Sıralama</div>
                            <div style={{ position: 'relative', width: '100%', height: '8px', background: 'rgba(255,255,255,0.1)', borderRadius: '4px', overflow: 'hidden', marginBottom: '0.5rem' }}>
                                <div style={{ width: `${Math.max(10, 100 - (veri.siralama / 3000000 * 100))}%`, height: '100%', background: 'linear-gradient(90deg, var(--neon-amber), var(--neon-green))' }}></div>
                            </div>
                            <div style={{ fontSize: '1.5rem', fontFamily: 'var(--font-display)', color: 'var(--neon-amber)' }}>
                                {veri.siralama ? veri.siralama.toLocaleString('tr-TR') : '---'}
                            </div>
                        </div>
                    </div>
                ))}

                {/* Okul Önerileri Mock */}
                <div className="glass-panel" style={{ border: '1px solid var(--neon-purple)', animationDelay: '0.6s' }}>
                    <h4 style={{ color: 'var(--neon-purple)', marginBottom: '1rem' }}>Tavsiye Edilen Hedefler</h4>
                    <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                        <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.8rem 0', borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                            <ChevronRight size={16} className="text-neon-purple" />
                            <div>
                                <div style={{ fontSize: '0.9rem' }}>Bilgisayar Mühendisliği</div>
                                <div className="text-muted" style={{ fontSize: '0.7rem' }}>Odtü / İtü Seviyesi</div>
                            </div>
                        </li>
                        <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.8rem 0' }}>
                            <ChevronRight size={16} className="text-neon-purple" />
                            <div>
                                <div style={{ fontSize: '0.9rem' }}>Yapay Zeka Mühendisliği</div>
                                <div className="text-muted" style={{ fontSize: '0.7rem' }}>Hacettepe / TOBB</div>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        )}
      </div>

    </div>
  );
}
