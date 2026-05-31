import React, { useState } from 'react';

export default function ParentDashboard() {
  const [taskName, setTaskName] = useState('');
  const [dayOfWeek, setDayOfWeek] = useState('Monday');
  const [points, setPoints] = useState(10);
  const [isReward, setIsReward] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const endpoint = isReward ? '/api/parent/reward' : '/api/parent/chore';
    const payload = { taskName, dayOfWeek, points, completed: false };

    fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    }).then(() => {
      alert(`${isReward ? 'Reward' : 'Chore'} assigned successfully!`);
      setTaskName('');
    });
  };

  return (
    <div className="dashboard parent-zone">
      <h2>🛠️ Parents Command Center</h2>
      <form onSubmit={handleSubmit} className="setup-form">
        <label>
          Item/Quest Type:
          <select onChange={(e) => setIsReward(e.target.value === 'true')}>
            <option value="false">Assign Chore Task</option>
            <option value="true">Create Redemption Prize</option>
          </select>
        </label>

        <label>
          Name:
          <input type="text" value={taskName} onChange={e => setTaskName(e.target.value)} placeholder="e.g. Clean room or Lego Set" required />
        </label>

        {!isReward && (
          <label>
            Target Day:
            <select value={dayOfWeek} onChange={e => setDayOfWeek(e.target.value)}>
              {['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'].map(d => (
                <option key={d} value={d}>{d}</option>
              ))}
            </select>
          </label>
        )}

        <label>
          Point Value:
          <input type="number" value={points} onChange={e => setPoints(Number(e.target.value))} min="1" />
        </label>

        <button type="submit" className="submit-btn">Deploy to Board 🚀</button>
      </form>
    </div>
  );
}