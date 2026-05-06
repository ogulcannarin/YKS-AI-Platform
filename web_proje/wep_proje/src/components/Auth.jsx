import { useState } from 'react';
import { supabase } from '../supabaseClient';

export default function Auth() {
  const [loading, setLoading] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [kullaniciAdi, setKullaniciAdi] = useState('');
  const [isLogin, setIsLogin] = useState(true);

  const handleAuth = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (isLogin) {
        const { error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) {
            alert("Giriş Hatası: Lütfen bilgilerinizi kontrol edin.");
            throw error;
        }
      } else {
        // Sign up
        const { data, error } = await supabase.auth.signUp({ email, password });
        if (error) {
            alert("Kayıt Hatası: Şifre çok kısa olabilir veya email zaten kullanımda.");
            throw error;
        }
        
        // Eğer email onayı gerekmiyorsa ve direkt giriş yapıldıysa:
        if (data?.session) {
          // Profil tablosuna ekle
          const { error: dbError } = await supabase.from('kullanicilar').insert([
            { email: email, kullanici_adi: kullaniciAdi }
          ]);
          if (dbError && dbError.code !== '23505') {
              console.error("Profil ekleme hatası:", dbError);
          }
        } else if (data?.user) {
          alert('Kayıt başarılı! Lütfen e-postanıza gelen doğrulama linkine tıklayın.');
          setIsLogin(true);
        }
      }
    } catch (error) {
      console.error("Auth error:", error);
    } finally {
      if (window.location) {
         setLoading(false); // only if not unmounted, but React handles this mostly
      }
    }
  };

  return (
    <div className="glass-panel page-content" style={{ maxWidth: '400px', margin: '2rem auto' }}>
      <h2 className="page-title text-center">{isLogin ? 'Giriş Yap' : 'Kayıt Ol'}</h2>
      <form onSubmit={handleAuth} className="mt-4">
        {!isLogin && (
          <div className="form-group">
            <label>Kullanıcı Adı</label>
            <input type="text" value={kullaniciAdi} onChange={e => setKullaniciAdi(e.target.value)} required />
          </div>
        )}
        <div className="form-group">
          <label>Email</label>
          <input type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Şifre</label>
          <input type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
        <button className="glow-on-hover" type="submit" disabled={loading}>
          {loading ? 'Bekleniyor...' : (isLogin ? 'Giriş Yap' : 'Kayıt Ol')}
        </button>
      </form>
      <p className="text-center mt-4 text-muted" style={{ cursor: 'pointer' }} onClick={() => setIsLogin(!isLogin)}>
        {isLogin ? "Hesabın yok mu? Kayıt Ol" : "Zaten hesabın var mı? Giriş Yap"}
      </p>
    </div>
  );
}
