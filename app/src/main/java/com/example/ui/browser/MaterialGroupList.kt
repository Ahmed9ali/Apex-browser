package com.example.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmphasizedTypography

/**
 * Standard vertical gap between items inside a Material 3 Expressive Grouped List.
 */
val GroupedListSpacing: Dp = 3.dp

/**
 * Computes the Material 3 Expressive Grouped List corner shape for an item at [index]
 * out of [totalCount]:
 * - Single item: [outerRadius] on all 4 corners.
 * - First item: [outerRadius] on top corners, [innerRadius] on bottom corners.
 * - Middle items: [innerRadius] on all 4 corners.
 * - Last item: [innerRadius] on top corners, [outerRadius] on bottom corners.
 */
fun groupedListItemShape(
    index: Int,
    totalCount: Int,
    outerRadius: Dp = 24.dp,
    innerRadius: Dp = 5.dp
): Shape {
    return when {
        totalCount <= 1 -> RoundedCornerShape(outerRadius)
        index <= 0 -> RoundedCornerShape(
            topStart = outerRadius,
            topEnd = outerRadius,
            bottomStart = innerRadius,
            bottomEnd = innerRadius
        )
        index >= totalCount - 1 -> RoundedCornerShape(
            topStart = innerRadius,
            topEnd = innerRadius,
            bottomStart = outerRadius,
            bottomEnd = outerRadius
        )
        else -> RoundedCornerShape(innerRadius)
    }
}

/**
 * Formatted section header positioned above a Material 3 Grouped List.
 */
@Composable
fun MaterialGroupSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    trailingAction: (@Composable () -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (trailingAction != null) {
                trailingAction()
            }
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Formatted Material 3 Grouped List row item with position-aware corner rounding,
 * tonal surface container, aligned leading badge, title/subtitle hierarchy, and trailing control.
 */
@Composable
fun MaterialGroupListItem(
    index: Int,
    totalCount: Int,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    border: BorderStroke? = null,
    role: Role? = null,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val itemShape = groupedListItemShape(index = index, totalCount = totalCount)
    val clickModifier = if (onClick != null) {
        Modifier
            .clip(itemShape)
            .clickable(role = role, onClick = onClick)
    } else {
        Modifier.clip(itemShape)
    }

    Surface(
        shape = itemShape,
        color = containerColor,
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
            if (leadingContent != null) {
                leadingContent()
                Spacer(modifier = Modifier.width(14.dp))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = EmphasizedTypography.activeTabTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailingContent()
            }
        }
    }
}
