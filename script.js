const API_URL = "https://www.thesportsdb.com/api/v1/json/3/searchevents.php?e=Arsenal_vs_Chelsea&s=2016-2017";

document.addEventListener("DOMContentLoaded", () => {
  fetchEvents();
});

async function fetchEvents() {
  const loader = document.getElementById("loader");
  const grid = document.getElementById("events-grid");
  const statsOverview = document.getElementById("stats-overview");
  
  try {
    const response = await fetch(API_URL);
    if (!response.ok) throw new Error("Network response was not ok");
    
    const data = await response.json();
    loader.style.display = "none";
    
    if (data.event && data.event.length > 0) {
      renderEvents(data.event, grid);
      renderStats(data.event, statsOverview);
    } else {
      grid.innerHTML = `<p style="text-align: center; color: var(--text-secondary); grid-column: 1/-1; font-size: 1.2rem;">No events found for this filter.</p>`;
    }
  } catch (error) {
    loader.style.display = "none";
    grid.innerHTML = `<p style="text-align: center; color: #ef4444; grid-column: 1/-1; font-size: 1.2rem;">Error loading events: ${error.message}</p>`;
  }
}

function renderStats(events, container) {
  const totalMatches = events.length;
  const totalGoals = events.reduce((sum, evt) => sum + (parseInt(evt.intHomeScore) || 0) + (parseInt(evt.intAwayScore) || 0), 0);
  
  container.innerHTML = `
    <div class="stat-box">
      <span class="stat-value">${totalMatches}</span>
      <span class="stat-label">Matches</span>
    </div>
    <div class="stat-box">
      <span class="stat-value">${totalGoals}</span>
      <span class="stat-label">Goals</span>
    </div>
  `;
}

function formatDate(dateStr) {
  if (!dateStr) return "TBD";
  const options = { year: 'numeric', month: 'short', day: 'numeric' };
  return new Date(dateStr).toLocaleDateString('en-US', options);
}

function renderEvents(events, container) {
  container.innerHTML = "";
  
  events.forEach((event, index) => {
    const homeScore = parseInt(event.intHomeScore);
    const awayScore = parseInt(event.intAwayScore);
    
    const isHomeWinner = homeScore > awayScore;
    const isAwayWinner = awayScore > homeScore;
    
    const delay = index * 0.15; // staggered animation
    
    const badgeHtml = event.strLeagueBadge ? 
      `<img src="${event.strLeagueBadge}" alt="${event.strLeague}" class="league-badge" loading="lazy">` : 
      '<div class="league-badge" style="background: rgba(255,255,255,0.1);"></div>';
      
    const videoBtnHtml = event.strVideo ?
      `<a href="${event.strVideo}" target="_blank" rel="noopener" class="btn-video">Watch Highlights</a>` :
      `<span class="no-video">No video available</span>`;
      
    const spectatorsNum = parseInt(event.intSpectators);
    const spectatorsHtml = (spectatorsNum && spectatorsNum > 0) ?
      `<span class="spectators">
        <svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20" style="margin-right: 4px;">
          <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" />
        </svg>
        ${spectatorsNum.toLocaleString()}
      </span>` :
      `<span class="spectators">
        <svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20" style="margin-right: 4px;">
          <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" />
        </svg>
        N/A
      </span>`;

    const html = `
      <article class="glass-panel event-card" style="animation-delay: ${delay}s">
        <div class="event-header">
          <div class="league-info">
            ${badgeHtml}
            <span class="league-name">${event.strLeague || "Unknown League"}</span>
          </div>
          <div class="event-date">${formatDate(event.dateEvent)}</div>
        </div>
        
        <div class="score-display">
          <div class="team ${isHomeWinner ? 'winner' : ''}">
            <div class="team-name">${event.strHomeTeam}</div>
            <div class="score">${isNaN(homeScore) ? "-" : homeScore}</div>
          </div>
          
          <div class="vs">VS</div>
          
          <div class="team ${isAwayWinner ? 'winner' : ''}">
            <div class="team-name">${event.strAwayTeam}</div>
            <div class="score">${isNaN(awayScore) ? "-" : awayScore}</div>
          </div>
        </div>
        
        <div class="event-footer">
          ${spectatorsHtml}
          ${videoBtnHtml}
        </div>
      </article>
    `;
    
    container.insertAdjacentHTML('beforeend', html);
  });
}
