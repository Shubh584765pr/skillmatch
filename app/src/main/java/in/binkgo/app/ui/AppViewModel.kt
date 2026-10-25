package `in`.binkgo.app.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.binkgo.app.R
import `in`.binkgo.app.SkillMatchApp
import `in`.binkgo.app.data.ApiClient
import `in`.binkgo.app.data.ApiError
import `in`.binkgo.app.data.AuthResponse
import `in`.binkgo.app.data.LoginRequest
import `in`.binkgo.app.data.ProfileUpdate
import `in`.binkgo.app.data.RegisterRequest
import `in`.binkgo.app.data.Skill
import `in`.binkgo.app.data.User
import `in`.binkgo.app.data.apiCall
import `in`.binkgo.app.notifications.JobAlertNotifications
import kotlinx.coroutines.launch

sealed interface Session {
    data object Loading : Session
    data object LoggedOut : Session
    data class LoggedIn(val user: User) : Session
}

enum class MainTab { JOBS, SKILLS, PROFILE }

/**
 * App-wide state: who is logged in, which screen is showing, and the user's profile.
 * Lives in a ViewModel so it survives the activity restart that a language change causes.
 */
class AppViewModel : ViewModel() {
    private val store = SkillMatchApp.instance.sessionStore

    var session by mutableStateOf<Session>(Session.Loading)
        private set
    var tab by mutableStateOf(MainTab.JOBS)
    var openJobId by mutableStateOf<Int?>(null)

    var authBusy by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)

    var profileBusy by mutableStateOf(false)
        private set
    var profileError by mutableStateOf<String?>(null)
    var profileSaved by mutableStateOf(false)

    var skillCatalog by mutableStateOf<List<Skill>>(emptyList())
        private set

    /** Bumped whenever the user's skills change, so job lists re-fetch their match/gaps. */
    var skillsVersion by mutableIntStateOf(0)
        private set

    val user: User? get() = (session as? Session.LoggedIn)?.user

    init {
        viewModelScope.launch {
            val token = store.loadToken()
            if (token == null) {
                session = Session.LoggedOut
                return@launch
            }
            ApiClient.token = token
            apiCall { me() }
                .onSuccess { onLoggedIn(it.user) }
                .onFailure { e ->
                    // A rejected token means log in again; anything else (e.g. server down) is shown on the login screen.
                    if ((e as? ApiError)?.status == 401) logout() else {
                        session = Session.LoggedOut
                        authError = e.message
                    }
                }
        }
    }

    fun login(email: String, password: String) = authenticate { login(LoginRequest(email.trim(), password)) }

    fun register(name: String, email: String, password: String) =
        authenticate { register(RegisterRequest(name.trim(), email.trim(), password)) }

    private fun authenticate(call: suspend `in`.binkgo.app.data.SkillMatchApi.() -> AuthResponse) {
        if (authBusy) return
        viewModelScope.launch {
            authBusy = true
            authError = null
            apiCall(call)
                .onSuccess {
                    ApiClient.token = it.token
                    store.saveToken(it.token)
                    onLoggedIn(it.user)
                }
                .onFailure { authError = it.message }
            authBusy = false
        }
    }

    private fun onLoggedIn(user: User) {
        session = Session.LoggedIn(user)
        applyLanguage(user.language)
    }

    fun logout() {
        viewModelScope.launch {
            store.clear()
            ApiClient.token = null
            session = Session.LoggedOut
            tab = MainTab.JOBS
            openJobId = null
        }
    }

    fun saveProfile(update: ProfileUpdate) {
        if (profileBusy) return
        viewModelScope.launch {
            profileBusy = true
            profileError = null
            profileSaved = false
            apiCall { updateMe(update) }
                .onSuccess {
                    session = Session.LoggedIn(it.user)
                    profileSaved = true
                    if (update.skills != null) skillsVersion++
                    applyLanguage(it.user.language)
                    if (it.user.jobAlerts) {
                        val app = SkillMatchApp.instance
                        JobAlertNotifications.show(
                            app,
                            app.getString(R.string.job_alerts_enabled_title),
                            app.getString(R.string.job_alerts_enabled_body),
                        )
                    }
                }
                .onFailure { profileError = it.message }
            profileBusy = false
        }
    }

    fun sendTestJobAlert() {
        val app = SkillMatchApp.instance
        JobAlertNotifications.show(
            app,
            app.getString(R.string.job_alerts_test_title),
            app.getString(R.string.job_alerts_test_body),
            notificationId = 1002,
        )
    }

    fun loadSkillCatalog() {
        if (skillCatalog.isNotEmpty()) return
        viewModelScope.launch {
            apiCall { skills() }.onSuccess { skillCatalog = it.skills }
        }
    }

    /** Switches the UI language ("en" or "hi"). AppCompat recreates the activity to apply it. */
    private fun applyLanguage(language: String) {
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != language) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
        }
    }
}
