import React, { useState, useEffect } from 'react';

interface Item {
  id: number;
  taskName: string;
  dayOfWeek: string;
  points: number;
  completed: boolean;
  rewardItem: boolean;
}

export default function KidDashboard() {
  const [items, setItems] = useState<Item[]>([]);
  const [activeTab, setActiveTab] = useState<'CHORES' | 'REWARDS'>('CHORES');

  useEffect(() => {
    fetch('/api/kid/chores')
      .then(res => res.json())
      .then(data => setItems(data));
  }, []);

  const completeChore = (id: number) => {
    fetch(`/api/kid/complete/${id}`, { method: 'POST' })
      .then(() => {
        setItems(items.map(item => item.id === id ? { ...item, completed: true } : item));
      });
  };

  const totalPoints = items
    .filter(i => !i.rewardItem && i.completed)
    .reduce((sum, current) => sum + current.points, 0);

  return (
    <div className="dashboard kid-zone">
      <div className="score-banner">
        ⭐ You Have Accumulated: <span className="points-badge">{totalPoints} Points</span> ⭐
      </div>

      <div className="tab-bar">
        <button className={`tab-btn ${activeTab === 'CHORES' ? 'active' : ''}`} onClick={() => setActiveTab('CHORES')}>📜 My Daily Quests</button>
        <button className={`tab-btn ${activeTab === 'REWARDS' ? 'active' : ''}`} onClick={() => setActiveTab('REWARDS')}>🎁 Claim Rewards</button>
      </div>

      <div className="content-grid">
        {activeTab === 'CHORES' ? (
          items.filter(i => !i.rewardItem).map(chore => (
            <div key={chore.id} className={`card chore-card ${chore.completed ? 'done' : ''}`}>
              <h3>{chore.taskName}</h3>
              <p className="day-tag">📅 {chore.dayOfWeek}</p>
              <span className="point-tag">💎 {chore.points} Pts</span>
              {!chore.completed ? (
                <button className="claim-btn" onClick={() => completeChore(chore.id)}>I Completed This! 🎉</button>
              ) : <p className="complete-status">✅ Finished!</p>}
            </div>
          ))
        ) : (
          items.filter(i => i.rewardItem).map(reward => (
            <div key={reward.id} className="card reward-card">
              <h3>{reward.taskName}</h3>
              <span className="point-tag cost">💰 Costs: {reward.points} Pts</span>
              <button className="redeem-btn" disabled={totalPoints < reward.points}>Redeem Reward 🚀</button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}