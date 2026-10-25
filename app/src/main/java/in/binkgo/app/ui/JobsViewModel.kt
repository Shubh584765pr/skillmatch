package `in`.binkgo.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.binkgo.app.data.Job
import `in`.binkgo.app.data.apiCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job as CoroutineJob

enum class JobTab(val apiValue: String) {
    RECOMMENDED("recommended"),
    ALL("all"),
    SAVED("saved"),
    APPLIED("applied"),
}

enum class JobsViewMode { LIST, MAP }

/** How the All Jobs list is arranged after map chips. */
enum class JobsListGrouping { NONE, BY_COMPANY }

private const val SEARCH_DEBOUNCE_MS = 300L

class JobsViewModel : ViewModel() {
    var tab by mutableStateOf(JobTab.RECOMMENDED)
        private set
    var query by mutableStateOf("")
        private set
    var jobs by mutableStateOf<List<Job>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var viewMode by mutableStateOf(JobsViewMode.LIST)
    var listGrouping by mutableStateOf(JobsListGrouping.NONE)
        private set
    var last24HoursOnly by mutableStateOf(false)

    var detail by mutableStateOf<Job?>(null)
        private set
    var detailError by mutableStateOf<String?>(null)
        private set

    private var listRequest: CoroutineJob? = null

    /** Jobs after optional “Last 24 hours” chip filter (client-side). */
    val visibleJobs: List<Job>
        get() {
            if (!last24HoursOnly) return jobs
            val cutoff = System.currentTimeMillis() - 24L * 60 * 60 * 1000
            return jobs.filter { postedAtMillis(it.postedAt) >= cutoff }
        }

    fun selectTab(newTab: JobTab) {
        tab = newTab
        // Map is only for All Jobs; other tabs stay as lists (like iExchange).
        viewMode = if (newTab == JobTab.ALL) JobsViewMode.MAP else JobsViewMode.LIST
        listGrouping = JobsListGrouping.NONE
        // Saved / Applied have no search bar — don't carry over a leftover query.
        if (newTab == JobTab.SAVED || newTab == JobTab.APPLIED) {
            query = ""
        }
        load()
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        listGrouping = JobsListGrouping.NONE
        load(delayMs = SEARCH_DEBOUNCE_MS) // wait for the user to stop typing
    }

    fun toggleLast24Hours() {
        last24HoursOnly = !last24HoursOnly
    }

    /** Map pin tap → list filtered to that city. */
    fun openLocationInList(location: String) {
        listGrouping = JobsListGrouping.NONE
        viewMode = JobsViewMode.LIST
        onQueryChange(location.substringBefore(",").trim())
    }

    /** Map “View jobs” → full job list (keeps current jobs; reloads clean). */
    fun openJobsList() {
        query = ""
        listGrouping = JobsListGrouping.NONE
        viewMode = JobsViewMode.LIST
        load()
    }

    /** Map “View companies” → same jobs, grouped by company name. */
    fun openCompaniesList() {
        query = ""
        listGrouping = JobsListGrouping.BY_COMPANY
        viewMode = JobsViewMode.LIST
        load()
    }

    /** Fetches the list for the current tab + search. A newer call cancels an older one still in flight. */
    fun load(delayMs: Long = 0) {
        listRequest?.cancel()
        listRequest = viewModelScope.launch {
            delay(delayMs)
            loading = true
            apiCall { jobs(tab.apiValue, query.trim()) }
                .onSuccess {
                    jobs = it.jobs
                    error = null
                }
                .onFailure { error = it.message }
            loading = false
        }
    }

    fun openDetail(id: Int) {
        if (detail?.id != id) detail = null // keep showing the same job while it refreshes
        detailError = null
        viewModelScope.launch {
            apiCall { job(id) }
                .onSuccess { detail = it.job }
                .onFailure { detailError = it.message }
        }
    }

    /** Bookmarks or un-bookmarks a job. Updates the UI first, and undoes it if the request fails. */
    fun toggleSave(job: Job) {
        val target = !job.saved
        setSaved(job.id, target)
        viewModelScope.launch {
            apiCall { if (target) saveJob(job.id) else unsaveJob(job.id) }
                .onFailure {
                    setSaved(job.id, !target)
                    error = it.message
                }
        }
    }

    /** Marks or unmarks a job as applied. Optimistic UI; rolls back on failure. */
    fun toggleApply(job: Job) {
        val target = !job.applied
        setApplied(job.id, target)
        viewModelScope.launch {
            apiCall { if (target) applyJob(job.id) else unapplyJob(job.id) }
                .onFailure {
                    setApplied(job.id, !target)
                    error = it.message
                }
                .onSuccess {
                    // If we're on Applied and user un-applies, drop it from the list.
                    if (tab == JobTab.APPLIED && !target) {
                        jobs = jobs.filter { it.id != job.id }
                    }
                }
        }
    }

    private fun setSaved(id: Int, saved: Boolean) {
        jobs = jobs.map { if (it.id == id) it.copy(saved = saved) else it }
        detail = detail?.let { if (it.id == id) it.copy(saved = saved) else it }
    }

    private fun setApplied(id: Int, applied: Boolean) {
        val appliedAt = if (applied) java.time.Instant.now().toString() else null
        jobs = jobs.map { if (it.id == id) it.copy(applied = applied, appliedAt = appliedAt) else it }
        detail = detail?.let { if (it.id == id) it.copy(applied = applied, appliedAt = appliedAt) else it }
    }
}

private fun postedAtMillis(isoTime: String): Long =
    runCatching { java.time.Instant.parse(isoTime).toEpochMilli() }.getOrDefault(0L)
