import { useState, useEffect } from 'react';
import './index.css';
import { supabase } from './supabaseClient';
import Navbar from './components/Navbar';
import Calculator from './components/Calculator';
import SoruCoz from './components/SoruCoz';
import KonuTakip from './components/KonuTakip';
import Profile from './components/Profile';
import Auth from './components/Auth';
import AiDanisman from './components/AiDanisman';

function App() {
  const [session, setSession] = useState(null);
  const [currentPage, setCurrentPage] = useState('calculator');
  const [hesaplananSonuclar, setHesaplananSonuclar] = useState(null);

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session);
    });

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session);
    });

    return () => subscription.unsubscribe();
  }, []);

  if (!session) {
    return (
      <>
        <div className="background-animation"></div>
        <div className="app-layout" style={{ justifyContent: 'center', paddingBottom: 0 }}>
          <header className="main-header stagger-container" style={{ textAlign: 'center', marginTop: '10vh' }}>
            <h1 className="glitch-text" data-text="YKS Asistanım" style={{ fontSize: '4rem', marginBottom: '1rem' }}>
              YKS Asistanım
            </h1>
            <div className="typewriter" style={{ color: 'var(--neon-green)', fontSize: '1.5rem', marginBottom: '3rem', fontFamily: 'var(--font-body)' }}>
              &gt; Geleceğini kodla. Sınavı hackle._
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'center', gap: '2rem', marginBottom: '4rem' }}>
              <div className="stat-card" style={{ flex: '1', maxWidth: '200px' }}>
                <div className="text-neon-purple" style={{ fontSize: '2rem', marginBottom: '1rem' }}>📊</div>
                <h3 style={{ fontSize: '1rem' }}>Puan Hesapla</h3>
              </div>
              <div className="stat-card" style={{ flex: '1', maxWidth: '200px' }}>
                <div className="text-neon-green" style={{ fontSize: '2rem', marginBottom: '1rem' }}>🤖</div>
                <h3 style={{ fontSize: '1rem' }}>AI Koç</h3>
              </div>
              <div className="stat-card" style={{ flex: '1', maxWidth: '200px' }}>
                <div className="text-neon-amber" style={{ fontSize: '2rem', marginBottom: '1rem' }}>📸</div>
                <h3 style={{ fontSize: '1rem' }}>Soru Çöz</h3>
              </div>
            </div>

            <Auth />
          </header>
        </div>
      </>
    );
  }

  const renderPage = () => {
    switch (currentPage) {
      case 'calculator': return <Calculator results={hesaplananSonuclar} setResults={setHesaplananSonuclar} />;
      case 'sorucoz': return <SoruCoz session={session} />;
      case 'aidanisman': return <AiDanisman session={session} results={hesaplananSonuclar} />;
      case 'konutakip': return <KonuTakip session={session} />;
      case 'profile': return <Profile session={session} />;
      default: return <Calculator results={hesaplananSonuclar} setResults={setHesaplananSonuclar} />;
    }
  };

  return (
    <>
      <div className="background-animation"></div>
      <div className="app-layout">
        <header className="main-header">
          <h1>YKS Asistanım</h1>
        </header>

        <main className="main-content">
          {renderPage()}
        </main>

        <Navbar currentPage={currentPage} setCurrentPage={setCurrentPage} />
      </div>
    </>
  );
}

export default App;
