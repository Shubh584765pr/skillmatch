package `in`.binkgo.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.binkgo.app.R
import `in`.binkgo.app.data.ProfileUpdate
import `in`.binkgo.app.data.User
import `in`.binkgo.app.notifications.JobAlertNotifications

private val EXPERIENCE_OPTIONS = listOf(
    "fresher" to R.string.exp_fresher,
    "junior" to R.string.exp_junior,
    "mid" to R.string.exp_mid,
    "senior" to R.string.exp_senior,
)

private val LANGUAGE_OPTIONS = listOf("en" to R.string.lang_en, "hi" to R.string.lang_hi)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: AppViewModel) {
    val user = vm.user ?: return
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.nav_profile), fontWeight = FontWeight.SemiBold) })
        ProfileForm(user, vm)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileForm(user: User, vm: AppViewModel) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* user may deny; Save / test button still work when granted later */ }

    fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !JobAlertNotifications.hasPermission(context)
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Local edits; nothing is sent until "Save changes". Keyed on user.id so a new login starts fresh.
    var name by rememberSaveable(user.id) { mutableStateOf(user.name) }
    var location by rememberSaveable(user.id) { mutableStateOf(user.location.orEmpty()) }
    var experience by rememberSaveable(user.id) { mutableStateOf(user.experienceLevel) }
    var language by rememberSaveable(user.id) { mutableStateOf(user.language) }
    var jobAlerts by rememberSaveable(user.id) { mutableStateOf(user.jobAlerts) }

    val changed = name.trim() != user.name || location.trim() != user.location.orEmpty() ||
        experience != user.experienceLevel || language != user.language || jobAlerts != user.jobAlerts

    // Hide the old "saved"/error message as soon as the user edits again.
    LaunchedEffect(name, location, experience, language, jobAlerts) {
        if (changed) {
            vm.profileSaved = false
            vm.profileError = null
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Box(
                Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    user.name.take(1).uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(Modifier.padding(start = 16.dp)) {
                Text(user.name, style = MaterialTheme.typography.titleLarge)
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text(stringResource(R.string.location)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Label(stringResource(R.string.experience_level))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EXPERIENCE_OPTIONS.forEach { (value, label) ->
                FilterChip(selected = experience == value, onClick = { experience = value }, label = { Text(stringResource(label)) })
            }
        }

        Label(stringResource(R.string.language))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LANGUAGE_OPTIONS.forEach { (value, label) ->
                FilterChip(selected = language == value, onClick = { language = value }, label = { Text(stringResource(label)) })
            }
        }

        Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.job_alerts), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.job_alerts_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = jobAlerts,
                onCheckedChange = { on ->
                    jobAlerts = on
                    if (on) ensureNotificationPermission()
                },
            )
        }

        Spacer(Modifier.height(24.dp))
        vm.profileError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (vm.profileSaved && !changed) Text(stringResource(R.string.profile_saved), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (jobAlerts) ensureNotificationPermission()
                vm.saveProfile(
                    ProfileUpdate(
                        name = name.trim(),
                        location = location.trim(), // empty clears it on the server
                        experienceLevel = experience,
                        language = language,
                        jobAlerts = jobAlerts,
                    ),
                )
            },
            enabled = changed && name.isNotBlank() && !vm.profileBusy,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            if (vm.profileBusy) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.save_changes))
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = vm::logout, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(stringResource(R.string.logout))
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 20.dp, bottom = 4.dp))
}
