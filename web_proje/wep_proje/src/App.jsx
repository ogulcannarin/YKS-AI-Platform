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
        <div className="background-animation" />
        <Auth />
      </>
    );
  }

  const renderPage = () => {
    switch (currentPage) {
      case 'calculator': return <Calculator key="calculator" results={hesaplananSonuclar} setResults={setHesaplananSonuclar} />;
      case 'sorucoz':    return <SoruCoz    key="sorucoz"    session={session} />;
      case 'aidanisman': return <AiDanisman key="aidanisman" session={session} results={hesaplananSonuclar} />;
      case 'konutakip':  return <KonuTakip  key="konutakip"  session={session} />;
      case 'profile':    return <Profile    key="profile"    session={session} />;
      default:           return <Calculator key="calculator" results={hesaplananSonuclar} setResults={setHesaplananSonuclar} />;
    }
  };

  return (
    <>
      <div className="background-animation" />
      <div className="app-layout">
        {/* Desktop Sidebar */}
        <Navbar
          currentPage={currentPage}
          setCurrentPage={setCurrentPage}
          session={session}
        />

        {/* Main Content */}
        <main className="app-main">
          {renderPage()}
        </main>
      </div>
    </>
  );
}

export default App;
