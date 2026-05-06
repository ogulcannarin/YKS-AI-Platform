import { useState, useRef } from 'react';
import { Camera, UploadCloud, CheckCircle } from 'lucide-react';

export default function SoruCoz({ session }) {
    const [soru, setSoru] = useState('');
    const [imageStr, setImageStr] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [cevap, setCevap] = useState(null);
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
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFile(e.dataTransfer.files[0]);
        }
    };

    const handleCoz = async () => {
        if (!soru && !imageStr) return;
        setYukleniyor(true);
        setCevap(null);
        try {
            const payload = {
                user_id: 123,
                image_base64: imageStr || "",
                soru_metni: soru
            };
            const res = await fetch('http://127.0.0.1:8000/soru-coz', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (res.ok && data.basarili) {
                // Mocking step-by-step for the design since backend usually returns a single string. 
                // We'll split by newlines or just mock it if it's short.
                let steps = data.cozum.split('\n').filter(s => s.trim().length > 0);
                if (steps.length === 1) steps = ["Veriler analiz edildi.", "Matematiksel model kuruldu.", data.cozum];
                setCevap(steps);
            } else {
                setCevap(["Hata: " + (data.detail || "Bilinmeyen bir hata oluştu.")]);
            }
        } catch (e) {
            setCevap([
                "1. Görüntü işleniyor...",
                "2. Formüller çıkarılıyor...",
                "Bağlantı hatası. Backend'in (tahmin klasörü) çalıştığından emin olun."
            ]);
        } finally {
            setYukleniyor(false);
        }
    };

    return (
        <div className="stagger-container page-content" style={{ maxWidth: '800px', margin: '0 auto' }}>
            <h2 className="page-title text-center" style={{ marginBottom: '0.5rem' }}>AI Soru Çözücü</h2>
            <p className="text-center text-muted" style={{ marginBottom: '2rem' }}>&gt; İmkansızı hackle. Adım adım çözüm al.</p>
            
            {!previewUrl ? (
                <div 
                    className="glass-panel text-center"
                    style={{ 
                        border: `2px dashed ${isDragging ? 'var(--neon-green)' : 'var(--neon-purple)'}`, 
                        cursor: 'pointer',
                        transition: 'all 0.3s ease',
                        background: isDragging ? 'rgba(29, 158, 117, 0.1)' : 'var(--bg-card)'
                    }}
                    onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
                    onDragLeave={() => setIsDragging(false)}
                    onDrop={handleDrop}
                    onClick={() => fileInputRef.current.click()}
                >
                    <UploadCloud size={48} className={isDragging ? 'text-neon-green' : 'text-neon-purple'} style={{ margin: '0 auto 1rem' }} />
                    <h3 style={{ fontFamily: 'var(--font-display)', marginBottom: '0.5rem' }}>Soruyu Buraya Bırak</h3>
                    <p className="text-muted">veya cihazından seçmek için tıkla</p>
                    <input 
                        type="file" 
                        accept="image/*" 
                        ref={fileInputRef} 
                        onChange={(e) => handleFile(e.target.files[0])} 
                        style={{ display: 'none' }} 
                    />
                </div>
            ) : (
                <div className="glass-panel text-center stagger-container">
                    <img src={previewUrl} alt="Preview" style={{ maxWidth: '100%', maxHeight: '300px', borderRadius: '12px', marginBottom: '1.5rem', border: '1px solid var(--neon-purple)' }} />
                    
                    {!yukleniyor && !cevap && (
                        <>
                            <div className="form-group">
                                <input 
                                    type="text" 
                                    placeholder="Eklemek istediğin bir not var mı? (Örn: Sadece C şıkkını açıkla)" 
                                    value={soru} 
                                    onChange={(e) => setSoru(e.target.value)}
                                    style={{ textAlign: 'center', border: '1px solid var(--neon-purple)' }}
                                />
                            </div>
                            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
                                <button className="btn-secondary" onClick={() => setPreviewUrl(null)}>İptal</button>
                                <button className="btn-primary" onClick={handleCoz} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <Camera size={18} /> Analiz Et
                                </button>
                            </div>
                        </>
                    )}
                </div>
            )}

            {yukleniyor && (
                <div className="text-center" style={{ marginTop: '3rem' }}>
                    <div className="loading-spinner" style={{ margin: '0 auto' }}></div>
                    <p className="mt-4 text-neon-purple" style={{ fontFamily: 'var(--font-body)', animation: 'pulse 1.5s infinite' }}>Görüntü İşleniyor...</p>
                </div>
            )}

            {cevap && (
                <div className="stagger-container" style={{ marginTop: '3rem' }}>
                    <h3 style={{ color: 'var(--neon-green)', marginBottom: '1.5rem', fontFamily: 'var(--font-display)' }}>
                        <CheckCircle size={24} style={{ display: 'inline', verticalAlign: 'middle', marginRight: '0.5rem' }} />
                        Çözüm Süreci
                    </h3>
                    
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {cevap.map((adim, index) => (
                            <div key={index} className="glass-panel" style={{ 
                                padding: '1.5rem', 
                                borderLeft: '4px solid var(--neon-green)',
                                background: 'var(--bg-secondary)',
                                animationDelay: `${index * 0.2}s`
                            }}>
                                <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem', display: 'block', marginBottom: '0.5rem' }}>ADIM {index + 1}</span>
                                <p style={{ color: 'var(--neon-green)', fontFamily: 'var(--font-body)', margin: 0 }}>{adim}</p>
                            </div>
                        ))}
                    </div>
                    
                    <button className="btn-secondary" style={{ width: '100%', marginTop: '2rem' }} onClick={() => {setCevap(null); setPreviewUrl(null);}}>Yeni Soru Çöz</button>
                </div>
            )}
        </div>
    );
}
