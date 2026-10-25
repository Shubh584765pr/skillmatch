package `in`.binkgo.app

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.binkgo.app.ui.AppViewModel
import `in`.binkgo.app.ui.JobDetailScreen
import `in`.binkgo.app.ui.JobsScreen
import `in`.binkgo.app.ui.JobsViewModel
import `in`.binkgo.app.ui.LoginScreen
import `in`.binkgo.app.ui.MainTab
import `in`.binkgo.app.ui.ProfileScreen
import `in`.binkgo.app.ui.Session
import `in`.binkgo.app.ui.SkillsScreen
import `in`.binkgo.app.ui.theme.SkillMatchTheme

// AppCompatActivity (not ComponentActivity) so AppCompatDelegate can switch the app language.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillMatchTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

@Composable
private fun App(vm: AppViewModel = viewModel(), jobsVm: JobsViewModel = viewModel()) {
    when (vm.session) {
        Session.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        Session.LoggedOut -> Scaffold { padding -> Box(Modifier.padding(padding)) { LoginScreen(vm) } }
        is Session.LoggedIn -> LoggedInApp(vm, jobsVm)
    }
}

@Composable
private fun LoggedInApp(vm: AppViewModel, jobsVm: JobsViewModel) {
    val openJobId = vm.openJobId
    if (openJobId != null) {
        BackHandler { vm.openJobId = null }
        // The detail screen's TopAppBar handles the status bar; this handles the bottom navigation bar.
        Box(Modifier.navigationBarsPadding()) {
            JobDetailScreen(openJobId, jobsVm, onBack = { vm.openJobId = null })
        }
        return
    }

    // Back from Skills/Profile goes to Jobs; back from Jobs leaves the app.
    BackHandler(enabled = vm.tab != MainTab.JOBS) { vm.tab = MainTab.JOBS }

    Scaffold(
        // Each tab's TopAppBar adds the status bar inset itself, so don't add it twice here.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                listOf(
                    Triple(MainTab.JOBS, Icons.Default.Home, R.string.nav_jobs),
                    Triple(MainTab.SKILLS, Icons.Default.Star, R.string.nav_skills),
                    Triple(MainTab.PROFILE, Icons.Default.Person, R.string.nav_profile),
                ).forEach { (tab, icon, label) ->
                    NavigationBarItem(
                        selected = vm.tab == tab,
                        onClick = { vm.tab = tab },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(stringResource(label)) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (vm.tab) {
                MainTab.JOBS -> JobsScreen(jobsVm, vm.skillsVersion, onOpenJob = { vm.openJobId = it })
                MainTab.SKILLS -> SkillsScreen(vm)
                MainTab.PROFILE -> ProfileScreen(vm)
            }
        }
    }
}
