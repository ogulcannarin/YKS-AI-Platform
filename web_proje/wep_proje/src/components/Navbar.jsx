import { supabase } from '../supabaseClient';
import {
  Calculator,
  Camera,
  Bot,
  BookOpen,
  User,
  Zap,
  LogOut,
  TrendingUp,
} from 'lucide-react';

const navItems = [
  { id: 'calculator', label: 'Puan Hesapla',  Icon: Calculator, badge: null },
  { id: 'sorucoz',    label: 'Soru Çöz',      Icon: Camera,     badge: 'AI' },
  { id: 'aidanisman', label: 'AI Koç',         Icon: Bot,        badge: 'AI' },
  { id: 'konutakip',  label: 'Konu Takip',    Icon: BookOpen,   badge: null },
  { id: 'profile',    label: 'İstatistikler',  Icon: TrendingUp, badge: null },
];

export default function Navbar({ currentPage, setCurrentPage, session }) {
  const email = session?.user?.email || '';
  const displayName = email.split('@')[0] || 'Kullanıcı';
  const avatarLetter = displayName.charAt(0).toUpperCase();

  const handleSignOut = async () => {
    await supabase.auth.signOut();
  };

  return (
    <>
      {/* ── Desktop Sidebar ── */}
      <aside className="app-sidebar">
        {/* Logo */}
        <div className="sidebar-logo">
          <div className="sidebar-logo-icon">
            <Zap size={18} color="#fff" />
          </div>
          <div className="sidebar-logo-text">
            <span className="sidebar-logo-title">YKS Asistan</span>
            <span className="sidebar-logo-sub">v2.0 · AI Powered</span>
          </div>
        </div>

        {/* Navigation */}
        <nav className="sidebar-nav">
          <span className="sidebar-section-label">Menü</span>

          {navItems.map(({ id, label, Icon, badge }) => (
            <button
              key={id}
              className={`nav-item ${currentPage === id ? 'active' : ''}`}
              onClick={() => setCurrentPage(id)}
              title={label}
            >
              <span className="nav-item-icon">
                <Icon size={17} />
              </span>
              <span className="nav-item-label">{label}</span>
              {badge && (
                <span className="nav-item-badge">{badge}</span>
              )}
            </button>
          ))}
        </nav>

        {/* Footer */}
        <div className="sidebar-footer">
          {/* User info */}
          <div className="sidebar-user">
            <div className="sidebar-user-avatar">{avatarLetter}</div>
            <div className="sidebar-user-info">
              <div className="sidebar-user-name">{displayName}</div>
              <div className="sidebar-user-email">{email}</div>
            </div>
          </div>

          {/* Sign out */}
          <button
            className="nav-item"
            onClick={handleSignOut}
            style={{ color: 'var(--neon-danger)', marginTop: '0.25rem' }}
          >
            <span className="nav-item-icon"><LogOut size={17} /></span>
            <span className="nav-item-label">Çıkış Yap</span>
          </button>
        </div>
      </aside>

      {/* ── Mobile Bottom Navbar ── */}
      <nav className="mobile-navbar">
        {navItems.map(({ id, label, Icon }) => (
          <button
            key={id}
            className={`mobile-nav-item ${currentPage === id ? 'active' : ''}`}
            onClick={() => setCurrentPage(id)}
          >
            <Icon size={20} />
            <span>{label.split(' ')[0]}</span>
          </button>
        ))}
      </nav>
    </>
  );
}
