import React, { useState, useEffect } from 'react';

interface KidProfile { id: number; username: string; }
const DAYS_OF_WEEK = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

export default function ParentDashboard() {
  const [kids, setKids] = useState<KidProfile[]>([]);
  const [selectedKid, setSelectedKid] = useState('');
  const [taskName, setTaskName] = useState('');
  const [selectedDays, setSelectedDays] = useState<string[]>([]);
  const [points, setPoints] = useState(10);
  const [isReward, setIsReward] = useState(false);

  useEffect(() => {
    fetch('/api/parent/kids')
      .then(res => res.json())
      .then(data => {
        setKids(data);
        if(data.length > 0) setSelectedKid(data[0].username);
      });
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const endpoint = isReward ? '/api/parent/reward' : '/api/parent/chore';

    if (!isReward && selectedDays.length === 0) {
      alert('Please select at least one target day!');
      return;
    }

    const basePayload = { taskName, points, completed: false, assignedToUsername: selectedKid };

    if (isReward) {
      await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...basePayload, dayOfWeek: '' })
      });
    } else {
      const requests = selectedDays.map(day =>
        fetch(endpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...basePayload, dayOfWeek: day })
        })
      );
      await Promise.all(requests);
    }
    alert("Mission deployed to board successfully!");
    setTaskName('');
  };

  return (
    <div className="dashboard parent-zone">
      <h2>🛠️ Parents Command Center</h2>
      <form onSubmit={handleSubmit} className="setup-form">
        <label>
          Target Adventurer (Select Kid):
          <select value={selectedKid} onChange={e => setSelectedKid(e.target.value)}>
            {kids.map(k => <option key={k.id} value={k.username}>{k.username.toUpperCase()}</option>)}
          </select>
        </label>

        {/* Keep the rest of your original components fields (Type, Name, Checkboxes, Points) exactly the same... */}
        <label>
          Item/Quest Type:
          <select onChange={(e) => setIsReward(e.target.value === 'true')}>
            <option value="false">Assign Chore Task</option>
            <option value="true">Create Redemption Prize</option>
          </select>
        </label>

        <label>
          Name:
          <input type="text" value={taskName} onChange={e => setTaskName(e.target.value)} required />
        </label>

        {!isReward && (
          <div className="days-selection-container">
            <span className="section-label">Select Target Days:</span>
            <div className="checkbox-grid">
              {DAYS_OF_WEEK.map(day => (
                <label key={day} className="checkbox-card">
                  <input type="checkbox" checked={selectedDays.includes(day)} onChange={() => {
                    selectedDays.includes(day) ? setSelectedDays(selectedDays.filter(d => d !== day)) : setSelectedDays([...selectedDays, day])
                  }} />
                  {day}
                </label>
              ))}
            </div>
          </div>
        )}

        <label>
          Point Value:
          <input type="number" value={points} onChange={e => setPoints(Number(e.target.value))} />
        </label>

        <button type="submit" className="submit-btn">Deploy to Board 🚀</button>
      </form>
    </div>
  );
}