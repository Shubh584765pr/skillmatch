package `in`.binkgo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.binkgo.app.R
import `in`.binkgo.app.ui.theme.FitColors
import java.time.Duration
import java.time.Instant

@Composable
fun FitBadge(fit: String, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (fit) {
        "excellent" -> Triple(R.string.fit_excellent, FitColors.excellentBg, FitColors.excellentFg)
        "good" -> Triple(R.string.fit_good, FitColors.goodBg, FitColors.goodFg)
        else -> Triple(R.string.fit_stretch, FitColors.stretchBg, FitColors.stretchFg)
    }
    Text(
        text = stringResource(label),
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Small rounded label for a skill name. */
@Composable
fun SkillTag(name: String, color: Color = MaterialTheme.colorScheme.surfaceVariant) {
    Text(
        text = name,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun timeAgo(isoTime: String): String {
    val hours = runCatching { Duration.between(Instant.parse(isoTime), Instant.now()).toHours() }.getOrDefault(0)
    return when {
        hours < 1 -> stringResource(R.string.posted_just_now)
        hours < 24 -> stringResource(R.string.posted_hours, hours.toInt())
        else -> stringResource(R.string.posted_days, (hours / 24).toInt())
    }
}

/** Formats 12.0 as "12" and 3.5 as "3.5". */
fun formatLpa(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

@Composable
fun MessageBox(message: String, onRetry: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onRetry != null) TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
    }
}
