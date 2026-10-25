package `in`.binkgo.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.binkgo.app.R
import `in`.binkgo.app.data.ProfileUpdate
import `in`.binkgo.app.data.SkillLevel
import `in`.binkgo.app.data.UserSkill

/** Skill levels from highest to lowest, with their label. */
private val LEVELS = listOf(
    4 to R.string.level_expert,
    3 to R.string.level_advanced,
    2 to R.string.level_proficient,
    1 to R.string.level_beginner,
)

private val StarColor = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillsScreen(vm: AppViewModel) {
    val skills = vm.user?.skills.orEmpty()
    var showAdd by remember { mutableStateOf(false) }

    // Every change saves the full skill list; the server replaces the old one.
    fun save(newSkills: List<UserSkill>) =
        vm.saveProfile(ProfileUpdate(skills = newSkills.map { SkillLevel(it.id, it.level) }))

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // the outer screen already handles system bars
        topBar = { TopAppBar(title = { Text(stringResource(R.string.my_skills, skills.size), fontWeight = FontWeight.SemiBold) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_skill)) },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                    if (vm.profileBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                Text(
                    vm.profileError ?: stringResource(R.string.skills_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (vm.profileError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(LEVELS, key = { it.first }) { (level, label) ->
                val inLevel = skills.filter { it.level == level }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${stringResource(label)} (${inLevel.size})", style = MaterialTheme.typography.titleMedium)
                        Row(Modifier.padding(start = 8.dp)) {
                            repeat(4) { i ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i < level) StarColor else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                    FlowRow(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (inLevel.isEmpty()) {
                            Text(stringResource(R.string.no_skills_in_level), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        inLevel.forEach { skill ->
                            SkillChip(
                                skill = skill,
                                onLevelChange = { newLevel -> save(skills.map { if (it.id == skill.id) it.copy(level = newLevel) else it }) },
                                onRemove = { save(skills.filter { it.id != skill.id }) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        LaunchedEffect(Unit) { vm.loadSkillCatalog() }
        AddSkillDialog(
            available = vm.skillCatalog.filter { c -> skills.none { it.id == c.id } },
            onPick = { skill ->
                showAdd = false
                save(skills + UserSkill(skill.id, skill.name, level = 2))
            },
            onDismiss = { showAdd = false },
        )
    }
}

@Composable
private fun SkillChip(skill: UserSkill, onLevelChange: (Int) -> Unit, onRemove: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { menuOpen = true },
            label = { Text(skill.name) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) },
        )
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            LEVELS.forEach { (level, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(label),
                            fontWeight = if (level == skill.level) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        menuOpen = false
                        if (level != skill.level) onLevelChange(level)
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.remove), color = MaterialTheme.colorScheme.error) },
                onClick = {
                    menuOpen = false
                    onRemove()
                },
            )
        }
    }
}

@Composable
private fun AddSkillDialog(
    available: List<`in`.binkgo.app.data.Skill>,
    onPick: (`in`.binkgo.app.data.Skill) -> Unit,
    onDismiss: () -> Unit,
) {
    var search by remember { mutableStateOf("") }
    val matches = available.filter { it.name.contains(search.trim(), ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_skill)) },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text(stringResource(R.string.search_skills)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(Modifier.heightIn(max = 320.dp).padding(top = 8.dp)) {
                    items(matches, key = { it.id }) { skill ->
                        Text(
                            skill.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(skill) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
