import { useState, useEffect } from 'react';
import { supabase } from '../supabaseClient';
import { BookOpen, Calculator, FlaskConical, Globe2, BookA, Atom, Beaker, Check, Flame, X, MessageSquare, Calendar, Bot, Plus, Filter } from 'lucide-react';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, BarChart, Bar, XAxis, Tooltip, PieChart, Pie, Cell } from 'recharts';

const DERSLER = {
  TYT: [
    { id: 'tyt_mat', ad: 'Matematik',     icon: Calculator,    toplam: 25 },
    { id: 'tyt_tur', ad: 'Türkçe',        icon: BookA,         toplam: 20 },
    { id: 'tyt_fen', ad: 'Fen Bilimleri', icon: Atom,          toplam: 15 },
    { id: 'tyt_sos', ad: 'Sosyal Bilgiler', icon: Globe2,      toplam: 15 },
  ],
  AYT: [
    { id: 'ayt_mat', ad: 'Matematik',     icon: Calculator,    toplam: 30 },
    { id: 'ayt_fiz', ad: 'Fizik',         icon: Atom,          toplam: 20 },
    { id: 'ayt_kim', ad: 'Kimya',         icon: FlaskConical,  toplam: 15 },
    { id: 'ayt_biy', ad: 'Biyoloji',      icon: Beaker,        toplam: 18 },
    { id: 'ayt_edb', ad: 'Edebiyat',      icon: BookOpen,      toplam: 24 },
  ],
};

const WEEKLY_DATA = [
  { name: 'Pzt', count: 3 }, { name: 'Sal', count: 5 }, { name: 'Çar', count: 2 },
  { name: 'Per', count: 6 }, { name: 'Cum', count: 4 }, { name: 'Cmt', count: 8 }, { name: 'Paz', count: 7 },
];

const ProgressBar = ({ percent }) => {
  const color = percent < 40 ? 'var(--neon-danger)' : percent < 70 ? 'var(--neon-amber)' : 'var(--neon-green)';
  return (
    <div className="progress-bar-track">
      <div style={{ width: `${percent}%`, height: '100%', background: color, borderRadius: 'var(--radius-full)', transition: 'width 0.5s ease' }} />
    </div>
  );
};

const ProgressRing = ({ percent, size = 44, stroke = 4 }) => {
  const radius = (size - stroke) / 2;
  const circ   = radius * 2 * Math.PI;
  const offset = circ - (percent / 100) * circ;
  return (
    <svg width={size} height={size} style={{ transform: 'rotate(-90deg)', flexShrink: 0 }}>
      <circle stroke="rgba(255,255,255,0.08)" fill="transparent" strokeWidth={stroke} r={radius} cx={size / 2} cy={size / 2} />
      <circle
        stroke="var(--neon-purple)" fill="transparent" strokeWidth={stroke}
        r={radius} cx={size / 2} cy={size / 2}
        strokeDasharray={circ} strokeDashoffset={offset} strokeLinecap="round"
        style={{ transition: 'stroke-dashoffset 0.5s ease' }}
      />
      <text x="50%" y="50%" fill="var(--text-primary)" fontSize={size * 0.22} textAnchor="middle"
        dy=".3em" transform={`rotate(90 ${size / 2} ${size / 2})`} fontFamily="var(--font-display)" fontWeight="700">
        {Math.round(percent)}%
      </text>
    </svg>
  );
};

