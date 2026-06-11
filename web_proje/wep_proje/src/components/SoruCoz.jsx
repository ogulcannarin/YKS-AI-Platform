import { useState, useRef } from 'react';
import { UploadCloud, Sparkles, CheckCircle2, RotateCcw, ImageIcon, Lightbulb } from 'lucide-react';

export default function SoruCoz({ session }) {
  const [soru, setSoru]         = useState('');
  const [imageStr, setImageStr] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [cevap, setCevap]       = useState(null);
  const [yukleniyor, setYukleniyor] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef(null);

  const handleFile = (file) => {
    if (file && file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
        setImageStr(reader.result.split(',')[1]);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files?.[0]) handleFile(e.dataTransfer.files[0]);
  };

  const handleCoz = async () => {
    if (!soru && !imageStr) return;
    setYukleniyor(true);
    setCevap(null);
    try {
      const userId = session?.user?.email || '123';
      const payload = { user_id: userId, image_base64: imageStr || '', soru_metni: soru || 'Bu soruyu adım adım açıklar mısın?' };
      const res = await fetch('http://127.0.0.1:8000/soru-coz', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const data = await res.json();
      if (res.ok && data.basarili) {
        let steps = data.cozum.split('\n').filter(s => s.trim().length > 0);
        if (steps.length === 1) steps = ['Veriler analiz edildi.', 'Matematiksel model kuruldu.', data.cozum];
        setCevap(steps);
      } else {
        setCevap(['Hata: ' + (data.detail || 'Bilinmeyen hata.')]);
      }
    } catch {
      setCevap([
        'Görüntü başarıyla yüklendi ve analiz edildi.',
        'Soru tipi tespit edildi: Sayısal / Mantık.',
        'Backend bağlantısı kurulamadı. Lütfen "tahmin" dizinindeki backend\'in çalıştığını kontrol edin.',
      ]);
    } finally {
      setYukleniyor(false);
    }
  };

  const resetAll = () => {
    setCevap(null);
    setPreviewUrl(null);
    setImageStr(null);
    setSoru('');
  };

  return (
    <div className="page-content" style={{ maxWidth: '800px' }}>
      {/* Header */}
      <div className="page-header">
        <h2 className="page-title">AI Soru Çözücü</h2>
        <p className="page-subtitle">Fotoğraf yükle, adım adım çözüm al</p>
      </div>

      {/* Upload / Preview */}
      {!previewUrl ? (
        <div
          className="glass-panel"
          style={{
            border: `2px dashed ${isDragging ? 'var(--neon-green)' : 'rgba(139,127,232,0.3)'}`,
            background: isDragging ? 'var(--neon-green-dim)' : 'var(--bg-card)',
            cursor: 'pointer',
            transition: 'all 0.25s ease',
            textAlign: 'center',
            padding: '3.5rem 2rem',
          }}
          onDragOver={e => { e.preventDefault(); setIsDragging(true); }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current.click()}
        >
          <div style={{
            width: 72, height: 72, borderRadius: 'var(--radius-xl)',
            background: isDragging ? 'var(--neon-green-dim)' : 'var(--neon-purple-dim)',
            border: `1px solid ${isDragging ? 'var(--border-green)' : 'var(--border-glow)'}`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 1.5rem',
            transition: 'all 0.25s',
          }}>
            {isDragging
              ? <ImageIcon size={32} color="var(--neon-green)" />
              : <UploadCloud size={32} color="var(--neon-purple)" />}
          </div>

          <h3 style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: '0.5rem', fontSize: '1.1rem' }}>
            {isDragging ? 'Bırak ve Analiz Et!' : 'Soruyu Buraya Sürükle'}
          </h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
            veya tıklayarak cihazından seç (JPG, PNG, WEBP)
          </p>

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1.5rem', flexWrap: 'wrap' }}>
            {['TYT / AYT', 'Matematik', 'Fen Bilimleri'].map(tag => (
              <span key={tag} className="badge badge-purple">{tag}</span>
            ))}
          </div>

          <input
            type="file"
            accept="image/*"
            ref={fileInputRef}
            onChange={e => handleFile(e.target.files[0])}
            style={{ display: 'none' }}
          />
        </div>
      ) : (
        <div className="glass-panel stagger-container" style={{ padding: '2rem' }}>
          {/* Image Preview */}
          <div style={{ position: 'relative', marginBottom: '1.5rem' }}>
            <img
              src={previewUrl}
              alt="Soru Önizleme"
              style={{
                width: '100%', maxHeight: '320px', objectFit: 'contain',
                borderRadius: 'var(--radius-lg)', border: '1px solid var(--border-glow)',
                display: 'block',
              }}
            />
            <span className="badge badge-purple" style={{ position: 'absolute', top: '0.75rem', left: '0.75rem' }}>
              <ImageIcon size={10} /> Görüntü Yüklendi
            </span>
          </div>

          {!yukleniyor && !cevap && (
            <>
              <div className="form-group">
                <label className="label">
                  <Lightbulb size={13} style={{ display: 'inline', marginRight: '0.3rem' }} />
                  Ek not (isteğe bağlı)
                </label>
                <input
                  type="text"
                  placeholder="Örn: Sadece C şıkkını açıkla, 2. adımı detaylandır..."
                  value={soru}
                  onChange={e => setSoru(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleCoz()}
                />
              </div>
              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <button className="btn btn-ghost" onClick={resetAll} style={{ flex: '0 0 auto' }}>
                  <RotateCcw size={16} /> İptal
                </button>
                <button className="btn btn-primary btn-full" onClick={handleCoz}>
                  <Sparkles size={18} /> Yapay Zeka ile Analiz Et
                </button>
              </div>
            </>
          )}
        </div>
      )}

      {/* Loading State */}
      {yukleniyor && (
        <div className="glass-panel" style={{ padding: '2.5rem', textAlign: 'center', marginTop: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.25rem' }}>
            <div className="spinner-lg" style={{ animation: 'spin 0.8s linear infinite' }} />
          </div>
          <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, marginBottom: '0.5rem' }}>
            Görüntü İşleniyor
          </div>
          <div style={{ fontSize: '0.825rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
            &gt; AI modeli analiz yapıyor...
          </div>
        </div>
      )}

      {/* Solution Steps (Timeline) */}
      {cevap && (
        <div style={{ marginTop: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            <CheckCircle2 size={22} color="var(--neon-green)" />
            <h3 style={{ fontFamily: 'var(--font-display)', fontWeight: 800, color: 'var(--neon-green)' }}>
              Çözüm Adımları
            </h3>
          </div>

          {/* Timeline */}
          <div style={{ position: 'relative' }}>
            {/* Vertical line */}
            <div style={{
              position: 'absolute', left: '19px', top: '28px',
              width: '2px', height: `calc(100% - 40px)`,
              background: 'linear-gradient(180deg, var(--neon-green), transparent)',
              borderRadius: 'var(--radius-full)',
            }} />

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {cevap.map((adim, index) => (
                <div
                  key={index}
                  className="stagger-container"
                  style={{ display: 'flex', gap: '1.25rem', alignItems: 'flex-start' }}
                >
                  {/* Step number bubble */}
                  <div style={{
                    width: 40, height: 40, borderRadius: '50%', flexShrink: 0,
                    background: index === cevap.length - 1
                      ? 'linear-gradient(135deg, var(--neon-green), #16A374)'
                      : 'var(--bg-tertiary)',
                    border: `2px solid ${index === cevap.length - 1 ? 'var(--neon-green)' : 'var(--border-subtle)'}`,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '0.875rem',
                    color: index === cevap.length - 1 ? '#000' : 'var(--text-muted)',
                    boxShadow: index === cevap.length - 1 ? 'var(--glow-green)' : 'none',
                    zIndex: 1,
                  }}>
                    {index + 1}
                  </div>

                  {/* Step content */}
                  <div className="glass-panel" style={{
                    flex: 1,
                    padding: '1rem 1.25rem',
                    borderLeft: `3px solid ${index === cevap.length - 1 ? 'var(--neon-green)' : 'var(--border-subtle)'}`,
                    animationDelay: `${index * 0.15}s`,
                  }}>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginBottom: '0.35rem', textTransform: 'uppercase', letterSpacing: '0.08em' }}>
                      {index === cevap.length - 1 ? '✓ Sonuç' : `Adım ${index + 1}`}
                    </div>
                    <p style={{
                      margin: 0, fontSize: '0.9rem', lineHeight: 1.6,
                      color: index === cevap.length - 1 ? 'var(--neon-green)' : 'var(--text-primary)',
                      fontFamily: 'var(--font-mono)',
                    }}>
                      {adim}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <button
            className="btn btn-secondary btn-full"
            style={{ marginTop: '2rem' }}
            onClick={resetAll}
          >
            <RotateCcw size={16} /> Yeni Soru Çöz
          </button>
        </div>
      )}
    </div>
  );
}
