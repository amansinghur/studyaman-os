package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AosCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ObsidianSurface,
    borderColor: Color = ObsidianBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var cardModifier = modifier
        .clip(RoundedCornerShape(16.dp))
        .background(backgroundColor)
        .border(1.dp, borderColor, RoundedCornerShape(16.dp))

    if (onClick != null) {
        cardModifier = cardModifier.clickable(onClick = onClick)
    }

    Column(
        modifier = cardModifier.padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun PremiumBadge(
    text: String,
    color: Color,
    textColor: Color = Color.White
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .border(0.5.dp, color, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun CircularProgressRing(
    progress: Float, // 0.0 to 1.0
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = AccentLinear,
    textColor: Color = TextPrimary,
    centerContent: @Composable (() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000)
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background track
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = (size.toPx() - strokeWidth.toPx()) / 2,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Draw active progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                size = Size(size.toPx() - strokeWidth.toPx(), size.toPx() - strokeWidth.toPx()),
                topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        if (centerContent != null) {
            centerContent()
        } else {
            Text(
                text = "${(progress * 100).toInt()}%",
                color = textColor,
                fontSize = (size.value * 0.25f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LineChart(
    scores: List<Float>, // Values (e.g. mock test scores scaled or percentages)
    dates: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = AccentClickUp,
    fillColor: Color = AccentClickUp.copy(alpha = 0.15f),
    maxVal: Float = 100f
) {
    if (scores.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No test history recorded", color = TextSecondary, fontSize = 13.sp)
        }
        return;
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 50f
        val paddingBottom = 60f
        val paddingTop = 20f
        val paddingRight = 20f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw grids
        val gridCount = 4
        for (i in 0..gridCount) {
            val y = paddingTop + (chartHeight * (gridCount - i) / gridCount)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1f
            )
        }

        // Compute points
        val stepX = if (scores.size > 1) chartWidth / (scores.size - 1) else chartWidth
        val points = scores.mapIndexed { idx, score ->
            val x = paddingLeft + (idx * stepX)
            val y = paddingTop + chartHeight - (score / maxVal * chartHeight)
            Offset(x, y)
        }

        // Draw Area Fill under the line
        val fillPath = Path()
        if (points.isNotEmpty()) {
            fillPath.moveTo(points[0].x, paddingTop + chartHeight)
            points.forEach { fillPath.lineTo(it.x, it.y) }
            fillPath.lineTo(points.last().x, paddingTop + chartHeight)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor, Color.Transparent),
                    startY = points.minOf { it.y },
                    endY = paddingTop + chartHeight
                )
            )
        }

        // Draw Line Path
        val strokePath = Path()
        if (points.isNotEmpty()) {
            strokePath.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                strokePath.lineTo(points[i].x, points[i].y)
            }
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Draw points
        points.forEach { pt ->
            drawCircle(
                color = ObsidianBg,
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = lineColor,
                radius = 3.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
fun BarChart(
    values: List<Float>, // Study hours
    labels: List<String>, // Days (Mon, Tue, etc.)
    modifier: Modifier = Modifier,
    barColor: Color = AccentLinear,
    maxVal: Float = 12f
) {
    if (values.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No study tracker data", color = TextSecondary, fontSize = 13.sp)
        }
        return;
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 40f
        val paddingBottom = 50f
        val paddingTop = 20f
        val paddingRight = 10f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw grids
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = paddingTop + (chartHeight * (gridLines - i) / gridLines)
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1f
            )
        }

        val barSpacing = 20f
        val barCount = values.size
        val barWidth = (chartWidth - (barSpacing * (barCount - 1))) / barCount

        values.forEachIndexed { idx, value ->
            val barHeight = (value / maxVal * chartHeight).coerceAtLeast(4f)
            val x = paddingLeft + (idx * (barWidth + barSpacing))
            val y = paddingTop + chartHeight - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw thin inner glow
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight / 2),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
fun SemiGauge(
    progress: Float, // 0.0 to 1.0
    title: String,
    modifier: Modifier = Modifier,
    gaugeColor: Color = AccentTeal
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1200)
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val strokePx = 10.dp.toPx()
            
            val arcSize = Size(width - strokePx, (height * 2) - strokePx)
            val startAngle = 180f
            val sweepAngle = 180f

            // Background Track
            drawArc(
                color = Color.White.copy(alpha = 0.05f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Track
            drawArc(
                color = gaugeColor,
                startAngle = startAngle,
                sweepAngle = animatedProgress * sweepAngle,
                useCenter = false,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
