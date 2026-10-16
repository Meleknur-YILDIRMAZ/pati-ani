package com.patikagunlugu.app.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.sin

class AudioFishProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val trackBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val fishOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#01579B") // Elegant deep blue outline for the fish
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val blackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1C1C1E")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 34f
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // Water bubbles styling
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A0E0F7FA") // Soft translucent watery white
        style = Paint.Style.FILL
    }

    private val bubbleOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D0F4FF")
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
    }

    private var progress = 0.0f // 0.0f to 1.0f
    private var currentPositionText = "00:00"
    private var totalDurationText = "00:00"

    // Bubble state management
    private class FishBubble(
        var x: Float,
        var y: Float,
        var radius: Float,
        var alpha: Int,
        var speedX: Float,
        var speedY: Float,
        var wobbleSpeed: Float,
        var wobblePhase: Float
    )
    private val bubbles = mutableListOf<FishBubble>()

    fun setProgress(progress: Float) {
        this.progress = progress.coerceIn(0.0f, 1.0f)
        invalidate()
    }

    fun setTimes(currentMs: Int, totalMs: Int) {
        currentPositionText = formatTime(currentMs)
        totalDurationText = formatTime(totalMs)
        invalidate()
    }

    private fun formatTime(ms: Int): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val density = resources.displayMetrics.density
        val paddingLeft = 40f
        val paddingRight = 240f // Leave space for time text on the right
        val centerY = h / 2f
        val trackLength = w - paddingLeft - paddingRight

        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

        // -------------------------------------------------------------
        // NEW: Draw flowing fluid waves at the bottom of the card layout!
        // -------------------------------------------------------------
        
        // A. Back Wave (Softer, slower, lighter cyan)
        val wavePath1 = Path()
        val phase1 = (System.currentTimeMillis() * 0.0012f)
        wavePath1.moveTo(0f, h)
        wavePath1.lineTo(0f, centerY + 6f * density)
        val waveSegments = 40
        for (i in 0..waveSegments) {
            val px = (w / waveSegments) * i
            val py = centerY + 10f * density + sin(i * 0.2f + phase1) * 3f * density
            wavePath1.lineTo(px, py)
        }
        wavePath1.lineTo(w, h)
        wavePath1.close()
        
        val backWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#0F1E3A8A") else Color.parseColor("#15B3F0FF")
            style = Paint.Style.FILL
        }
        canvas.drawPath(wavePath1, backWavePaint)

        // B. Front Wave (Slightly deeper, faster, overlapping in opposite direction!)
        val wavePath2 = Path()
        val phase2 = -(System.currentTimeMillis() * 0.0018f)
        wavePath2.moveTo(0f, h)
        wavePath2.lineTo(0f, centerY + 10f * density)
        for (i in 0..waveSegments) {
            val px = (w / waveSegments) * i
            val py = centerY + 14f * density + sin(i * 0.25f + phase2) * 4f * density
            wavePath2.lineTo(px, py)
        }
        wavePath2.lineTo(w, h)
        wavePath2.close()
        
        val frontWavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#1A0EA5E9") else Color.parseColor("#2280DEEA")
            style = Paint.Style.FILL
        }
        canvas.drawPath(wavePath2, frontWavePaint)

        // 1. Draw Wavy Background Track (Underwater stream background)
        trackBackgroundPaint.color = if (isDark) Color.parseColor("#27374D") else Color.parseColor("#E2E8F0")
        val bgPath = Path()
        bgPath.moveTo(paddingLeft, centerY)
        
        val segments = 60
        for (i in 0..segments) {
            val px = paddingLeft + (trackLength / segments) * i
            val py = centerY + 10f * sin((i.toFloat() / segments) * 4f * PI).toFloat()
            bgPath.lineTo(px, py)
        }
        canvas.drawPath(bgPath, trackBackgroundPaint)

        // 2. Draw Active Wavy Track (Glowing water current)
        trackPaint.color = if (isDark) Color.parseColor("#00E5FF") else Color.parseColor("#0284C7")
        val activePath = Path()
        activePath.moveTo(paddingLeft, centerY)
        val activeSegments = (segments * progress).toInt()
        for (i in 0..activeSegments) {
            val px = paddingLeft + (trackLength / segments) * i
            val py = centerY + 10f * sin((i.toFloat() / segments) * 4f * PI).toFloat()
            activePath.lineTo(px, py)
        }
        canvas.drawPath(activePath, trackPaint)

        // Sleek, cute, smaller fish size (32dp)
        val fishSize = 32f * density

        // 3. Fish position alignment on the wave with safe padding bounds
        // This mathematically guarantees the tail (left) and snout (right) are always fully visible and never clipped or overlapped.
        val startX = paddingLeft + fishSize * 0.85f
        val endX = paddingLeft + trackLength - fishSize * 0.5f
        val fishX = startX + progress * (endX - startX)
        val fishProgressOnTrack = (fishX - paddingLeft) / trackLength
        val fishY = centerY + 10f * sin(fishProgressOnTrack * 4f * PI).toFloat()

        // C. Bubble simulation trailing behind the swimming fish with faster vertical rise
        if (Math.random() < 0.42) {
            bubbles.add(
                FishBubble(
                    x = fishX - fishSize * 0.4f,
                    y = fishY + (Math.random().toFloat() - 0.5f) * 4f * density,
                    radius = (1.5f + Math.random().toFloat() * 3.5f) * density,
                    alpha = 245,
                    speedX = -(0.8f + Math.random().toFloat() * 1.2f) * density,
                    speedY = -(1.2f + Math.random().toFloat() * 1.8f) * density, // Bubbles float up faster!
                    wobbleSpeed = 0.05f + Math.random().toFloat() * 0.1f,
                    wobblePhase = Math.random().toFloat() * 2f * PI.toFloat()
                )
            )
        }

        // Draw and update bubbles in the water current with wobble wave
        val timeMs = System.currentTimeMillis()
        val iterator = bubbles.iterator()
        while (iterator.hasNext()) {
            val bubble = iterator.next()
            bubble.x += bubble.speedX
            bubble.y += bubble.speedY
            val wobbleOffset = sin(timeMs * bubble.wobbleSpeed + bubble.wobblePhase) * 1.5f * density
            bubble.alpha -= 6
            if (bubble.alpha <= 0 || bubble.x < paddingLeft || bubble.y < centerY - 20f * density) {
                iterator.remove()
            } else {
                bubblePaint.alpha = bubble.alpha
                bubbleOutlinePaint.alpha = bubble.alpha
                canvas.drawCircle(bubble.x + wobbleOffset, bubble.y, bubble.radius, bubblePaint)
                canvas.drawCircle(bubble.x + wobbleOffset, bubble.y, bubble.radius, bubbleOutlinePaint)
            }
        }

        // D. Draw cute, realistic BLUE Tropical Fish
        val bodyGradient = RadialGradient(
            fishX + fishSize * 0.1f, fishY - fishSize * 0.1f,
            fishSize * 0.5f,
            Color.parseColor("#00E5FF"), // Neon Cyan highlight
            Color.parseColor("#0D47A1"), // Deep royal sapphire blue
            Shader.TileMode.CLAMP
        )
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = bodyGradient
            style = Paint.Style.FILL
        }

        val finGradient = LinearGradient(
            fishX - fishSize * 0.6f, fishY - fishSize * 0.4f,
            fishX - fishSize * 0.3f, fishY + fishSize * 0.4f,
            Color.parseColor("#E000E5FF"), // Bright semi-translucent cyan
            Color.parseColor("#601565C0"), // Soft translucent deep blue
            Shader.TileMode.CLAMP
        )
        val finPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = finGradient
            style = Paint.Style.FILL
        }

        // Dynamic tail wagging based on timeline
        val tailWag = sin(System.currentTimeMillis() * 0.020).toFloat() * (6f * density)

        // I. Draw dorsal fin (sırt yüzgeci)
        val dorsalPath = Path().apply {
            moveTo(fishX - fishSize * 0.2f, fishY - fishSize * 0.22f)
            quadTo(
                fishX - fishSize * 0.35f, fishY - fishSize * 0.52f,
                fishX - fishSize * 0.55f, fishY - fishSize * 0.32f
            )
            lineTo(fishX - fishSize * 0.4f, fishY - fishSize * 0.18f)
            close()
        }
        canvas.drawPath(dorsalPath, finPaint)
        canvas.drawPath(dorsalPath, fishOutlinePaint)

        // II. Draw beautiful, long flowy tail fin (Dynamic tail wagging)
        val tailPath = Path().apply {
            moveTo(fishX - fishSize * 0.38f, fishY)
            quadTo(
                fishX - fishSize * 0.65f, fishY - fishSize * 0.45f + tailWag,
                fishX - fishSize * 0.85f, fishY - fishSize * 0.4f + tailWag
            )
            quadTo(
                fishX - fishSize * 0.72f, fishY + tailWag,
                fishX - fishSize * 0.85f, fishY + fishSize * 0.4f + tailWag
            )
            quadTo(
                fishX - fishSize * 0.65f, fishY + fishSize * 0.45f + tailWag,
                fishX - fishSize * 0.38f, fishY
            )
            close()
        }
        canvas.drawPath(tailPath, finPaint)
        canvas.drawPath(tailPath, fishOutlinePaint)

        // III. Draw chubby fish Body
        val bodyPath = Path().apply {
            // Smooth natural teardrop fish silhouette (swims towards the right)
            moveTo(fishX + fishSize * 0.5f, fishY) // Head/Mouth
            cubicTo(
                fishX + fishSize * 0.4f, fishY - fishSize * 0.36f,
                fishX - fishSize * 0.1f, fishY - fishSize * 0.32f,
                fishX - fishSize * 0.42f, fishY // Narrowing into tail root
            )
            cubicTo(
                fishX - fishSize * 0.1f, fishY + fishSize * 0.32f,
                fishX + fishSize * 0.4f, fishY + fishSize * 0.36f,
                fishX + fishSize * 0.5f, fishY
            )
            close()
        }
        canvas.drawPath(bodyPath, bodyPaint)
        canvas.drawPath(bodyPath, fishOutlinePaint)

        // IV. Draw cute pectoral side fin (göğüs yüzgeci)
        val pectoralPath = Path().apply {
            moveTo(fishX - fishSize * 0.05f, fishY + fishSize * 0.08f)
            quadTo(
                fishX - fishSize * 0.25f, fishY + fishSize * 0.32f,
                fishX - fishSize * 0.32f, fishY + fishSize * 0.22f
            )
            quadTo(
                fishX - fishSize * 0.2f, fishY + fishSize * 0.05f,
                fishX - fishSize * 0.05f, fishY + fishSize * 0.08f
            )
            close()
        }
        canvas.drawPath(pectoralPath, finPaint)
        canvas.drawPath(pectoralPath, fishOutlinePaint)

        // V. Big cute shiny cartoon-biological Eye
        val ex = fishX + fishSize * 0.24f
        val ey = fishY - fishSize * 0.08f
        val eyeRadius = fishSize * 0.09f

        canvas.drawCircle(ex, ey, eyeRadius, blackPaint) // Eye outer border
        canvas.drawCircle(ex, ey, eyeRadius - 0.3f * density, whitePaint)
        canvas.drawCircle(ex, ey, eyeRadius - 0.7f * density, blackPaint) // Pupil
        canvas.drawCircle(ex - 0.4f * density, ey - 0.4f * density, 0.4f * density, whitePaint) // Highlight sparkle

        // 4. Draw Time Text on the right
        textPaint.color = if (isDark) Color.parseColor("#94A3B8") else Color.parseColor("#64748B")
        val timeStr = "$currentPositionText / $totalDurationText"
        canvas.drawText(timeStr, w - 10f, centerY + 10f, textPaint)

        // Continuous invalidation keeps the tail wagging and waves/bubbles floating dynamically
        postInvalidateOnAnimation()
    }
}
