package com.studyasist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale

/**
 * Activity heatmap: grid of cells, one per day over the last N weeks.
 * Color intensity reflects number of attempts that day.
 *
 * @param activityByDay Map of day (ms at midnight) -> attempt count
 * @param weeks Number of weeks to display (default 12)
 * @param color Base color for filled cells
 * @param modifier Modifier for the composable
 */
@Composable
fun ActivityHeatmap(
    activityByDay: Map<Long, Int>,
    weeks: Int = 12,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (activityByDay.isEmpty()) return

    val cal = Calendar.getInstance(Locale.getDefault())
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val today = cal.timeInMillis
    val dayMs = 24 * 60 * 60 * 1000L
    val totalDays = weeks * 7
    val maxCount = activityByDay.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    val cellData = (0 until totalDays).map { offset ->
        val dayStart = today - (totalDays - 1 - offset) * dayMs
        val count = activityByDay[dayStart] ?: 0
        count
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(com.studyasist.R.string.activity_last_weeks_format, weeks),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(cellData) { count ->
                val alpha = if (count == 0) 0.08f else (0.3f + 0.7f * (count.toFloat() / maxCount).coerceIn(0f, 1f))
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(color.copy(alpha = alpha))
                )
            }
        }
    }
}
