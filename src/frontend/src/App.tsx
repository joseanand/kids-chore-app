import React, { useState } from 'react';
import KidDashboard from './components/KidDashboard';
import ParentDashboard from './components/ParentDashboard';
import './styles.css';

export default function App() {
  const [role, setRole] = useState<'KID' | 'PARENT' | null>(null);

  const handleLogout = () => {
    // Creating a form submit or POST request to Spring Security's default /logout
    fetch('/logout', { method: 'POST' })
      .then(() => {
        // Redirect back to the login page cleanly
        window.location.href = '/login';
      })
      .catch(err => console.error("Logout failed:", err));
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <div className="header-top">
          <h1>✨ ChoreQuest Adventure! ✨</h1>
          <button className="logout-btn" onClick={handleLogout}>🚪 Quit Game (Logout)</button>
        </div>
        <div className="role-selector">
          <button className="btn kid-btn" onClick={() => setRole('KID')}>👦 Kid Mode</button>
          <button className="btn parent-btn" onClick={() => setRole('PARENT')}>🔑 Parent Admin</button>
        </div>
      </header>
      <main className="main-content">
        {role === 'KID' && <KidDashboard />}
        {role === 'PARENT' && <ParentDashboard />}
        {!role && (
          <div className="welcome-card">
            <h2>Welcome to your daily adventure map! Select who you are to start earning golden tokens!</h2>
          </div>
        )}
      </main>
    </div>
  );
}