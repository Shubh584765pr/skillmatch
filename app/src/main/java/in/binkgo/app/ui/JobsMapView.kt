package `in`.binkgo.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.binkgo.app.R
import `in`.binkgo.app.data.Job
import kotlin.math.roundToInt

data class LocationCluster(
    val location: String,
    val count: Int,
    val sampleCompany: String,
)

/**
 * iExchange-style opportunities map (stylized — not Google Maps tiles).
 * Real job counts by city from the API/DB.
 */
@Composable
fun JobsMapView(
    jobs: List<Job>,
    onSelectLocation: (String) -> Unit,
    onViewJobs: () -> Unit,
    onViewCompanies: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clusters = remember(jobs) {
        jobs.groupBy { normalizeLocation(it.location) }
            .map { (loc, list) ->
                LocationCluster(
                    location = loc,
                    count = list.size,
                    sampleCompany = list.groupingBy { it.company }.eachCount().maxByOrNull { it.value }?.key
                        ?: list.first().company,
                )
            }
            .sortedByDescending { it.count }
    }
    val companyCount = remember(jobs) { jobs.map { it.company }.distinct().size }

    Box(modifier.fillMaxSize()) {
        MapBackground()
        MapPins(clusters = clusters, onSelectLocation = onSelectLocation)
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MapBottomChip(
                text = stringResource(R.string.view_companies_count, companyCount),
                onClick = onViewCompanies,
            )
            MapBottomChip(
                text = stringResource(R.string.view_jobs_count, jobs.size),
                onClick = onViewJobs,
            )
        }
    }
}

@Composable
private fun MapBottomChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.White,
        shadowElevation = 6.dp,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun MapBackground() {
    val park = Color(0xFFC8E6C9)
    val water = Color(0xFFBBDEFB)
    val road = Color(0xFFFFFFFF)
    val land = Color(0xFFF5F0E6)
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(land, Color(0xFFEEF5EE), water.copy(alpha = 0.35f))),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // Soft parks
            drawCircle(park.copy(alpha = 0.55f), radius = size.minDimension * 0.18f, center = Offset(size.width * 0.25f, size.height * 0.35f))
            drawCircle(park.copy(alpha = 0.4f), radius = size.minDimension * 0.12f, center = Offset(size.width * 0.7f, size.height * 0.55f))
            // Water blob
            drawCircle(water.copy(alpha = 0.5f), radius = size.minDimension * 0.2f, center = Offset(size.width * 0.85f, size.height * 0.2f))
            // Road grid
            val stepX = size.width / 8f
            val stepY = size.height / 10f
            for (i in 1 until 8) {
                drawLine(road.copy(alpha = 0.85f), Offset(stepX * i, 0f), Offset(stepX * i, size.height), strokeWidth = 4f)
            }
            for (j in 1 until 10) {
                drawLine(road.copy(alpha = 0.85f), Offset(0f, stepY * j), Offset(size.width, stepY * j), strokeWidth = 3.5f)
            }
            // Diagonal artery
            drawLine(
                Color(0xFFE0E0E0),
                Offset(0f, size.height * 0.2f),
                Offset(size.width, size.height * 0.75f),
                strokeWidth = 10f,
            )
        }
    }
}

@Composable
private fun MapPins(
    clusters: List<LocationCluster>,
    onSelectLocation: (String) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val heightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        clusters.forEachIndexed { index, cluster ->
            val (fx, fy) = pinFraction(cluster.location, index)
            val x = (fx * widthPx).roundToInt()
            val y = (fy * heightPx).roundToInt()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset { IntOffset(x - 48, y - 52) }
                    .clickable { onSelectLocation(cluster.location) },
            ) {
                JobCountPin(count = cluster.count)
                Spacer(Modifier.height(4.dp))
                Text(
                    cluster.sampleCompany,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
                Text(
                    cluster.location.substringBefore(","),
                    fontSize = 10.sp,
                    color = Color(0xFF424242),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun JobCountPin(count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .border(1.dp, Color(0xFF90CAF9), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Place, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        // Little pin tip under the bubble (iExchange-style)
        Canvas(Modifier.size(12.dp, 8.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path, Color.White)
            drawPath(path, Color(0xFF90CAF9), style = Stroke(width = 2f))
        }
    }
}

private fun pinFraction(location: String, index: Int): Pair<Float, Float> {
    // Prefer recognizable city layout for India metros (demo-friendly).
    val known = mapOf(
        "Bengaluru, Karnataka" to (0.55f to 0.48f),
        "Hyderabad, Telangana" to (0.48f to 0.62f),
        "Chennai, Tamil Nadu" to (0.62f to 0.72f),
        "Mumbai, Maharashtra" to (0.22f to 0.42f),
        "Pune, Maharashtra" to (0.28f to 0.55f),
        "Delhi, NCR" to (0.40f to 0.18f),
        "Gurugram, Haryana" to (0.38f to 0.26f),
        "Remote" to (0.78f to 0.22f),
    )
    known[location]?.let { return it }
    val h = location.hashCode()
    val x = 0.15f + ((h ushr 8) % 65) / 100f
    val y = 0.18f + ((h ushr 16) % 55) / 100f + (index % 3) * 0.03f
    return x.coerceIn(0.1f, 0.8f) to y.coerceIn(0.12f, 0.75f)
}

private fun normalizeLocation(raw: String): String {
    val t = raw.trim()
    return when {
        t.equals("Bengaluru", ignoreCase = true) || t.equals("Bangalore", ignoreCase = true) ->
            "Bengaluru, Karnataka"
        else -> t
    }
}
