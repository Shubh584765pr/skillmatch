package `in`.binkgo.app.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.binkgo.app.R
import `in`.binkgo.app.data.Job
import `in`.binkgo.app.ui.theme.FitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(jobId: Int, vm: JobsViewModel, onBack: () -> Unit) {
    LaunchedEffect(jobId) { vm.openDetail(jobId) }
    val job = vm.detail?.takeIf { it.id == jobId }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.job_details)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            },
            actions = { if (job != null) SaveButton(job.saved) { vm.toggleSave(job) } },
        )
        val error = vm.detailError
        when {
            job != null -> JobDetailContent(job, onToggleApply = { vm.toggleApply(job) })
            error != null -> MessageBox(error, onRetry = { vm.openDetail(jobId) })
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JobDetailContent(job: Job, onToggleApply: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(job.company, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        FitBadge(job.fit)

        Spacer(Modifier.height(16.dp))
        InfoRow(Icons.Default.LocationOn, "${job.location} · ${job.workMode}")
        InfoRow(Icons.Default.Info, "${job.employmentType} · ${stringResource(R.string.experience_years, job.experienceMin, job.experienceMax)}")
        if (job.salaryMinLpa != null && job.salaryMaxLpa != null) {
            InfoRow(Icons.Default.Star, stringResource(R.string.salary_lpa, formatLpa(job.salaryMinLpa), formatLpa(job.salaryMaxLpa)))
        }
        InfoRow(Icons.Default.DateRange, timeAgo(job.postedAt))

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        Text(stringResource(R.string.match_score, job.matchScore), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { job.matchScore / 100f },
            color = if (job.fit == "stretch") FitColors.goodFg else FitColors.match,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )

        if (job.matched.isNotEmpty()) {
            SectionTitle(stringResource(R.string.skills_you_have))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                job.matched.forEach { SkillTag(it, FitColors.excellentBg) }
            }
        }
        if (job.gaps.isNotEmpty()) {
            SectionTitle(stringResource(R.string.skills_to_learn))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                job.gaps.forEach { SkillTag(it, FitColors.goodBg) }
            }
        }

        SectionTitle(stringResource(R.string.about_role))
        Text(job.description.orEmpty(), style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(24.dp))
        if (job.applied) {
            OutlinedButton(
                onClick = onToggleApply,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(stringResource(R.string.withdraw_application))
            }
        } else {
            Button(
                onClick = onToggleApply,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(stringResource(R.string.apply_job))
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}
