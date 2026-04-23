import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.w3c.dom.HTMLElement

@Serializable
data class Event(
    val idEvent: String? = null,
    val strEvent: String? = null,
    val dateEvent: String? = null,
    val strHomeTeam: String? = null,
    val strAwayTeam: String? = null,
    val intHomeScore: String? = null,
    val intAwayScore: String? = null,
    val strLeague: String? = null,
    val strLeagueBadge: String? = null,
    val strVideo: String? = null,
    val intSpectators: String? = null
)

@Serializable
data class EventResponse(
    val event: List<Event>? = null
)

val json = Json { ignoreUnknownKeys = true }

fun main() {
    window.addEventListener("DOMContentLoaded", {
        MainScope().launch {
            fetchEvents()
        }
    })
}

suspend fun fetchEvents() {
    val loader = document.getElementById("loader") as HTMLElement?
    val grid = document.getElementById("events-grid") as HTMLElement?
    val statsOverview = document.getElementById("stats-overview") as HTMLElement?
    
    val API_URL = "https://www.thesportsdb.com/api/v1/json/3/searchevents.php?e=Arsenal_vs_Chelsea&s=2016-2017"
    
    try {
        val response = window.fetch(API_URL).await()
        val responseText = response.text().await()
        val data = json.decodeFromString<EventResponse>(responseText)
        
        loader?.style?.display = "none"
        
        if (!data.event.isNullOrEmpty()) {
            renderEvents(data.event, grid)
            renderStats(data.event, statsOverview)
        } else {
            grid?.innerHTML = """<p style="text-align: center; color: var(--text-secondary); grid-column: 1/-1; font-size: 1.2rem;">No events found for this filter.</p>"""
        }
    } catch (e: Exception) {
        loader?.style?.display = "none"
        grid?.innerHTML = """<p style="text-align: center; color: #ef4444; grid-column: 1/-1; font-size: 1.2rem;">Error loading events: ${e.message}</p>"""
    }
}

fun renderStats(events: List<Event>, container: HTMLElement?) {
    if (container == null) return
    val totalMatches = events.size
    var totalGoals = 0
    events.forEach { evt ->
        totalGoals += (evt.intHomeScore?.toIntOrNull() ?: 0) + (evt.intAwayScore?.toIntOrNull() ?: 0)
    }
    
    container.innerHTML = """
        <div class="stat-box">
          <span class="stat-value">$totalMatches</span>
          <span class="stat-label">Matches</span>
        </div>
        <div class="stat-box">
          <span class="stat-value">$totalGoals</span>
          <span class="stat-label">Goals</span>
        </div>
    """.trimIndent()
}

fun formatDate(dateStr: String?): String {
    if (dateStr == null) return "TBD"
    val date = kotlin.js.Date(dateStr)
    val options = js("{ year: 'numeric', month: 'short', day: 'numeric' }")
    return date.toLocaleDateString("en-US", options)
}

fun renderEvents(events: List<Event>, container: HTMLElement?) {
    if (container == null) return
    container.innerHTML = ""
    
    events.forEachIndexed { index, event ->
        val homeScoreVal = event.intHomeScore?.toIntOrNull()
        val awayScoreVal = event.intAwayScore?.toIntOrNull()
        
        val isHomeWinner = homeScoreVal != null && awayScoreVal != null && homeScoreVal > awayScoreVal
        val isAwayWinner = homeScoreVal != null && awayScoreVal != null && awayScoreVal > homeScoreVal
        
        val delay = index * 0.15
        
        val badgeHtml = if (!event.strLeagueBadge.isNullOrEmpty()) {
            """<img src="${event.strLeagueBadge}" alt="${event.strLeague ?: ""}" class="league-badge" loading="lazy">"""
        } else {
            """<div class="league-badge" style="background: rgba(255,255,255,0.1);"></div>"""
        }
        
        val videoBtnHtml = if (!event.strVideo.isNullOrEmpty()) {
            """<a href="${event.strVideo}" target="_blank" rel="noopener" class="btn-video">Watch Highlights</a>"""
        } else {
            """<span class="no-video">No video available</span>"""
        }
        
        val spectatorsNum = event.intSpectators?.toIntOrNull()
        val spectatorsHtml = if (spectatorsNum != null && spectatorsNum > 0) {
            """<span class="spectators">
                <svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20" style="margin-right: 4px;">
                  <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" />
                </svg>
                ${js("spectatorsNum.toLocaleString()")}
              </span>"""
        } else {
            """<span class="spectators">
                <svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20" style="margin-right: 4px;">
                  <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" />
                </svg>
                N/A
              </span>"""
        }
        
        val html = """
          <article class="glass-panel event-card" style="animation-delay: ${delay}s">
            <div class="event-header">
              <div class="league-info">
                $badgeHtml
                <span class="league-name">${event.strLeague ?: "Unknown League"}</span>
              </div>
              <div class="event-date">${formatDate(event.dateEvent)}</div>
            </div>
            
            <div class="score-display">
              <div class="team ${if (isHomeWinner) "winner" else ""}">
                <div class="team-name">${event.strHomeTeam ?: ""}</div>
                <div class="score">${homeScoreVal?.toString() ?: "-"}</div>
              </div>
              
              <div class="vs">VS</div>
              
              <div class="team ${if (isAwayWinner) "winner" else ""}">
                <div class="team-name">${event.strAwayTeam ?: ""}</div>
                <div class="score">${awayScoreVal?.toString() ?: "-"}</div>
              </div>
            </div>
            
            <div class="event-footer">
              $spectatorsHtml
              $videoBtnHtml
            </div>
          </article>
        """.trimIndent()
        
        container.insertAdjacentHTML("beforeend", html)
    }
}
