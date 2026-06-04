import { useEffect, useState } from 'react';
import { supabase } from '../supabaseClient';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { Flame, TrendingUp, Target, Clock, Award, Zap, Star } from 'lucide-react';

const mockChartData = [
  { name: 'Pzt', süre: 120 },
  { name: 'Sal', süre: 150 },
  { name: 'Çar', süre: 180 },
  { name: 'Per', süre: 90 },
  { name: 'Cum', süre: 210 },
  { name: 'Cmt', süre: 240 },
  { name: 'Paz', süre: 300 },
];

const ACHIEVEMENTS = [
  { icon: Star,  label: '7 Günlük Seri',  desc: 'Kesintisiz çalışma',  color: 'var(--neon-amber)' },
  { icon: Zap,   label: 'AI Koç Ustası',  desc: '50 soru soruldu',     color: 'var(--neon-purple)' },
  { icon: Award, label: 'Net Avcısı',     desc: '100 net üzeri TYT',   color: 'var(--neon-green)' },
];

export default function Profile({ session }) {
  const [kullaniciAdi, setKullaniciAdi] = useState('');
  const email = session?.user?.email;
  const displayName = kullaniciAdi || email?.split('@')[0] || 'Kullanıcı';
  const avatarLetter = displayName.charAt(0).toUpperCase();

  useEffect(() => {
    const fetchProfil = async () => {
      const { data, error } = await supabase
        .from('kullanicilar')
        .select('kullanici_adi')
        .eq('email', email)
        .single();
      if (data && !error) setKullaniciAdi(data.kullanici_adi);
    };
    if (email) fetchProfil();
  }, [email]);

  return (
    <div className="page-content stagger-container" style={{ maxWidth: '1000px' }}>

      {/* Profile Hero */}
      <div className="glass-panel" style={{
        padding: '2rem',
        marginBottom: '1.5rem',
        background: 'linear-gradient(135deg, rgba(139,127,232,0.08) 0%, transparent 60%)',
        border: '1px solid var(--border-glow)',
        display: 'flex',
        alignItems: 'center',
        gap: '1.5rem',
        flexWrap: 'wrap',
      }}>
        {/* Avatar */}
        <div style={{
          width: 80, height: 80, borderRadius: 'var(--radius-xl)', flexShrink: 0,
          background: 'linear-gradient(135deg, var(--neon-purple), var(--neon-green))',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '2rem', fontWeight: 900, color: '#000',
          fontFamily: 'var(--font-display)',
          boxShadow: 'var(--glow-purple)',
        }}>
          {avatarLetter}
        </div>

        {/* Info */}
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '0.35rem' }}>
            <h2 style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.5rem', letterSpacing: '-0.5px' }}>
              {displayName}
            </h2>
            <span className="badge badge-purple">Pro</span>
          </div>
          <div style={{ fontSize: '0.825rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginBottom: '0.75rem' }}>
            {email}
          </div>
          <div style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap' }}>
            <div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', fontFamily: 'var(--font-mono)' }}>Hedef</div>
              <div style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--neon-purple)' }}>Tıp Fakültesi</div>
            </div>
            <div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', fontFamily: 'var(--font-mono)' }}>Sınav</div>
              <div style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--neon-amber)' }}>YKS 2026</div>
            </div>
            <div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', fontFamily: 'var(--font-mono)' }}>Üye Olma</div>
              <div style={{ fontSize: '0.875rem', fontWeight: 600 }}>Haziran 2026</div>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
        {[
          { Icon: Clock,     label: 'Toplam Çalışma', value: '42',   unit: 'saat',   color: 'var(--neon-purple)', change: '+12% bu hafta', up: true },
          { Icon: Target,    label: 'Çözülen Soru',   value: '1,204', unit: 'soru',   color: 'var(--neon-green)', pct: 75 },
          { Icon: Flame,     label: 'Günlük Seri',    value: '14',   unit: 'gün',    color: 'var(--neon-amber)', msg: 'Ateşi canlı tut!' },
          { Icon: TrendingUp,label: 'Bu Hafta Net',   value: '+8.5', unit: 'net',    color: 'var(--neon-blue)',  change: 'TYT Matematik', up: true },
        ].map(({ Icon, label, value, unit, color, change, up, pct, msg }) => (
          <div key={label} className="glass-panel" style={{
            padding: '1.375rem',
            borderTop: `2px solid ${color}`,
            display: 'flex', flexDirection: 'column',
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
              <div style={{ width: 32, height: 32, borderRadius: 'var(--radius-md)', background: `${color}18`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Icon size={17} color={color} />
              </div>
              <span style={{ fontSize: '0.72rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                {label}
              </span>
            </div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.35rem', marginBottom: '0.5rem' }}>
              <span style={{ fontFamily: 'var(--font-display)', fontWeight: 900, fontSize: '2rem', letterSpacing: '-0.5px', color: 'var(--text-primary)', lineHeight: 1 }}>
                {value}
              </span>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{unit}</span>
            </div>
            {change && (
              <span style={{ fontSize: '0.78rem', color: up ? 'var(--neon-green)' : 'var(--neon-danger)', display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                {up ? '↑' : '↓'} {change}
              </span>
            )}
            {pct !== undefined && (
              <div className="progress-bar-track" style={{ marginTop: '0.5rem' }}>
                <div style={{ width: `${pct}%`, height: '100%', background: color, borderRadius: 'var(--radius-full)', boxShadow: `0 0 6px ${color}66` }} />
              </div>
            )}
            {msg && (
              <span style={{ fontSize: '0.78rem', color }}>{msg}</span>
            )}
          </div>
        ))}
      </div>

      {/* Chart + Achievements */}
      <div style={{ display: 'flex', gap: '1.25rem', flexWrap: 'wrap' }}>
        {/* Activity Chart */}
        <div className="glass-panel" style={{ flex: '1 1 400px', padding: '1.5rem' }}>
          <div style={{ marginBottom: '1.25rem' }}>
            <h3 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1rem', marginBottom: '0.2rem' }}>
              Çalışma Aktivitesi
            </h3>
            <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>Son 7 gün (dakika)</p>
          </div>
          <div style={{ height: 260 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={mockChartData} barSize={28}>
                <defs>
                  <linearGradient id="chartGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%"  stopColor="var(--neon-purple)" stopOpacity={0.9} />
                    <stop offset="100%" stopColor="var(--neon-green)" stopOpacity={0.6} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false} />
                <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={11} tickLine={false} axisLine={false} />
                <YAxis stroke="var(--text-muted)" fontSize={11} tickLine={false} axisLine={false} />
                <Tooltip
                  cursor={{ fill: 'rgba(139,127,232,0.07)' }}
                  contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--border-glow)', borderRadius: '10px', fontSize: '0.8rem' }}
                  labelStyle={{ color: 'var(--text-primary)', fontWeight: 700 }}
                />
                <Bar dataKey="süre" fill="url(#chartGrad)" radius={[6, 6, 0, 0]} animationDuration={1200} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Achievements */}
        <div style={{ flex: '0 0 260px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="glass-panel" style={{ padding: '1.375rem' }}>
            <h3 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1rem', marginBottom: '1.125rem' }}>
              🏆 Başarımlar
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.875rem' }}>
              {ACHIEVEMENTS.map(({ icon: Icon, label, desc, color }) => (
                <div key={label} style={{ display: 'flex', alignItems: 'center', gap: '0.875rem' }}>
                  <div style={{
                    width: 38, height: 38, borderRadius: 'var(--radius-md)', flexShrink: 0,
                    background: `${color}18`, border: `1px solid ${color}40`,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    <Icon size={18} color={color} />
                  </div>
                  <div>
                    <div style={{ fontSize: '0.875rem', fontWeight: 600, marginBottom: '0.1rem' }}>{label}</div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>{desc}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Quick actions */}
          <div className="glass-panel" style={{ padding: '1.375rem' }}>
            <h3 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1rem', marginBottom: '1rem' }}>
              Hızlı Ayarlar
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <button className="btn btn-ghost btn-sm btn-full" style={{ justifyContent: 'flex-start' }}>
                Profili Düzenle
              </button>
              <button className="btn btn-ghost btn-sm btn-full" style={{ justifyContent: 'flex-start' }}>
                Bildirim Ayarları
              </button>
              <button
                className="btn btn-danger btn-sm btn-full"
                style={{ justifyContent: 'flex-start', marginTop: '0.25rem' }}
                onClick={() => supabase.auth.signOut()}
              >
                Çıkış Yap
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
