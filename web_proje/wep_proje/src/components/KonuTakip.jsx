import { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';
import { BookOpen, Calculator, FlaskConical, Globe2, BookA, Atom, Beaker, Check, Flame, X, MessageSquare, Calendar, Bot } from 'lucide-react';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, PieChart, Pie, Cell } from 'recharts';

const DERSLER = {
  TYT: [
    { id: 'tyt_mat', ad: 'Matematik', icon: Calculator, toplam: 25, kolay: 5, orta: 15, zor: 5 },
    { id: 'tyt_tur', ad: 'Türkçe', icon: BookA, toplam: 20, kolay: 4, orta: 12, zor: 4 },
    { id: 'tyt_fen', ad: 'Fen Bilimleri', icon: Atom, toplam: 15, kolay: 5, orta: 7, zor: 3 },
    { id: 'tyt_sos', ad: 'Sosyal Bilgiler', icon: Globe2, toplam: 15, kolay: 8, orta: 5, zor: 2 },
  ],
  AYT: [
    { id: 'ayt_mat', ad: 'Matematik', icon: Calculator, toplam: 30, kolay: 5, orta: 10, zor: 15 },
    { id: 'ayt_fiz', ad: 'Fizik', icon: Atom, toplam: 20, kolay: 3, orta: 7, zor: 10 },
    { id: 'ayt_kim', ad: 'Kimya', icon: FlaskConical, toplam: 15, kolay: 4, orta: 6, zor: 5 },
    { id: 'ayt_biy', ad: 'Biyoloji', icon: Beaker, toplam: 18, kolay: 5, orta: 8, zor: 5 },
    { id: 'ayt_edb', ad: 'Edebiyat', icon: BookOpen, toplam: 24, kolay: 6, orta: 12, zor: 6 },
  ]
};

const WEEKLY_DATA = [
  { name: 'Pzt', count: 3 }, { name: 'Sal', count: 5 }, { name: 'Çar', count: 2 },
  { name: 'Per', count: 6 }, { name: 'Cum', count: 4 }, { name: 'Cmt', count: 8 }, { name: 'Paz', count: 7 }
];

