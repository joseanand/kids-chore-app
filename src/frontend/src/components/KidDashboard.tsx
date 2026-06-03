import React, { useState, useEffect } from 'react';

interface Item {
  id: number;
  taskName: string;
  dayOfWeek: string;
  points: number;
  completed: boolean;
  pending: boolean;
  rewardItem: boolean;
}

const DAYS_MAP = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

interface KidDashboardProps {
  staticBalance: number;
  refreshUserPoints: () => void;
}

export default function KidDashboard({ staticBalance, refreshUserPoints }: KidDashboardProps) {
  const [items, setItems] = useState<Item[]>([]);
  const [activeTab, setActiveTab] = useState<'CHORES' | 'REWARDS'>('CHORES');
  const currentDay = DAYS_MAP[new Date().getDay()];

  useEffect(() => {
    fetch('/api/kid/chores')
      .then(res => res.json())
      .then(data => setItems(data));
  }, []);


  const todaysChores = items.filter(i => !i.rewardItem && i.dayOfWeek === currentDay);
  const rewardItems = items.filter(i => i.rewardItem);

  const completeChore = (id: number) => {
    fetch(`/api/kid/complete/${id}`, { method: 'POST' })
      .then(() => {
        // Temporarily track locally that it's submitted
        setItems(items.map(item => item.id === id ? { ...item, pending: true } : item));
      });
  };

  return (
    <div className="dashboard kid-zone">
      <div className="score-banner">
        {/* FIX: Now securely displays the actual database balance (Chores + Bonus Points) */}
        ⭐ Accumulated Balance: <span className="points-badge">{staticBalance} Points</span> ⭐
        <div className="today-indicator">📅 Today's Realm: <strong>{currentDay}</strong></div>
      </div>

      <div className="tab-bar">
        <button className={`tab-btn ${activeTab === 'CHORES' ? 'active' : ''}`} onClick={() => setActiveTab('CHORES')}>
          📜 Today's Quests ({todaysChores.length})
        </button>
        <button className={`tab-btn ${activeTab === 'REWARDS' ? 'active' : ''}`} onClick={() => setActiveTab('REWARDS')}>
          🎁 Claim Rewards
        </button>
      </div>

      <div className="content-grid">
        {activeTab === 'CHORES' ? (
          todaysChores.length > 0 ? (
            todaysChores.map(chore => (
              <div key={chore.id} className={`card chore-card ${chore.completed ? 'done' : ''}`}>
                <h3>{chore.taskName}</h3>
                <span className="point-tag">💎 {chore.points} Pts</span>
                {!chore.completed && !chore.pending ? (
                  <button className="claim-btn" onClick={() => completeChore(chore.id)}>I Completed This! 🎉</button>
                ) : chore.pending ? (
                  <p className="pending-status">⏳ Waiting for Parent Approval...</p>
                ) : <p className="complete-status">✅ Mission Accomplished!</p>}
              </div>
            ))
          ) : (
            <div className="welcome-card clear-day">
              <h2>🎉 All clear for {currentDay}! No active quests left for today. Go play! 🎮</h2>
            </div>
          )
        ) : (
          rewardItems.map(reward => (
            <div key={reward.id} className="card reward-card">
              <h3>{reward.taskName}</h3>
              <span className="point-tag cost">💰 Costs: {reward.points} Pts</span>
              <button className="redeem-btn" disabled={staticBalance < reward.points}>Redeem Reward 🚀</button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}