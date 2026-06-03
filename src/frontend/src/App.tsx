import React, { useState, useEffect } from 'react';
import KidDashboard from './components/KidDashboard';
import ParentDashboard from './components/ParentDashboard';
import './styles.css';

export default function App() {
  const [role, setRole] = useState<'KID' | 'PARENT' | null>(null);
  const [username, setUsername] = useState('');
  const [balancePoints, setBalancePoints] = useState(0); // <-- Add state for tracking balance
  const [loading, setLoading] = useState(true);

  const fetchUserInfo = () => {
    fetch('/api/user/me')
      .then(res => res.json())
      .then(data => {
        setRole(data.role);
        setUsername(data.username);
        setBalancePoints(data.balancePoints); // <-- Store the live balance
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchUserInfo();
  }, []);

  const handleQuitGame = () => {
    fetch('/logout', { method: 'POST' }).then(() => {
      window.location.href = '/login';
    });
  };

  if (loading) {
    return <div className="loading-screen">✨ Loading Quest Map... ✨</div>;
  }

  const handleHardRefresh = () => {
    // Forces a clean browser reload, fetching fresh assets straight from the server
    window.location.reload();
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-top">
          <h1>✨ ChoreQuest Adventure! ✨</h1>
          <div className="header-right">
            <span className="user-badge">👤 Player: {username.toUpperCase()} ({role})</span>
           <div className="system-actions-group">
             <button
               type="button"
               className="system-btn hard-refresh-btn"
               onClick={handleHardRefresh}
             >
               🔄 Hard Refresh
             </button>

             <button
               type="button"
               className="system-btn quit-game-btn"
               onClick={handleQuitGame} // Your existing quit handler
             >
               🛑 Quit Game
             </button>
           </div>
          </div>
        </div>
      </header>
      <main className="main-content">
        {/* Pass down the live balance and a refresh trigger to the Kid Dashboard */}
        {role === 'KID' && (
          <KidDashboard staticBalance={balancePoints} refreshUserPoints={fetchUserInfo} />
        )}
        {role === 'PARENT' && <ParentDashboard />}
      </main>
    </div>
  );
}