export default function KonuTakip({ session }) {
    const [activeTab, setActiveTab] = useState('TYT');
    const [expandedDers, setExpandedDers] = useState(null);
    const [konuDurumlari, setKonuDurumlari] = useState({}); // { "Matematik_Temel Kavramlar": true/false }
    const [konuNotlari, setKonuNotlari] = useState({}); // { "Matematik_Temel Kavramlar": "not metni" }
    const [userKonular, setUserKonular] = useState({}); // { "Matematik": [{id: "konu", ad: "konu"}] }
    const [yeniKonuInput, setYeniKonuInput] = useState('');
    const [filter, setFilter] = useState('Tümü');
    const [modalKonu, setModalKonu] = useState(null);
    const [modalNotText, setModalNotText] = useState("");
    const email = session?.user?.email;

    useEffect(() => {
        if (!email) return;
        const fetchKonular = async () => {
            const { data, error } = await supabase
                .from('konu_takip')
                .select('*')
                .eq('email', email);
            
            if (!error && data) {
                const mapDurum = {};
                const mapNot = {};
                const mapKonular = {};
                data.forEach(item => {
                    mapDurum[`${item.ders_adi}_${item.konu_adi}`] = item.durum === 'bitti';
                    if (item.notlar !== undefined && item.notlar !== null) {
                        mapNot[`${item.ders_adi}_${item.konu_adi}`] = item.notlar;
                    }
                    if (!mapKonular[item.ders_adi]) mapKonular[item.ders_adi] = [];
                    // Prevent duplicates just in case
                    if (!mapKonular[item.ders_adi].find(k => k.ad === item.konu_adi)) {
                        mapKonular[item.ders_adi].push({ id: item.konu_adi, ad: item.konu_adi });
                    }
                });
                setKonuDurumlari(mapDurum);
                setKonuNotlari(mapNot);
                setUserKonular(mapKonular);
            }
        };
        fetchKonular();
    }, [email]);

    const getDurum = (dersAdi, konuAdi) => konuDurumlari[`${dersAdi}_${konuAdi}`] || false;
    const getNot = (dersAdi, konuAdi) => konuNotlari[`${dersAdi}_${konuAdi}`] || "";
    
    const ekleYeniKonu = async (dersAdi) => {
        if (!yeniKonuInput.trim() || !email) return;
        
        const konuAdi = yeniKonuInput.trim();
        const key = `${dersAdi}_${konuAdi}`;
        
        // UI optimistik guncelleme
        setUserKonular(prev => {
            const list = prev[dersAdi] || [];
            if (list.find(k => k.ad === konuAdi)) return prev;
            return { ...prev, [dersAdi]: [...list, { id: konuAdi, ad: konuAdi }] };
        });
        setKonuDurumlari(prev => ({ ...prev, [key]: false }));
        setYeniKonuInput('');

        const payload = {
            email: email,
            ders_adi: dersAdi,
            konu_adi: konuAdi,
            durum: 'calisilacak'
        };

        const { error } = await supabase.from('konu_takip').upsert(payload, { onConflict: 'email,ders_adi,konu_adi' });
        if (error) console.error("Konu ekleme hatası:", error);
    };

    const toggleKonu = async (e, dersAdi, konuAdi) => {
        e.stopPropagation();
        const key = `${dersAdi}_${konuAdi}`;
        const mevcutDurum = konuDurumlari[key] || false;
        const yeniDurum = !mevcutDurum;

        // Optimistic update
        setKonuDurumlari(prev => ({ ...prev, [key]: yeniDurum }));

        if (email) {
            const payload = {
                email: email,
                ders_adi: dersAdi,
                konu_adi: konuAdi,
                durum: yeniDurum ? 'bitti' : 'calisilacak'
            };
            if (konuNotlari[key] !== undefined) {
                payload.notlar = konuNotlari[key];
            }
            
            const { error } = await supabase.from('konu_takip').upsert(payload, { onConflict: 'email,ders_adi,konu_adi' });
            if (error) {
                console.error("Güncelleme hatası:", error);
                setKonuDurumlari(prev => ({ ...prev, [key]: mevcutDurum })); // revert
            }
        }
    };

    const kaydetNot = async () => {
        if (!modalKonu) return;
        const key = `${modalKonu.ders}_${modalKonu.ad}`;
        const yeniNot = modalNotText;
        
        setKonuNotlari(prev => ({ ...prev, [key]: yeniNot }));
        
        if (email) {
            const payload = {
                email: email,
                ders_adi: modalKonu.ders,
                konu_adi: modalKonu.ad,
                durum: getDurum(modalKonu.ders, modalKonu.ad) ? 'bitti' : 'calisilacak',
                notlar: yeniNot
            };
            
            const { error } = await supabase.from('konu_takip').upsert(payload, { onConflict: 'email,ders_adi,konu_adi' });
            if (error) {
                alert("Not kaydedilemedi! Lütfen Supabase 'konu_takip' tablonuza 'notlar' adında (text tipinde) bir sütun eklediğinizden emin olun.");
                console.error("Not kayıt hatası:", error);
            } else {
                alert("Not başarıyla kaydedildi!");
            }
        }
    };

    const ProgressBar = ({ percent }) => {
        let color = percent < 40 ? 'var(--neon-danger)' : percent < 70 ? 'var(--neon-amber)' : 'var(--neon-green)';
        return (
            <div style={{ width: '100%', height: '4px', background: 'rgba(255,255,255,0.1)', borderRadius: '2px', marginTop: '0.5rem', overflow: 'hidden' }}>
                <div style={{ width: `${percent}%`, height: '100%', background: color, transition: 'width 0.5s ease' }}></div>
            </div>
        );
    };

    const ProgressRing = ({ percent, size = 40, stroke = 4 }) => {
        const radius = (size - stroke) / 2;
        const circ = radius * 2 * Math.PI;
        const offset = circ - (percent / 100) * circ;
        return (
            <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
                <circle stroke="rgba(255,255,255,0.1)" fill="transparent" strokeWidth={stroke} r={radius} cx={size/2} cy={size/2} />
                <circle 
                    stroke="var(--neon-purple)" fill="transparent" strokeWidth={stroke} r={radius} cx={size/2} cy={size/2} 
                    strokeDasharray={circ} strokeDashoffset={offset} strokeLinecap="round"
                    style={{ transition: 'stroke-dashoffset 0.5s ease' }}
                />
                <text x="50%" y="50%" fill="var(--text-main)" fontSize={size * 0.25} textAnchor="middle" dy=".3em" transform={`rotate(90 ${size/2} ${size/2})`} fontFamily="var(--font-display)">
                    {Math.round(percent)}%
                </text>
            </svg>
        );
    };

    const radarData = DERSLER[activeTab].map(d => ({
        subject: d.ad,
        A: Math.floor(Math.random() * 100) + 20, // Mock completed %
        fullMark: 100
    }));

    return (
        <div className="page-content" style={{ maxWidth: '1400px', margin: '0 auto', display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
            
            {/* Modal */}
            {modalKonu && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(5px)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', opacity: 1, transition: 'opacity 0.2s' }} onClick={() => setModalKonu(null)}>
                    <div className="glass-panel" style={{ width: '90%', maxWidth: '500px', transform: 'scale(1)', transition: 'transform 0.2s', padding: '2rem' }} onClick={e => e.stopPropagation()}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                            <div>
                                <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
                                    <span style={{ background: 'rgba(127,119,221,0.2)', color: 'var(--neon-purple)', padding: '0.2rem 0.5rem', borderRadius: '100px', fontSize: '0.7rem', border: '1px solid var(--neon-purple)' }}>{modalKonu.ders}</span>
                                    <span style={{ background: modalKonu.zorluk==='Zor'?'rgba(226,75,74,0.2)':modalKonu.zorluk==='Orta'?'rgba(239,159,39,0.2)':'rgba(29,158,117,0.2)', color: modalKonu.zorluk==='Zor'?'var(--neon-danger)':modalKonu.zorluk==='Orta'?'var(--neon-amber)':'var(--neon-green)', padding: '0.2rem 0.5rem', borderRadius: '100px', fontSize: '0.7rem' }}>{modalKonu.zorluk}</span>
                                </div>
                                <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.5rem' }}>{modalKonu.ad}</h3>
                            </div>
                            <button onClick={() => setModalKonu(null)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}><X size={24} /></button>
                        </div>
                        
                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>Durum</label>
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                <button className="btn-secondary" style={{ flex: 1, padding: '0.5rem', fontSize: '0.9rem', borderColor: !getDurum(modalKonu.id) ? 'var(--neon-danger)' : 'var(--text-muted)', color: !getDurum(modalKonu.id) ? 'var(--neon-danger)' : 'var(--text-muted)' }}>Öğrenmedim</button>
                                <button className="btn-secondary" style={{ flex: 1, padding: '0.5rem', fontSize: '0.9rem', borderColor: 'var(--text-muted)', color: 'var(--text-muted)' }}>Tekrar Lazım</button>
                                <button className="btn-secondary" style={{ flex: 1, padding: '0.5rem', fontSize: '0.9rem', borderColor: getDurum(modalKonu.id) ? 'var(--neon-green)' : 'var(--text-muted)', color: getDurum(modalKonu.id) ? 'var(--neon-green)' : 'var(--text-muted)' }}>Öğrendim</button>
                            </div>
                        </div>

                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}><MessageSquare size={16} /> Notlarım</label>
                            <textarea 
                                className="custom-textarea" 
                                placeholder="Bu konuyla ilgili notunu ekle..." 
                                rows={3} 
                                style={{ width: '100%', marginBottom: '0.5rem' }}
                                value={modalNotText}
                                onChange={e => setModalNotText(e.target.value)}
                            />
                            <button className="btn-primary" style={{ width: '100%', padding: '0.5rem' }} onClick={kaydetNot}>Notu Kaydet</button>
                        </div>

                        <div style={{ display: 'flex', gap: '1rem' }}>
                            <div style={{ flex: 1 }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}><Calendar size={16} /> Tekrar Tarihi</label>
                                <input type="date" style={{ width: '100%', padding: '0.5rem', background: 'rgba(0,0,0,0.5)', border: '1px solid var(--border-glow)', color: 'white', borderRadius: '8px' }} />
                            </div>
                            <div style={{ flex: 1, display: 'flex', alignItems: 'flex-end' }}>
                                <button className="btn-secondary" style={{ width: '100%', padding: '0.5rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', boxShadow: '0 0 10px rgba(127,119,221,0.2)' }}>
                                    <Bot size={18} /> AI Koç'a Sor
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Sol / Ana Alan */}
            <div className="stagger-container" style={{ flex: '1 1 700px', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                    <div>
                        <h2 className="page-title" style={{ fontSize: '1.8rem', margin: 0 }}>Gelişim Ağacı</h2>
                        <p className="text-muted" style={{ margin: 0 }}>&gt; Zayıf noktalarını tespit et, sistemi hackle.</p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span style={{ fontSize: '2rem', fontFamily: 'var(--font-display)', color: 'var(--neon-green)' }}>%42</span>
                        <span className="text-muted" style={{ fontSize: '0.8rem', textTransform: 'uppercase' }}>Genel<br/>İlerleme</span>
                    </div>
                </div>

                {/* Tabs */}
                <div style={{ display: 'flex', borderBottom: '1px solid var(--border-glow)' }}>
                    {['TYT', 'AYT'].map(tab => (
                        <button 
                            key={tab} 
                            onClick={() => { setActiveTab(tab); setExpandedDers(null); }}
                            style={{ 
                                background: 'transparent', border: 'none', color: activeTab === tab ? 'var(--neon-purple)' : 'var(--text-muted)', 
                                padding: '1rem 2rem', fontSize: '1.2rem', fontFamily: 'var(--font-display)', cursor: 'pointer',
                                borderBottom: activeTab === tab ? '2px solid var(--neon-purple)' : '2px solid transparent',
                                textShadow: activeTab === tab ? '0 0 10px rgba(127,119,221,0.5)' : 'none',
                                transition: 'all 0.3s'
                            }}
                        >
                            {tab}
                        </button>
                    ))}
                </div>

                {/* Accordion List */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {DERSLER[activeTab].map((ders, i) => {
                        const Icon = ders.icon;
                        const isExpanded = expandedDers === ders.id;
                        
                        const konular = userKonular[ders.ad] || [];
                        const toplamKonu = konular.length;
                        const tamamlanan = konular.filter(k => getDurum(ders.ad, k.ad)).length;
                        const percent = toplamKonu === 0 ? 0 : (tamamlanan / toplamKonu) * 100;

                        return (
                            <div key={ders.id} className="glass-panel" style={{ padding: '0', overflow: 'hidden', animationDelay: `${i*0.1}s` }}>
                                {/* Header */}
                                <div 
                                    style={{ padding: '1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', cursor: 'pointer', background: isExpanded ? 'rgba(127,119,221,0.05)' : 'transparent' }}
                                    onClick={() => setExpandedDers(isExpanded ? null : ders.id)}
                                >
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                        <div style={{ padding: '0.8rem', background: 'rgba(0,0,0,0.5)', borderRadius: '12px', border: '1px solid var(--border-glow)' }}>
                                            <Icon className="text-neon-purple" size={24} />
                                        </div>
                                        <div>
                                            <h3 style={{ margin: 0, fontFamily: 'var(--font-display)' }}>{ders.ad}</h3>
                                            <div className="text-muted" style={{ fontSize: '0.8rem' }}>{tamamlanan} / {toplamKonu} konu tamamlandı</div>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', width: '200px' }}>
                                        <div style={{ flex: 1 }}><ProgressBar percent={percent} /></div>
                                        <ProgressRing percent={percent} size={45} stroke={4} />
                                    </div>
                                </div>

                                {/* Body (Konular) */}
                                {isExpanded && (
                                    <div style={{ padding: '1.5rem', borderTop: '1px solid var(--border-glow)', background: 'rgba(0,0,0,0.2)' }}>
                                        
                                        {/* Yeni Konu Ekleme Alanı */}
                                        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem' }}>
                                            <input 
                                                type="text" 
                                                placeholder={`${ders.ad} için listene yeni bir konu ekle...`}
                                                className="custom-textarea"
                                                style={{ flex: 1, padding: '0.8rem 1rem', borderRadius: '8px', border: '1px solid var(--border-glow)' }}
                                                value={yeniKonuInput}
                                                onChange={e => setYeniKonuInput(e.target.value)}
                                                onKeyPress={e => e.key === 'Enter' && ekleYeniKonu(ders.ad)}
                                                onClick={e => e.stopPropagation()}
                                            />
                                            <button className="btn-primary" onClick={(e) => { e.stopPropagation(); ekleYeniKonu(ders.ad); }} style={{ padding: '0.8rem 1.5rem' }}>Ekle</button>
                                        </div>

                                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
                                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                                {['Tümü', 'Tamamlanmadı', 'Tamamlandı'].map(f => (
                                                    <button key={f} onClick={() => setFilter(f)} style={{ background: filter === f ? 'var(--neon-purple)' : 'transparent', color: filter === f ? '#000' : 'var(--text-muted)', border: '1px solid var(--neon-purple)', padding: '0.3rem 0.8rem', borderRadius: '100px', fontSize: '0.8rem', cursor: 'pointer', transition: 'all 0.2s' }}>{f}</button>
                                                ))}
                                            </div>
                                            <button className="btn-secondary" style={{ padding: '0.3rem 0.8rem', fontSize: '0.8rem' }}>Tümünü İşaretle</button>
                                        </div>

                                        {konular.length === 0 ? (
                                            <div className="text-muted" style={{ textAlign: 'center', padding: '1rem', fontStyle: 'italic', fontSize: '0.9rem' }}>
                                                Henüz bu derse ait hiçbir konu eklemedin. Yukarıdan bir tane ekleyerek başla!
                                            </div>
                                        ) : (
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                                {konular.map(konu => {
                                                    const checked = getDurum(ders.ad, konu.ad);
                                                    // Filter logic
                                                    if (filter === 'Tamamlandı' && !checked) return null;
                                                    if (filter === 'Tamamlanmadı' && checked) return null;

                                                    return (
                                                    <div 
                                                        key={konu.id} 
                                                        onClick={() => {
                                                            setModalKonu({...konu, ders: ders.ad});
                                                            setModalNotText(getNot(ders.ad, konu.ad));
                                                        }}
                                                        style={{ 
                                                            display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '1rem', 
                                                            background: checked ? 'rgba(29, 158, 117, 0.05)' : 'rgba(255,255,255,0.02)', 
                                                            border: '1px solid var(--border-glow)', borderRadius: '8px', cursor: 'pointer',
                                                            transition: 'all 0.3s'
                                                        }}
                                                    >
                                                        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                                            <div 
                                                                onClick={(e) => toggleKonu(e, konu.id)}
                                                                style={{ width: '24px', height: '24px', borderRadius: '6px', border: `2px solid ${checked ? 'var(--neon-purple)' : 'var(--text-muted)'}`, background: checked ? 'var(--neon-purple)' : 'transparent', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', transition: 'all 0.2s' }}
                                                            >
                                                                {checked && <Check size={16} color="#000" />}
                                                            </div>
                                                            <span style={{ fontFamily: 'var(--font-body)', textDecoration: checked ? 'line-through' : 'none', opacity: checked ? 0.5 : 1 }}>{konu.ad}</span>
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                        )}
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>

                {/* Charts Area */}
                <h3 style={{ fontFamily: 'var(--font-display)', marginTop: '2rem', borderBottom: '1px solid var(--border-glow)', paddingBottom: '0.5rem' }}>İlerleme Analizi</h3>
                <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                    <div className="glass-panel" style={{ flex: '1 1 300px', height: '300px', padding: '1rem', display: 'flex', flexDirection: 'column' }}>
                        <h4 className="text-center text-muted" style={{ marginBottom: '1rem' }}>Alan Dağılımı (Radar)</h4>
                        <ResponsiveContainer width="100%" height="100%">
                            <RadarChart outerRadius="70%" data={radarData}>
                                <PolarGrid stroke="rgba(255,255,255,0.1)" />
                                <PolarAngleAxis dataKey="subject" tick={{ fill: 'rgba(255,255,255,0.4)', fontSize: 10 }} />
                                <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
                                <Radar name="Tamamlanan" dataKey="A" stroke="var(--neon-purple)" fill="var(--neon-purple)" fillOpacity={0.3} />
                                <Tooltip contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-glow)', borderRadius: '8px' }} />
                            </RadarChart>
                        </ResponsiveContainer>
                    </div>
                    
                    <div className="glass-panel" style={{ flex: '1 1 300px', height: '300px', padding: '1rem', display: 'flex', flexDirection: 'column' }}>
                        <h4 className="text-center text-muted" style={{ marginBottom: '1rem' }}>Haftalık Aktivite (Son 7 Gün)</h4>
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={WEEKLY_DATA}>
                                <XAxis dataKey="name" tick={{ fill: 'rgba(255,255,255,0.4)', fontSize: 12 }} axisLine={false} tickLine={false} />
                                <Tooltip cursor={{fill: 'rgba(255,255,255,0.05)'}} contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--neon-green)', borderRadius: '8px' }} />
                                <Bar dataKey="count" fill="url(#colorGreen)" radius={[4,4,0,0]} />
                                <defs>
                                    <linearGradient id="colorGreen" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="var(--neon-green)" stopOpacity={0.8}/>
                                    <stop offset="95%" stopColor="var(--neon-purple)" stopOpacity={0.8}/>
                                    </linearGradient>
                                </defs>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>

                    <div className="glass-panel" style={{ flex: '1 1 250px', height: '300px', padding: '1rem', display: 'flex', flexDirection: 'column', position: 'relative' }}>
                        <h4 className="text-center text-muted" style={{ marginBottom: '1rem' }}>Genel TYT/AYT</h4>
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie data={[{name: 'Tamamlandı', value: 42}, {name: 'Kalan', value: 58}]} innerRadius={60} outerRadius={80} dataKey="value" stroke="none">
                                    <Cell fill="var(--neon-green)" />
                                    <Cell fill="#222" />
                                </Pie>
                                <Tooltip contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-glow)' }} />
                            </PieChart>
                        </ResponsiveContainer>
                        <div style={{ position: 'absolute', top: '55%', left: '50%', transform: 'translate(-50%, -50%)', textAlign: 'center' }}>
                            <div style={{ fontSize: '2rem', fontFamily: 'var(--font-display)', color: 'var(--text-main)' }}>%42</div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Sağ Taraf - Sticky Panel */}
            <div className="stagger-container" style={{ flex: '0 0 300px', display: 'flex', flexDirection: 'column', gap: '1.5rem', position: 'sticky', top: '2rem', alignSelf: 'flex-start' }}>
                <div className="glass-panel text-center" style={{ padding: '2rem 1.5rem' }}>
                    <h3 style={{ fontSize: '1rem', color: 'var(--text-muted)', marginBottom: '1.5rem', textTransform: 'uppercase' }}>Genel İlerleme</h3>
                    <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '2rem' }}>
                        <svg width="150" height="150" style={{ transform: 'rotate(-90deg)' }}>
                            <defs>
                                <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
                                    <stop offset="0%" style={{ stopColor: 'var(--neon-purple)', stopOpacity: 1 }} />
                                    <stop offset="100%" style={{ stopColor: 'var(--neon-green)', stopOpacity: 1 }} />
                                </linearGradient>
                            </defs>
                            <circle stroke="rgba(255,255,255,0.05)" fill="transparent" strokeWidth="12" r="65" cx="75" cy="75" />
                            <circle stroke="url(#grad1)" fill="transparent" strokeWidth="12" strokeLinecap="round" r="65" cx="75" cy="75" strokeDasharray="408" strokeDashoffset="236" />
                            <text x="50%" y="50%" fill="var(--text-main)" fontSize="28" fontWeight="bold" textAnchor="middle" dy=".3em" transform="rotate(90 75 75)" fontFamily="var(--font-display)">
                                42%
                            </text>
                        </svg>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', textAlign: 'left' }}>
                        <div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '0.3rem' }}><span>Matematik</span> <span className="text-neon-purple">%60</span></div>
                            <ProgressBar percent={60} />
                        </div>
                        <div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '0.3rem' }}><span>Fizik</span> <span className="text-neon-amber">%35</span></div>
                            <ProgressBar percent={35} />
                        </div>
                        <div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '0.3rem' }}><span>Biyoloji</span> <span className="text-neon-danger">%15</span></div>
                            <ProgressBar percent={15} />
                        </div>
                    </div>
                </div>

                <div className="glass-panel" style={{ padding: '1.5rem', borderLeft: '4px solid var(--neon-danger)' }}>
                    <h3 style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '1rem', textTransform: 'uppercase' }}>Kritik / Zayıf Dersler</h3>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                        <span style={{ background: 'rgba(226,75,74,0.1)', color: 'var(--neon-danger)', padding: '0.3rem 0.8rem', borderRadius: '100px', fontSize: '0.8rem', border: '1px solid var(--neon-danger)' }}>Biyoloji</span>
                        <span style={{ background: 'rgba(226,75,74,0.1)', color: 'var(--neon-danger)', padding: '0.3rem 0.8rem', borderRadius: '100px', fontSize: '0.8rem', border: '1px solid var(--neon-danger)' }}>Tarih</span>
                        <span style={{ background: 'rgba(226,75,74,0.1)', color: 'var(--neon-danger)', padding: '0.3rem 0.8rem', borderRadius: '100px', fontSize: '0.8rem', border: '1px solid var(--neon-danger)' }}>Geometri</span>
                    </div>
                </div>

                <div className="glass-panel" style={{ padding: '1.5rem', border: '1px solid var(--neon-amber)', background: 'linear-gradient(180deg, rgba(239,159,39,0.05), transparent)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                        <h3 style={{ fontSize: '0.9rem', color: 'var(--neon-amber)', textTransform: 'uppercase' }}>Bugünkü Hedef</h3>
                        <Flame className="text-neon-amber" size={24} />
                    </div>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.5rem' }}>
                        <strong style={{ fontSize: '2.5rem', fontFamily: 'var(--font-display)' }}>3</strong>
                        <span className="text-muted">/ 5 Konu</span>
                    </div>
                    <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>Seriyi bozmamak için 2 konu daha işaretle!</p>
                </div>
            </div>

        </div>
    );
}
