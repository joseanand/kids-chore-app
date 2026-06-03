import React, { useState, useEffect } from 'react';

interface KidProfile { id: number; username: string; balancePoints: number; }
interface ChoreItem { id: number; taskName: string; dayOfWeek: string; points: number; completed: boolean; rewardItem: boolean; }
const DAYS_OF_WEEK = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

export default function ParentDashboard() {
  const [kids, setKids] = useState<KidProfile[]>([]);
  const [selectedKid, setSelectedKid] = useState('');
  const [taskName, setTaskName] = useState('');
  const [selectedDays, setSelectedDays] = useState<string[]>([]);
  const [points, setPoints] = useState(10);
  const [isReward, setIsReward] = useState(false);
  const [bonusPoints, setBonusPoints] = useState(5);
  const [childChores, setChildChores] = useState<ChoreItem[]>([]);

  useEffect(() => {
    fetch('/api/parent/kids')
      .then(res => res.json())
      .then(data => {
        setKids(data);
        if (data.length > 0) setSelectedKid(data[0].username);
      });
  }, []);

  const refreshChildSchedule = () => {
    // If ALL_KIDS is active, clear out the schedule dashboard list preview since it's collective
    if (!selectedKid || selectedKid === 'ALL_KIDS') {
      setChildChores([]);
      return;
    }
    fetch(`/api/parent/chores?username=${selectedKid}`)
      .then(res => res.json())
      .then(data => setChildChores(data));
  };

  useEffect(() => {
    refreshChildSchedule();
  }, [selectedKid]);

  const handleDayChange = (day: string) => {
    selectedDays.includes(day) ? setSelectedDays(selectedDays.filter(d => d !== day)) : setSelectedDays([...selectedDays, day]);
  };

  const handleAllDaysChange = () => {
    selectedDays.length === DAYS_OF_WEEK.length ? setSelectedDays([]) : setSelectedDays([...DAYS_OF_WEEK]);
  };

  const refreshScoreboard = () => {
    fetch('/api/parent/kids').then(res => res.json()).then(data => setKids(data));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const endpoint = isReward ? '/api/parent/reward' : '/api/parent/chore';
    const basePayload = { taskName, points, completed: false, assignedToUsername: selectedKid };

    if (!isReward && selectedDays.length === 0) {
      alert('Please select at least one target day!');
      return;
    }

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

    alert("Missions deployed successfully! 🚀");
    setTaskName('');
    setSelectedDays([]);
    refreshScoreboard();
    refreshChildSchedule();
  };

  const handleGiveBonus = (e: React.FormEvent) => {
    e.preventDefault();
    fetch('/api/parent/bonus', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: selectedKid, points: bonusPoints })
    })
    .then(res => res.text())
    .then(message => {
      alert(message);
      refreshScoreboard();
    });
  };

  const handleManualBackup = () => {
    fetch('/api/parent/backup', { method: 'POST' }).then(res => res.text()).then(msg => alert(msg));
  };

  // Add this state hook at the top of ParentDashboard
  const [pendingApprovals, setPendingApprovals] = useState<any[]>([]);

 // 1. Add a state tracker for custom point inputs at the top of ParentDashboard
 const [pointsOverrides, setPointsOverrides] = useState<Record<number, number>>({});

 // 2. Update the fetch callback to pre-populate our inputs with their default values
 const fetchApprovals = () => {
   fetch('/api/parent/pending-approvals')
     .then(res => res.json())
     .then(data => {
       setPendingApprovals(data);

       // Map initial point allocations into our editable form state memory
       const initialPoints: Record<number, number> = {};
       data.forEach((req: any) => {
         initialPoints[req.completionId] = req.points;
       });
       setPointsOverrides(initialPoints);
     });
 };

  useEffect(() => {
    fetchApprovals();
  }, []);

 const handleApprove = (completionId: number) => {
   const finalPoints = pointsOverrides[completionId];

   fetch(`/api/parent/approve/${completionId}`, {
     method: 'POST',
     headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({ overridePoints: finalPoints })
   })
   .then(res => {
     if (res.ok) {
       alert("Quest verified with adjusted points!");
       fetchApprovals();    // Clear out item card
       refreshScoreboard(); // Update parent scoreboard panel totals
     }
   });
 };

 // 4. Update the input change listener helper function
 const handleOverrideChange = (completionId: number, val: number) => {
   setPointsOverrides({
     ...pointsOverrides,
     [completionId]: val
   });
 };

  return (
    <div className="dashboard parent-zone">
      <h2>🛠️ Parents Command Center</h2>

      {/* 1. Scoreboard Panel */}
      <div className="parent-scoreboard">
        <div className="scoreboard-header">
          <h3>📊 Current Family Standings</h3>
          <button type="button" className="backup-action-btn" onClick={handleManualBackup}>💾 Save Game State</button>
        </div>
        <div className="scoreboard-grid">
          {kids.map(k => (
            <div key={k.id} className={`score-card-mini ${selectedKid === k.username ? 'active-focus' : ''}`}>
              <span className="kid-name">👦 {k.username.toUpperCase()}</span>
              <span className="kid-points-total">💎 {k.balancePoints} Total Points</span>
            </div>
          ))}
        </div>
       {pendingApprovals.length > 0 && (
         <div className="approval-queue-box">
           <h3>🔔 Quests Awaiting Approval ({pendingApprovals.length})</h3>
           <div className="approval-list">
             {pendingApprovals.map(req => (
               <div key={req.completionId} className="approval-card">
                 <div className="approval-info-side">
                   <strong>👦 {req.kid.toUpperCase()}</strong> claimed completion for:
                   <span className="task-review-quote"> "{req.taskName}"</span>
                 </div>

                 <div className="approval-action-side">
                   {/* Numeric input to dynamically scale the point value allocation */}
                   <label className="override-input-label">
                     Points:
                     <input
                       type="number"
                       value={pointsOverrides[req.completionId] ?? req.points}
                       onChange={(e) => handleOverrideChange(req.completionId, Number(e.target.value))}
                       className="points-override-field"
                       min="0"
                     />
                   </label>

                   <button
                     className="approve-action-btn"
                     onClick={() => handleApprove(req.completionId)}
                   >
                     ✅ Verify & Pay
                   </button>
                 </div>
               </div>
             ))}
           </div>
         </div>
       )}
      </div>

      {/* 2. Global Dropdown Selection Banner */}
      <div className="global-kid-selector">
        <label htmlFor="kid-select">🎯 Managing Quests For:</label>
        <select id="kid-select" value={selectedKid} onChange={e => setSelectedKid(e.target.value)} className="styled-global-select">
          {/* NEW OPTION AT THE TOP */}
          <option value="ALL_KIDS">🌟 ALL KIDS (Mass Deploy) 🌟</option>
          {kids.map(k => <option key={k.id} value={k.username}>{k.username.toUpperCase()}</option>)}
        </select>
      </div>

      {/* 3. Conditional Weekly Calendar View (Only shows if a specific single child is selected) */}
      {selectedKid !== 'ALL_KIDS' ? (
        <div className="weekly-overview-container">
          <h3>📅 {selectedKid.toUpperCase()}'s Weekly Adventure Track</h3>
          <div className="weekly-calendar-grid">
            {DAYS_OF_WEEK.map(day => {
              const daysTasks = childChores.filter(chore => chore.dayOfWeek === day && !chore.rewardItem);
              return (
                <div key={day} className="calendar-column">
                  <h4>{day}</h4>
                  <div className="calendar-task-list">
                    {daysTasks.length > 0 ? (
                      daysTasks.map(task => (
                        <div key={task.id} className={`mini-task-item ${task.completed ? 'task-done' : ''}`}>
                          <span className="task-text">{task.taskName}</span>
                          <span className="task-pts">💎{task.points}</span>
                        </div>
                      ))
                    ) : <span className="no-tasks-text">Rest Day 🛌</span>}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ) : (
        <div className="mass-deploy-alert-banner">
          📢 <strong>Mass Deploy Active:</strong> Any tasks or rewards configured below will be added to <strong>every child's profile</strong> simultaneously!
        </div>
      )}

      {/* 4. Deployment Workbench */}
      <form onSubmit={handleSubmit} className="setup-form">
        <h3>📜 Deploy Tasks & Rewards</h3>

        <label>
          Item/Quest Type:
          <select onChange={(e) => setIsReward(e.target.value === 'true')}>
            <option value="false">Assign Chore Task</option>
            <option value="true">Create Redemption Prize</option>
          </select>
        </label>

        <label>
          Name:
          <input type="text" value={taskName} onChange={e => setTaskName(e.target.value)} placeholder="e.g. Brush teeth" required />
        </label>

        {!isReward && (
          <div className="days-selection-container">
            <span className="section-label">Select Target Days:</span>
            <div className="checkbox-grid">
              <label className="checkbox-card all-days-card">
                <input type="checkbox" checked={selectedDays.length === DAYS_OF_WEEK.length} onChange={handleAllDaysChange} />
                📅 <strong>ALL DAYS</strong>
              </label>
              {DAYS_OF_WEEK.map(day => (
                <label key={day} className="checkbox-card">
                  <input type="checkbox" checked={selectedDays.includes(day)} onChange={() => handleDayChange(day)} />
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

      {/* 5. Instant Bonus Form (Only accessible if a specific child is targeted) */}
      {selectedKid !== 'ALL_KIDS' && (
        <div className="bonus-container">
          <h3>🎁 Award Instant Star Bonus</h3>
          <form onSubmit={handleGiveBonus} className="bonus-form">
            <div className="bonus-inputs">
              <label>Awarding to: <span className="highlight-target">{selectedKid.toUpperCase()}</span></label>
              <input type="number" value={bonusPoints} onChange={e => setBonusPoints(Number(e.target.value))} min="1" className="bonus-input-field" />
            </div>
            <button type="submit" className="bonus-submit-btn">Send Sparkle Points ✨</button>
          </form>
        </div>
      )}
    </div>
  );
}