export default function KonuTakip({ session }) {
  const [activeTab, setActiveTab]       = useState('TYT');
  const [expandedDers, setExpandedDers] = useState(null);
  const [konuDurumlari, setKonuDurumlari] = useState({});
  const [konuNotlari, setKonuNotlari]   = useState({});
  const [userKonular, setUserKonular]   = useState({});
  const [yeniKonuInput, setYeniKonuInput] = useState('');
  const [filter, setFilter]             = useState('Tümü');
  const [modalKonu, setModalKonu]       = useState(null);
  const [modalNotText, setModalNotText] = useState('');
  const email = session?.user?.email;

  useEffect(() => {
    if (!email) return;
    const fetchKonular = async () => {
      const { data, error } = await supabase.from('konu_takip').select('*').eq('email', email);
      if (!error && data) {
        const mapDurum = {}, mapNot = {}, mapKonular = {};
        data.forEach(item => {
          mapDurum[`${item.ders_adi}_${item.konu_adi}`] = item.durum === 'bitti';
          if (item.notlar != null) mapNot[`${item.ders_adi}_${item.konu_adi}`] = item.notlar;
          if (!mapKonular[item.ders_adi]) mapKonular[item.ders_adi] = [];
          if (!mapKonular[item.ders_adi].find(k => k.ad === item.konu_adi))
            mapKonular[item.ders_adi].push({ id: item.konu_adi, ad: item.konu_adi });
        });
        setKonuDurumlari(mapDurum);
        setKonuNotlari(mapNot);
        setUserKonular(mapKonular);
      }
    };
    fetchKonular();
  }, [email]);

  const getDurum = (dersAdi, konuAdi) => konuDurumlari[`${dersAdi}_${konuAdi}`] || false;
  const getNot   = (dersAdi, konuAdi) => konuNotlari[`${dersAdi}_${konuAdi}`] || '';

  const ekleYeniKonu = async (dersAdi) => {
    if (!yeniKonuInput.trim() || !email) return;
    const konuAdi = yeniKonuInput.trim();
    const key = `${dersAdi}_${konuAdi}`;
    setUserKonular(prev => {
      const list = prev[dersAdi] || [];
      if (list.find(k => k.ad === konuAdi)) return prev;
      return { ...prev, [dersAdi]: [...list, { id: konuAdi, ad: konuAdi }] };
    });
    setKonuDurumlari(prev => ({ ...prev, [key]: false }));
    setYeniKonuInput('');
    await supabase.from('konu_takip').upsert(
      { email, ders_adi: dersAdi, konu_adi: konuAdi, durum: 'calisilacak' },
      { onConflict: 'email,ders_adi,konu_adi' }
    );
  };

  const toggleKonu = async (e, dersAdi, konuAdi) => {
    e.stopPropagation();
    const key = `${dersAdi}_${konuAdi}`;
    const mevcutDurum = konuDurumlari[key] || false;
    const yeniDurum = !mevcutDurum;
    setKonuDurumlari(prev => ({ ...prev, [key]: yeniDurum }));
    if (email) {
      const payload = { email, ders_adi: dersAdi, konu_adi: konuAdi, durum: yeniDurum ? 'bitti' : 'calisilacak' };
      if (konuNotlari[key] !== undefined) payload.notlar = konuNotlari[key];
      const { error } = await supabase.from('konu_takip').upsert(payload, { onConflict: 'email,ders_adi,konu_adi' });
      if (error) setKonuDurumlari(prev => ({ ...prev, [key]: mevcutDurum }));
    }
  };

  const kaydetNot = async () => {
    if (!modalKonu) return;
    const key = `${modalKonu.ders}_${modalKonu.ad}`;
    setKonuNotlari(prev => ({ ...prev, [key]: modalNotText }));
    if (email) {
      const payload = {
        email, ders_adi: modalKonu.ders, konu_adi: modalKonu.ad,
        durum: getDurum(modalKonu.ders, modalKonu.ad) ? 'bitti' : 'calisilacak',
        notlar: modalNotText,
      };
      const { error } = await supabase.from('konu_takip').upsert(payload, { onConflict: 'email,ders_adi,konu_adi' });
      if (error) {
        alert("Not kaydedilemedi! 'konu_takip' tablosuna 'notlar' sütunu eklenmeli.");
      } else {
        setModalKonu(null);
      }
    }
  };

  const radarData = DERSLER[activeTab].map(d => ({
    subject: d.ad,
    A: Math.floor(Math.random() * 80) + 20,
    fullMark: 100,
  }));

  return (
    <div className="page-content">

      {/* Modal */}
      {modalKonu && (
        <div className="modal-overlay" onClick={() => setModalKonu(null)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            {/* Modal Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
              <div>
                <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
                  <span className="badge badge-purple">{modalKonu.ders}</span>
                  {getDurum(modalKonu.ders, modalKonu.ad)
                    ? <span className="badge badge-green">Tamamlandı</span>
                    : <span className="badge badge-amber">Devam Ediyor</span>}
                </div>
                <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.35rem', fontWeight: 800 }}>
                  {modalKonu.ad}
                </h3>
              </div>
              <button className="btn-icon" onClick={() => setModalKonu(null)}>
                <X size={18} />
              </button>
            </div>

            {/* Durum */}
            <div className="form-group">
              <label className="label">Durum</label>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                {[
                  { label: 'Öğrenmedim', color: 'danger' },
                  { label: 'Tekrar Lazım', color: 'amber' },
                  { label: 'Öğrendim', color: 'green' },
                ].map(({ label, color }) => (
                  <button key={label} className={`btn btn-sm`} style={{
                    flex: 1,
                    background: 'transparent',
                    border: `1px solid rgba(255,255,255,0.1)`,
                    color: 'var(--text-muted)',
                    borderRadius: 'var(--radius-md)',
                  }}>
                    {label}
                  </button>
                ))}
              </div>
            </div>

            {/* Not */}
            <div className="form-group">
              <label className="label"><MessageSquare size={13} style={{ display: 'inline', marginRight: '0.3rem' }} />Notlarım</label>
              <textarea
                placeholder="Bu konuyla ilgili notunu ekle..."
                rows={3}
                value={modalNotText}
                onChange={e => setModalNotText(e.target.value)}
                style={{ width: '100%' }}
              />
            </div>

            {/* Tekrar Tarihi + AI */}
            <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.25rem' }}>
              <div style={{ flex: 1 }}>
                <label className="label"><Calendar size={13} style={{ display: 'inline', marginRight: '0.3rem' }} />Tekrar Tarihi</label>
                <input type="date" style={{ width: '100%' }} />
              </div>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
                <button className="btn btn-secondary btn-full" style={{ gap: '0.4rem' }}>
                  <Bot size={15} /> AI Koç'a Sor
                </button>
              </div>
            </div>

            <button className="btn btn-primary btn-full" onClick={kaydetNot}>
              Notu Kaydet
            </button>
          </div>
        </div>
      )}

      {/* Page Header */}
      <div className="page-header">
        <h2 className="page-title">Gelişim Ağacı</h2>
        <p className="page-subtitle">Zayıf noktalarını tespit et, sistemi hackle.</p>
      </div>

      <div style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap', alignItems: 'flex-start' }}>

        {/* ── Main Column ── */}
        <div className="stagger-container" style={{ flex: '1 1 640px', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>

          {/* Tabs */}
          <div className="tab-group-line">
            {['TYT', 'AYT'].map(tab => (
              <button
                key={tab}
                className={`tab-btn-line ${activeTab === tab ? 'active' : ''}`}
                onClick={() => { setActiveTab(tab); setExpandedDers(null); }}
              >
                {tab}
              </button>
            ))}
          </div>

          {/* Accordion */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {DERSLER[activeTab].map((ders, i) => {
              const Icon = ders.icon;
              const isExpanded = expandedDers === ders.id;
              const konular     = userKonular[ders.ad] || [];
              const tamamlanan  = konular.filter(k => getDurum(ders.ad, k.ad)).length;
              const percent     = konular.length === 0 ? 0 : (tamamlanan / konular.length) * 100;

              return (
                <div key={ders.id} className="glass-panel" style={{ padding: 0, overflow: 'hidden', animationDelay: `${i * 0.07}s` }}>
                  {/* Accordion Header */}
                  <div
                    style={{
                      padding: '1.125rem 1.5rem',
                      display: 'flex', alignItems: 'center',
                      justifyContent: 'space-between', cursor: 'pointer',
                      background: isExpanded ? 'rgba(139,127,232,0.04)' : 'transparent',
                      transition: 'background 0.2s',
                      gap: '1rem',
                    }}
                    onClick={() => setExpandedDers(isExpanded ? null : ders.id)}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', minWidth: 0 }}>
                      <div style={{
                        width: 40, height: 40, borderRadius: 'var(--radius-md)', flexShrink: 0,
                        background: 'rgba(0,0,0,0.4)', border: '1px solid var(--border-subtle)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                      }}>
                        <Icon size={20} color="var(--neon-purple)" />
                      </div>
                      <div style={{ minWidth: 0 }}>
                        <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '0.95rem', marginBottom: '0.1rem' }}>
                          {ders.ad}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          {tamamlanan} / {konular.length} konu tamamlandı
                        </div>
                      </div>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flexShrink: 0 }}>
                      <div style={{ width: '120px' }}>
                        <ProgressBar percent={percent} />
                      </div>
                      <ProgressRing percent={percent} size={44} stroke={4} />
                    </div>
                  </div>

                  {/* Accordion Body */}
                  {isExpanded && (
                    <div style={{ padding: '1.25rem 1.5rem', borderTop: '1px solid var(--border-subtle)', background: 'rgba(0,0,0,0.15)' }}>

                      {/* Add new topic */}
                      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem' }}>
                        <div className="input-wrapper" style={{ flex: 1 }}>
                          <span className="input-icon"><Plus size={15} /></span>
                          <input
                            type="text"
                            placeholder={`${ders.ad} için yeni konu ekle...`}
                            value={yeniKonuInput}
                            onChange={e => setYeniKonuInput(e.target.value)}
                            onKeyDown={e => e.key === 'Enter' && ekleYeniKonu(ders.ad)}
                            onClick={e => e.stopPropagation()}
                          />
                        </div>
                        <button
                          className="btn btn-primary btn-sm"
                          onClick={e => { e.stopPropagation(); ekleYeniKonu(ders.ad); }}
                        >
                          Ekle
                        </button>
                      </div>

                      {/* Filter pills */}
                      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
                        <Filter size={14} color="var(--text-muted)" />
                        {['Tümü', 'Tamamlanmadı', 'Tamamlandı'].map(f => (
                          <button key={f} onClick={() => setFilter(f)} className={`badge ${filter === f ? 'badge-purple' : ''}`} style={{
                            cursor: 'pointer', border: `1px solid ${filter === f ? 'rgba(139,127,232,0.3)' : 'var(--border-subtle)'}`,
                            background: filter === f ? 'var(--neon-purple-dim)' : 'transparent',
                            color: filter === f ? 'var(--neon-purple)' : 'var(--text-muted)',
                            transition: 'all 0.2s',
                          }}>
                            {f}
                          </button>
                        ))}
                      </div>

                      {/* Topic list */}
                      {konular.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                          Henüz hiç konu eklenmedi. Yukarıdan ekleyerek başla!
                        </div>
                      ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                          {konular.map(konu => {
                            const checked = getDurum(ders.ad, konu.ad);
                            if (filter === 'Tamamlandı' && !checked) return null;
                            if (filter === 'Tamamlanmadı' && checked) return null;
                            return (
                              <div
                                key={konu.id}
                                onClick={() => { setModalKonu({ ...konu, ders: ders.ad }); setModalNotText(getNot(ders.ad, konu.ad)); }}
                                style={{
                                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                                  padding: '0.75rem 1rem',
                                  background: checked ? 'rgba(34,199,138,0.04)' : 'rgba(255,255,255,0.02)',
                                  border: `1px solid ${checked ? 'rgba(34,199,138,0.15)' : 'var(--border-subtle)'}`,
                                  borderRadius: 'var(--radius-md)', cursor: 'pointer',
                                  transition: 'all 0.2s',
                                }}
                              >
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.875rem' }}>
                                  <div
                                    onClick={e => toggleKonu(e, ders.ad, konu.ad)}
                                    style={{
                                      width: 22, height: 22, borderRadius: '6px', flexShrink: 0,
                                      border: `2px solid ${checked ? 'var(--neon-green)' : 'var(--text-muted)'}`,
                                      background: checked ? 'var(--neon-green)' : 'transparent',
                                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                                      transition: 'all 0.2s', cursor: 'pointer',
                                    }}
                                  >
                                    {checked && <Check size={13} color="#000" strokeWidth={3} />}
                                  </div>
                                  <span style={{
                                    fontSize: '0.875rem', fontWeight: 500,
                                    textDecoration: checked ? 'line-through' : 'none',
                                    opacity: checked ? 0.5 : 1,
                                    transition: 'all 0.2s',
                                  }}>
                                    {konu.ad}
                                  </span>
                                </div>
                                {getNot(ders.ad, konu.ad) && (
                                  <MessageSquare size={13} color="var(--text-muted)" />
                                )}
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

          {/* Charts */}
          <div style={{ marginTop: '1rem' }}>
            <h3 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: '1rem', fontSize: '1.05rem' }}>
              İlerleme Analizi
            </h3>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              {/* Radar */}
              <div className="glass-panel" style={{ flex: '1 1 280px', height: 280, padding: '1.25rem', display: 'flex', flexDirection: 'column' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.06em', fontFamily: 'var(--font-mono)' }}>
                  Alan Dağılımı
                </div>
                <ResponsiveContainer width="100%" height="100%">
                  <RadarChart outerRadius="70%" data={radarData}>
                    <PolarGrid stroke="rgba(255,255,255,0.07)" />
                    <PolarAngleAxis dataKey="subject" tick={{ fill: 'rgba(255,255,255,0.35)', fontSize: 11 }} />
                    <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
                    <Radar name="Tamamlanan" dataKey="A" stroke="var(--neon-purple)" fill="var(--neon-purple)" fillOpacity={0.2} />
                    <Tooltip contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-glow)', borderRadius: '8px', fontSize: '0.8rem' }} />
                  </RadarChart>
                </ResponsiveContainer>
              </div>

              {/* Bar */}
              <div className="glass-panel" style={{ flex: '1 1 280px', height: 280, padding: '1.25rem', display: 'flex', flexDirection: 'column' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.06em', fontFamily: 'var(--font-mono)' }}>
                  Haftalık Aktivite
                </div>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={WEEKLY_DATA}>
                    <defs>
                      <linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%"  stopColor="var(--neon-green)"  stopOpacity={0.9} />
                        <stop offset="95%" stopColor="var(--neon-purple)" stopOpacity={0.7} />
                      </linearGradient>
                    </defs>
                    <XAxis dataKey="name" tick={{ fill: 'rgba(255,255,255,0.35)', fontSize: 11 }} axisLine={false} tickLine={false} />
                    <Tooltip cursor={{ fill: 'rgba(255,255,255,0.04)' }} contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-glow)', borderRadius: '8px', fontSize: '0.8rem' }} />
                    <Bar dataKey="count" fill="url(#barGrad)" radius={[5, 5, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              {/* Donut */}
              <div className="glass-panel" style={{ flex: '1 1 200px', height: 280, padding: '1.25rem', display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.06em', fontFamily: 'var(--font-mono)', alignSelf: 'flex-start' }}>
                  Genel İlerleme
                </div>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie data={[{ value: 42 }, { value: 58 }]} innerRadius={55} outerRadius={75} dataKey="value" stroke="none" startAngle={90} endAngle={-270}>
                      <Cell fill="var(--neon-green)" />
                      <Cell fill="rgba(255,255,255,0.05)" />
                    </Pie>
                    <Tooltip contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-glow)', fontSize: '0.8rem', borderRadius: '8px' }} />
                  </PieChart>
                </ResponsiveContainer>
                <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -10%)', textAlign: 'center' }}>
                  <div style={{ fontFamily: 'var(--font-display)', fontWeight: 900, fontSize: '1.75rem', lineHeight: 1 }}>%42</div>
                  <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginTop: '0.2rem' }}>tamamlandı</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ── Right Sidebar ── */}
        <div className="stagger-container" style={{ flex: '0 0 280px', display: 'flex', flexDirection: 'column', gap: '1rem', position: 'sticky', top: '2rem', alignSelf: 'flex-start' }}>

          {/* Genel İlerleme Ring */}
          <div className="glass-panel" style={{ padding: '1.5rem', textAlign: 'center' }}>
            <div style={{ fontSize: '0.7rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.1em', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginBottom: '1.25rem' }}>
              Genel İlerleme
            </div>
            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.5rem' }}>
              <svg width="140" height="140" style={{ transform: 'rotate(-90deg)' }}>
                <defs>
                  <linearGradient id="ringGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style={{ stopColor: 'var(--neon-purple)', stopOpacity: 1 }} />
                    <stop offset="100%" style={{ stopColor: 'var(--neon-green)', stopOpacity: 1 }} />
                  </linearGradient>
                </defs>
                <circle stroke="rgba(255,255,255,0.05)" fill="transparent" strokeWidth="10" r="60" cx="70" cy="70" />
                <circle stroke="url(#ringGrad)" fill="transparent" strokeWidth="10" strokeLinecap="round"
                  r="60" cx="70" cy="70" strokeDasharray="377" strokeDashoffset="218" />
                <text x="50%" y="50%" fill="var(--text-primary)" fontSize="24" fontWeight="900"
                  textAnchor="middle" dy=".3em" transform="rotate(90 70 70)" fontFamily="var(--font-display)">
                  42%
                </text>
              </svg>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', textAlign: 'left' }}>
              {[
                { label: 'Matematik', pct: 60, color: 'var(--neon-purple)' },
                { label: 'Fizik',     pct: 35, color: 'var(--neon-amber)' },
                { label: 'Biyoloji', pct: 15, color: 'var(--neon-danger)' },
              ].map(({ label, pct, color }) => (
                <div key={label}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '0.3rem' }}>
                    <span style={{ color: 'var(--text-secondary)' }}>{label}</span>
                    <span style={{ color, fontWeight: 700, fontFamily: 'var(--font-mono)' }}>%{pct}</span>
                  </div>
                  <div className="progress-bar-track">
                    <div style={{ width: `${pct}%`, height: '100%', background: color, borderRadius: 'var(--radius-full)', transition: 'width 0.5s ease', boxShadow: `0 0 6px ${color}66` }} />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Kritik Dersler */}
          <div className="glass-panel card-danger" style={{ padding: '1.25rem' }}>
            <div style={{ fontSize: '0.7rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--neon-danger)', fontFamily: 'var(--font-mono)', marginBottom: '0.875rem' }}>
              ⚠ Kritik Konular
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
              {['Biyoloji', 'Tarih', 'Geometri'].map(d => (
                <span key={d} className="badge badge-danger">{d}</span>
              ))}
            </div>
          </div>

          {/* Bugünkü Hedef */}
          <div className="glass-panel card-amber" style={{ padding: '1.25rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <div style={{ fontSize: '0.7rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--neon-amber)', fontFamily: 'var(--font-mono)' }}>
                Bugünkü Hedef
              </div>
              <Flame size={20} color="var(--neon-amber)" />
            </div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.4rem', marginBottom: '0.5rem' }}>
              <span style={{ fontFamily: 'var(--font-display)', fontWeight: 900, fontSize: '2.25rem', letterSpacing: '-1px' }}>3</span>
              <span style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>/ 5 Konu</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0 }}>
              Seriyi bozmamak için 2 konu daha işaretle!
            </p>
            <div className="progress-bar-track" style={{ marginTop: '0.875rem' }}>
              <div style={{ width: '60%', height: '100%', background: 'var(--neon-amber)', borderRadius: 'var(--radius-full)', boxShadow: '0 0 8px rgba(245,166,35,0.5)' }} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
