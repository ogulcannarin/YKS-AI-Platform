import { useState } from 'react';
import { supabase } from '../supabaseClient';
import { Mail, Lock, Eye, EyeOff, User, Zap, CheckCircle, TrendingUp, Bot, Camera } from 'lucide-react';

const features = [
  { Icon: TrendingUp, title: 'Puan Hesaplama', desc: 'TYT & AYT simülatörü ile gerçek zamanlı tahmin' },
  { Icon: Bot,        title: 'AI Koç',         desc: 'Kişiselleştirilmiş yapay zeka danışmanın' },
  { Icon: Camera,     title: 'Soru Çözücü',    desc: 'Fotoğraf yükle, adım adım çözüm al' },
];

export default function Auth() {
  const [loading, setLoading] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [kullaniciAdi, setKullaniciAdi] = useState('');
  const [isLogin, setIsLogin] = useState(true);
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleAuth = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccessMsg('');

    try {
      if (isLogin) {
        const { error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) {
          setError('E-posta veya şifre hatalı. Lütfen tekrar deneyin.');
          throw error;
        }
      } else {
        const { data, error } = await supabase.auth.signUp({ email, password });
        if (error) {
          setError('Kayıt başarısız. Şifre çok kısa veya e-posta zaten kullanımda.');
          throw error;
        }

        if (data?.session) {
          const { error: dbError } = await supabase.from('kullanicilar').insert([
            { email, kullanici_adi: kullaniciAdi }
          ]);
          if (dbError && dbError.code !== '23505') {
            console.error('Profil ekleme hatası:', dbError);
          }
        } else if (data?.user) {
          setSuccessMsg('Kayıt başarılı! E-postanızdaki doğrulama linkine tıklayın.');
          setIsLogin(true);
        }
      }
    } catch (err) {
      console.error('Auth error:', err);
    } finally {
      setLoading(false);
    }
  };

  const switchMode = () => {
    setIsLogin(v => !v);
    setError('');
    setSuccessMsg('');
  };

  return (
    <div className="auth-layout">
      {/* ── Left Panel ── */}
      <div className="auth-left stagger-container">
        {/* Decorative blobs */}
        <div style={{
          position: 'absolute', top: '-100px', left: '-100px',
          width: '400px', height: '400px', borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(139,127,232,0.12) 0%, transparent 70%)',
          pointerEvents: 'none',
        }} />
        <div style={{
          position: 'absolute', bottom: '50px', right: '-80px',
          width: '300px', height: '300px', borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(34,199,138,0.08) 0%, transparent 70%)',
          pointerEvents: 'none',
        }} />

        {/* Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '3rem', position: 'relative' }}>
          <div className="sidebar-logo-icon" style={{ width: 44, height: 44 }}>
            <Zap size={22} color="#fff" />
          </div>
          <div>
            <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.2rem' }}>YKS Asistanım</div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>AI Powered Platform</div>
          </div>
        </div>

        {/* Headline */}
        <div style={{ position: 'relative', marginBottom: '2.5rem' }}>
          <div className="hero-badge" style={{ marginBottom: '1rem' }}>
            <Zap size={12} />
            2026 YKS Hazırlık
          </div>
          <h1 style={{ fontFamily: 'var(--font-display)', fontWeight: 900, fontSize: '2.8rem', letterSpacing: '-1.5px', lineHeight: 1.1, marginBottom: '1rem' }}>
            Geleceğini<br />
            <span className="gradient-text">Yapay Zeka</span><br />
            ile Yaz.
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', lineHeight: 1.7, maxWidth: '360px' }}>
            Türkiye'nin ilk YKS AI platformu ile akıllı çalış, daha az zaman harca, daha yüksek puan al.
          </p>
        </div>

        {/* Feature list */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', position: 'relative' }}>
          {features.map(({ Icon, title, desc }) => (
            <div key={title} style={{ display: 'flex', alignItems: 'flex-start', gap: '0.875rem' }}>
              <div style={{
                width: 36, height: 36, borderRadius: 'var(--radius-md)',
                background: 'var(--neon-purple-dim)', border: '1px solid rgba(139,127,232,0.25)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
              }}>
                <Icon size={16} color="var(--neon-purple)" />
              </div>
              <div>
                <div style={{ fontWeight: 600, fontSize: '0.9rem', marginBottom: '0.1rem' }}>{title}</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{desc}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* ── Right Panel (Form) ── */}
      <div className="auth-right">
        <div className="auth-form-card stagger-container">

          {/* Header */}
          <div style={{ marginBottom: '2rem' }}>
            <h2 style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.75rem', letterSpacing: '-0.5px', marginBottom: '0.35rem' }}>
              {isLogin ? 'Tekrar hoş geldin 👋' : 'Hesap oluştur'}
            </h2>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>
              {isLogin
                ? 'Hesabına giriş yaparak çalışmaya devam et.'
                : 'Ücretsiz hesap oluştur, hemen başla.'}
            </p>
          </div>

          {/* Success/Error messages */}
          {error && (
            <div style={{
              background: 'var(--neon-danger-dim)', border: '1px solid var(--border-danger)',
              borderRadius: 'var(--radius-md)', padding: '0.75rem 1rem',
              marginBottom: '1.25rem', fontSize: '0.85rem', color: 'var(--neon-danger)',
              display: 'flex', alignItems: 'center', gap: '0.5rem',
            }}>
              ⚠️ {error}
            </div>
          )}

          {successMsg && (
            <div style={{
              background: 'var(--neon-green-dim)', border: '1px solid var(--border-green)',
              borderRadius: 'var(--radius-md)', padding: '0.75rem 1rem',
              marginBottom: '1.25rem', fontSize: '0.85rem', color: 'var(--neon-green)',
              display: 'flex', alignItems: 'center', gap: '0.5rem',
            }}>
              <CheckCircle size={16} /> {successMsg}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleAuth}>
            {!isLogin && (
              <div className="form-group">
                <label className="label">Kullanıcı Adı</label>
                <div className="input-wrapper">
                  <span className="input-icon"><User size={16} /></span>
                  <input
                    type="text"
                    placeholder="kullanici_adi"
                    value={kullaniciAdi}
                    onChange={e => setKullaniciAdi(e.target.value)}
                    required
                  />
                </div>
              </div>
            )}

            <div className="form-group">
              <label className="label">E-posta</label>
              <div className="input-wrapper">
                <span className="input-icon"><Mail size={16} /></span>
                <input
                  type="email"
                  placeholder="ornek@mail.com"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="label">Şifre</label>
              <div className="input-wrapper" style={{ position: 'relative' }}>
                <span className="input-icon"><Lock size={16} /></span>
                <input
                  type={showPass ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  required
                  style={{ paddingRight: '3rem' }}
                />
                <button
                  type="button"
                  onClick={() => setShowPass(v => !v)}
                  style={{
                    position: 'absolute', right: '0.875rem', top: '50%',
                    transform: 'translateY(-50%)', background: 'none', border: 'none',
                    color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', padding: 0,
                  }}
                >
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-xl btn-full"
              disabled={loading}
              style={{ marginTop: '0.5rem' }}
            >
              {loading ? (
                <><div className="spinner" style={{ width: 18, height: 18 }} /> Bekleniyor...</>
              ) : (
                isLogin ? 'Giriş Yap →' : 'Hesap Oluştur →'
              )}
            </button>
          </form>

          {/* Toggle */}
          <div className="divider-label" style={{ marginTop: '1.5rem', marginBottom: '1.5rem' }}>
            {isLogin ? 'Hesabın yok mu?' : 'Zaten hesabın var mı?'}
          </div>

          <button
            type="button"
            className="btn btn-ghost btn-full"
            onClick={switchMode}
          >
            {isLogin ? 'Ücretsiz Kayıt Ol' : 'Giriş Yap'}
          </button>

          <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
            Devam ederek Kullanım Şartları'nı kabul etmiş olursunuz.
          </p>
        </div>
      </div>
    </div>
  );
}
