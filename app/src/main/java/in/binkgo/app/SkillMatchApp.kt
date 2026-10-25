package `in`.binkgo.app

import android.app.Application
import `in`.binkgo.app.data.SessionStore
import `in`.binkgo.app.notifications.JobAlertNotifications

class SkillMatchApp : Application() {
    lateinit var sessionStore: SessionStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        sessionStore = SessionStore(this)
        JobAlertNotifications.ensureChannel(this)
    }

    companion object {
        lateinit var instance: SkillMatchApp
            private set
    }
}
