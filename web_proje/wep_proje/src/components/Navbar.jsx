export default function Navbar({ currentPage, setCurrentPage }) {
  const navItems = [
    { id: 'calculator', label: 'Hesapla', icon: '🔢' },
    { id: 'sorucoz', label: 'Soru Çöz', icon: '📸' },
    { id: 'aidanisman', label: 'AI Koç', icon: '🤖' },
    { id: 'konutakip', label: 'Konu Takip', icon: '📚' },
    { id: 'profile', label: 'Profil', icon: '👤' }
  ];

  return (
    <nav className="navbar glass-panel">
      {navItems.map(item => (
        <button
          key={item.id}
          className={`nav-item ${currentPage === item.id ? 'active' : ''}`}
          onClick={() => setCurrentPage(item.id)}
        >
          <span className="nav-icon">{item.icon}</span>
          <span className="nav-label">{item.label}</span>
        </button>
      ))}
    </nav>
  );
}
