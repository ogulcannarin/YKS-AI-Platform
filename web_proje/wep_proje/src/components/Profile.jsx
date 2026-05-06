import { useEffect, useState } from 'react';
import { supabase } from '../supabaseClient';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { Flame, TrendingUp, Target, Clock } from 'lucide-react';

const mockChartData = [
  { name: 'Pzt', süre: 120 },
  { name: 'Sal', süre: 150 },
  { name: 'Çar', süre: 180 },
  { name: 'Per', süre: 90 },
  { name: 'Cum', süre: 210 },
  { name: 'Cmt', süre: 240 },
  { name: 'Paz', süre: 300 },
];

export default function Profile({ session }) {
    const [kullaniciAdi, setKullaniciAdi] = useState('');
    const email = session?.user?.email;

    useEffect(() => {
        const fetchProfil = async () => {
            const { data, error } = await supabase
                .from('kullanicilar')
                .select('kullanici_adi')
                .eq('email', email)
                .single();
            
            if (data && !error) {
                setKullaniciAdi(data.kullanici_adi);
            }
        };
        if (email) fetchProfil();
    }, [email]);

    return (
        <div className="stagger-container page-content" style={{ maxWidth: '1000px', margin: '0 auto' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem' }}>
                <div>
                    <h2 className="page-title">İstatistikler</h2>
                    <p className="text-muted">Ajan {kullaniciAdi || email} // Sistem verileri yükleniyor...</p>
                </div>
                <button 
                    className="btn-danger" 
                    style={{ padding: '0.5rem 1rem', borderRadius: '4px', cursor: 'pointer' }}
                    onClick={() => supabase.auth.signOut()}
                >
                    Sistemden Çık
                </button>
            </div>
            
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
                <div className="stat-card" style={{ border: '1px solid rgba(127,119,221,0.3)', position: 'relative', overflow: 'hidden' }}>
                    <Clock size={24} className="text-neon-purple" style={{ marginBottom: '1rem' }} />
                    <h3 style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Toplam Çalışma</h3>
                    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'center', gap: '0.5rem' }}>
                        <strong style={{ fontSize: '2.5rem', color: 'var(--text-main)', fontFamily: 'var(--font-display)' }}>42</strong>
                        <span className="text-muted">saat</span>
                    </div>
                    <div style={{ color: 'var(--neon-green)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.2rem', fontSize: '0.9rem', marginTop: '0.5rem' }}>
                        <TrendingUp size={16} /> +12% bu hafta
                    </div>
                </div>

                <div className="stat-card" style={{ border: '1px solid rgba(29,158,117,0.3)' }}>
                    <Target size={24} className="text-neon-green" style={{ marginBottom: '1rem' }} />
                    <h3 style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Çözülen Soru</h3>
                    <strong style={{ fontSize: '2.5rem', color: 'var(--text-main)', fontFamily: 'var(--font-display)', display: 'block' }}>1,204</strong>
                    <div style={{ width: '100%', height: '4px', background: 'rgba(255,255,255,0.1)', marginTop: '1rem', borderRadius: '2px' }}>
                        <div style={{ width: '75%', height: '100%', background: 'var(--neon-green)', borderRadius: '2px' }}></div>
                    </div>
                </div>

                <div className="stat-card" style={{ border: '1px solid rgba(239,159,39,0.3)' }}>
                    <Flame size={24} className="text-neon-amber" style={{ marginBottom: '1rem' }} />
                    <h3 style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Günlük Streak</h3>
                    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'center', gap: '0.5rem' }}>
                        <strong style={{ fontSize: '2.5rem', color: 'var(--text-main)', fontFamily: 'var(--font-display)' }}>14</strong>
                        <span className="text-muted">gün</span>
                    </div>
                    <p style={{ color: 'var(--neon-amber)', fontSize: '0.9rem', marginTop: '0.5rem' }}>Ateşi canlı tut!</p>
                </div>
            </div>

            <div className="glass-panel" style={{ padding: '2rem 1rem 1rem 1rem' }}>
                <h3 style={{ fontFamily: 'var(--font-display)', marginBottom: '1.5rem', marginLeft: '1rem' }}>Çalışma Aktivitesi (Son 7 Gün)</h3>
                <div style={{ height: '300px', width: '100%' }}>
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={mockChartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                            <XAxis dataKey="name" stroke="var(--text-muted)" fontSize={12} tickLine={false} axisLine={false} />
                            <YAxis stroke="var(--text-muted)" fontSize={12} tickLine={false} axisLine={false} />
                            <Tooltip 
                                cursor={{ fill: 'rgba(127,119,221,0.1)' }}
                                contentStyle={{ background: 'var(--bg-secondary)', border: '1px solid var(--neon-purple)', borderRadius: '8px' }}
                            />
                            <Bar dataKey="süre" fill="var(--neon-purple)" radius={[4, 4, 0, 0]} animationDuration={1500} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
}
