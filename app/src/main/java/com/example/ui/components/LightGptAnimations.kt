package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MintDark
import com.example.ui.theme.MintGlow
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.MintPrimaryVariant
import kotlin.math.cos
import kotlin.math.sin

/**
 * Bespoke Light GPT Neural Ring Loading Spinner
 * Matches the glowing mint-green neural ring and spark motif from the app logo
 */
@Composable
fun LightGptPulsingLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LightGptPulsing")

    // Outer ring rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Inner core breathing scale
    val coreScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coreScale"
    )

    // Outer ring pulse alpha
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Canvas drawing neural orbital ring with nodes
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) - 3.dp.toPx()

            // Outer dashed / multi-arc glowing ring
            drawCircle(
                color = MintDark.copy(alpha = 0.25f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Dynamic sweeping arc with gradient
            val sweepGradient = Brush.sweepGradient(
                listOf(
                    MintPrimary.copy(alpha = 0.1f),
                    MintPrimaryVariant.copy(alpha = ringAlpha),
                    MintGlow,
                    MintPrimary
                )
            )

            drawArc(
                brush = sweepGradient,
                startAngle = rotation,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Orbiting neural nodes
            val nodeCount = 6
            for (i in 0 until nodeCount) {
                val angleRad = Math.toRadians((rotation + (i * 360f / nodeCount)).toDouble())
                val nodeX = center.x + radius * cos(angleRad).toFloat()
                val nodeY = center.y + radius * sin(angleRad).toFloat()
                drawCircle(
                    color = MintGlow,
                    radius = 2.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
            }
        }

        // Center pulsating geometric core
        Box(
            modifier = Modifier
                .size(size * 0.42f)
                .scale(coreScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(MintGlow, MintPrimary, Color.Transparent)
                    )
                )
        )
    }
}

/**
 * 3 Bouncing Mint Neural Dots
 */
@Composable
fun LightGptTypingDots(
    modifier: Modifier = Modifier,
    dotSize: Dp = 7.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TypingDots")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .scale(if (dot1Offset < -3f) 1.2f else 0.9f)
                .clip(CircleShape)
                .background(MintGlow)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .scale(if (dot2Offset < -3f) 1.2f else 0.9f)
                .clip(CircleShape)
                .background(MintPrimary)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .scale(if (dot3Offset < -3f) 1.2f else 0.9f)
                .clip(CircleShape)
                .background(MintPrimaryVariant)
        )
    }
}

/**
 * Animated Audio Equalizer Bars for Voice Transcription
 */
@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceWaveform")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(340, delayMillis = 50, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(260, delayMillis = 100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 24f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(310, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(290, delayMillis = 75, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h5"
    )

    Row(
        modifier = modifier.height(30.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (isRecording) h.dp else 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(MintGlow, MintPrimary)
                        )
                    )
            )
        }
    }
}

/**
 * Elegant Light GPT Thinking Skeleton with Neural Pulse
 */
@Composable
fun LightGptThinkingCard(
    statusText: String = "Light GPT is synthesizing response...",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ThinkingCard")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .border(
                1.dp,
                MintPrimary.copy(alpha = borderAlpha),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LightGptPulsingLogo(size = 28.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Light GPT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintPrimary
                )
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Processing neural context",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            LightGptTypingDots(dotSize = 6.dp)
        }
    }
}
