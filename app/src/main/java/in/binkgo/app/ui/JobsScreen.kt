package `in`.binkgo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import `in`.binkgo.app.R
import `in`.binkgo.app.data.Job
import `in`.binkgo.app.ui.theme.FitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(vm: JobsViewModel, skillsVersion: Int, onOpenJob: (Int) -> Unit) {
    // Reload whenever this screen is shown (e.g. back from a job) or the user's skills change.
    LaunchedEffect(skillsVersion) { vm.load() }

    val visible = vm.visibleJobs
    val error = vm.error
    val tabs = listOf(
        JobTab.RECOMMENDED to R.string.tab_recommended,
        JobTab.ALL to R.string.tab_all,
        JobTab.SAVED to R.string.tab_saved,
        JobTab.APPLIED to R.string.tab_applied,
    )

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.opportunities), fontWeight = FontWeight.SemiBold) })

        PrimaryScrollableTabRow(selectedTabIndex = vm.tab.ordinal, edgePadding = 16.dp) {
            tabs.forEach { (tab, label) ->
                val text = stringResource(label)
                Tab(
                    selected = vm.tab == tab,
                    onClick = { vm.selectTab(tab) },
                    text = {
                        Text(if (vm.tab == tab && !vm.loading) stringResource(R.string.tab_with_count, text, vm.jobs.size) else text)
                    },
                )
            }
        }

        // Search + filters only on Recommended / All Jobs — Saved & Applied are plain lists.
        if (vm.tab == JobTab.RECOMMENDED || vm.tab == JobTab.ALL) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = vm.query,
                    onValueChange = vm::onQueryChange,
                    placeholder = {
                        Text(
                            stringResource(
                                if (vm.tab == JobTab.ALL && vm.viewMode == JobsViewMode.MAP) {
                                    R.string.search_location_hint
                                } else {
                                    R.string.search_hint
                                },
                            ),
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (vm.tab == JobTab.ALL && vm.viewMode == JobsViewMode.MAP) {
                                Icons.Default.Place
                            } else {
                                Icons.Default.Search
                            },
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        if (vm.query.isNotEmpty()) {
                            IconButton(onClick = { vm.onQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f),
                )
                if (vm.tab == JobTab.ALL) {
                    Spacer(Modifier.width(8.dp))
                    ViewModeToggle(mode = vm.viewMode, onModeChange = { vm.viewMode = it })
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = vm.tab == JobTab.RECOMMENDED,
                    onClick = { vm.selectTab(JobTab.RECOMMENDED) },
                    label = { Text(stringResource(R.string.tab_recommended)) },
                    leadingIcon = { Icon(Icons.Default.Star, null, Modifier.size(16.dp)) },
                )
                FilterChip(
                    selected = vm.last24HoursOnly,
                    onClick = vm::toggleLast24Hours,
                    label = { Text(stringResource(R.string.filter_last_24h)) },
                )
            }
        }

        if (vm.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        when {
            error != null && visible.isEmpty() -> {
                Box(Modifier.weight(1f)) { MessageBox(error, onRetry = { vm.load() }) }
            }
            visible.isEmpty() && !vm.loading -> {
                Box(Modifier.weight(1f)) {
                    MessageBox(
                        when {
                            vm.query.isNotBlank() -> stringResource(R.string.empty_search, vm.query.trim())
                            vm.tab == JobTab.SAVED -> stringResource(R.string.empty_saved)
                            vm.tab == JobTab.APPLIED -> stringResource(R.string.empty_applied)
                            vm.tab == JobTab.RECOMMENDED -> stringResource(R.string.empty_recommended)
                            vm.last24HoursOnly -> stringResource(R.string.empty_last_24h)
                            else -> stringResource(R.string.empty_all)
                        },
                    )
                }
            }
            vm.tab == JobTab.ALL && vm.viewMode == JobsViewMode.MAP -> {
                JobsMapView(
                    jobs = visible,
                    onSelectLocation = vm::openLocationInList,
                    onViewJobs = vm::openJobsList,
                    onViewCompanies = vm::openCompaniesList,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
            else -> {
                // Box+weight gives a fixed viewport; scroll goes on the inner Column only.
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (vm.tab == JobTab.RECOMMENDED) {
                            RecommendedBanner()
                        }
                        if (vm.listGrouping == JobsListGrouping.BY_COMPANY) {
                            Text(
                                stringResource(R.string.companies_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            visible
                                .groupBy { it.company }
                                .toList()
                                .sortedBy { it.first.lowercase() }
                                .forEach { (company, companyJobs) ->
                                    Text(
                                        stringResource(R.string.company_jobs_count, company, companyJobs.size),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                    companyJobs.forEach { job ->
                                        JobCard(job, onClick = { onOpenJob(job.id) }, onToggleSave = { vm.toggleSave(job) })
                                    }
                                }
                        } else {
                            visible.forEach { job ->
                                if (vm.tab == JobTab.RECOMMENDED) {
                                    RecommendedJobCard(job, onClick = { onOpenJob(job.id) }, onToggleSave = { vm.toggleSave(job) })
                                } else {
                                    JobCard(job, onClick = { onOpenJob(job.id) }, onToggleSave = { vm.toggleSave(job) })
                                }
                            }
                        }
                        Text(
                            stringResource(R.string.scroll_end_hint, visible.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggle(mode: JobsViewMode, onModeChange: (JobsViewMode) -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp),
    ) {
        IconButton(
            onClick = { onModeChange(JobsViewMode.LIST) },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (mode == JobsViewMode.LIST) MaterialTheme.colorScheme.surface else Color.Transparent),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.List,
                contentDescription = stringResource(R.string.view_list),
                tint = if (mode == JobsViewMode.LIST) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(
            onClick = { onModeChange(JobsViewMode.MAP) },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (mode == JobsViewMode.MAP) MaterialTheme.colorScheme.surface else Color.Transparent),
        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = stringResource(R.string.view_map),
                tint = if (mode == JobsViewMode.MAP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecommendedBanner() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp),
        )
        Column(Modifier.padding(start = 10.dp)) {
            Text(
                stringResource(R.string.recommended_banner_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                stringResource(R.string.recommended_banner_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** Recommended tab: big match % + skills you already have (skill-first, like iExchange “for you”). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecommendedJobCard(job: Job, onClick: () -> Unit, onToggleSave: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 14.dp, end = 4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(top = 4.dp)) {
                    Text(job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(job.company, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            job.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.match_percent, job.matchScore),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FitColors.match,
                    )
                    FitBadge(job.fit, Modifier.padding(top = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SaveButton(job.saved, onToggleSave)
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.open_job),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { job.matchScore / 100f },
                color = FitColors.match,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp)
                    .height(6.dp),
            )

            Text(
                stringResource(R.string.why_recommended),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, end = 12.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp, end = 12.dp),
            ) {
                job.matched.take(5).forEach { SkillTag(it, FitColors.excellentBg) }
                if (job.matched.size > 5) SkillTag(stringResource(R.string.more_count, job.matched.size - 5))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobCard(job: Job, onClick: () -> Unit, onToggleSave: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 14.dp, end = 4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f).padding(top = 4.dp)) {
                    Text(job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(job.company, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FitBadge(job.fit, Modifier.padding(top = 4.dp))
                SaveButton(job.saved, onToggleSave)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.open_job),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, end = 8.dp),
                )
            }

            Row(Modifier.padding(top = 6.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    job.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                Text(
                    timeAgo(job.appliedAt?.takeIf { job.applied } ?: job.postedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(Modifier.padding(top = 10.dp, end = 12.dp)) {
                MarkedLine(FitColors.match) {
                    Text(
                        "${stringResource(R.string.match)}: ${job.matched.take(3).joinToString(", ").ifEmpty { "—" }}" +
                            if (job.matched.size > 3) "  " + stringResource(R.string.more_count, job.matched.size - 3) else "",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                MarkedLine(if (job.gaps.isEmpty()) FitColors.match else FitColors.gap) {
                    if (job.gaps.isEmpty()) {
                        Text("${stringResource(R.string.gaps)}: ${stringResource(R.string.no_gaps)}", style = MaterialTheme.typography.bodySmall)
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${stringResource(R.string.gaps)}:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                            job.gaps.take(3).forEach { SkillTag(it) }
                            if (job.gaps.size > 3) SkillTag(stringResource(R.string.more_count, job.gaps.size - 3))
                        }
                    }
                }
            }
        }
    }
}

/** A row with a short coloured bar on the left, used for the Match and Gaps lines. */
@Composable
private fun MarkedLine(color: Color, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(16.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        content()
    }
}

@Composable
fun SaveButton(saved: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            if (saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = stringResource(if (saved) R.string.unsave_job else R.string.save_job),
            tint = if (saved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
