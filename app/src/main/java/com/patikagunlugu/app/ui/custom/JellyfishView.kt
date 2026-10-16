package com.patikagunlugu.app.ui.custom

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

class JellyfishView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val jellyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tentaclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val jellyPath = Path()
    private val tentaclePath = Path()
    private val pathHelper = Path()
    
    // Bubble particles
    private class Bubble(var x: Float, var y: Float, var radius: Float, var speed: Float, var alpha: Int)
    private val bubbles = ArrayList<Bubble>()
    
    // Background retro video projector icons (Left to Right)
    private class FloatingProjector(var x: Float, var y: Float, var size: Float, var speed: Float, var alpha: Int)
    private val projectors = ArrayList<FloatingProjector>()
    
    private val random = Random()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // 1. Draw Watery Gradient Background
        val bgGrad = if (isDark) {
            LinearGradient(0f, 0f, 0f, h,
                Color.parseColor("#0F172A"), // Very deep blue-grey
                Color.parseColor("#1E1B4B"), // Midnight blue/purple
                Shader.TileMode.CLAMP
            )
        } else {
            LinearGradient(0f, 0f, 0f, h,
                Color.parseColor("#E0F2FE"), // Light blue
                Color.parseColor("#E0E7FF"), // Light lavender
                Shader.TileMode.CLAMP
            )
        }
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        // 2. Robust Dynamic Initialization (Guaranteed to execute when dimensions are valid!)
        if (bubbles.isEmpty()) {
            for (i in 0 until 12) {
                bubbles.add(
                    Bubble(
                        x = random.nextFloat() * w,
                        y = random.nextFloat() * h,
                        radius = 3f + random.nextFloat() * 5f,
                        speed = 0.5f + random.nextFloat() * 1.2f,
                        alpha = 40 + random.nextInt(60)
                    )
                )
            }
        }

        if (projectors.isEmpty()) {
            // Initialize 3 background floating retro video projectors (Left to Right)
            for (i in 0 until 3) {
                projectors.add(
                    FloatingProjector(
                        x = random.nextFloat() * w,
                        y = 20f + random.nextFloat() * (h - 40f),
                        size = 35f + random.nextFloat() * 15f,
                        speed = 0.25f + random.nextFloat() * 0.35f, // Yavaş hareket (0.25 - 0.6 pixel/frame)
                        alpha = 20 + random.nextInt(25)            // Soft watermark opacity (8% - 18%)
                    )
                )
            }
        }

        // 3a. Draw & Update Floating Bubbles (Upwards)
        bubblePaint.style = Paint.Style.STROKE
        bubblePaint.strokeWidth = 1.5f
        for (bubble in bubbles) {
            bubble.y -= bubble.speed
            if (bubble.y < -20) {
                bubble.y = h + 20
                bubble.x = random.nextFloat() * w
            }
            
            val bubbleColor = if (isDark) {
                Color.argb(bubble.alpha, 176, 154, 239) // Soft purple glow bubble
            } else {
                Color.argb(bubble.alpha, 124, 58, 237) // Darker violet bubble
            }
            bubblePaint.color = bubbleColor
            canvas.drawCircle(bubble.x, bubble.y, bubble.radius, bubblePaint)
        }

        // 3b. Draw & Update Retro Video Projectors (Left to Right)
        for (proj in projectors) {
            proj.x += proj.speed
            if (proj.x > w + proj.size) {
                proj.x = -proj.size
                proj.y = 20f + random.nextFloat() * (h - 40f)
            }
            drawRetroProjector(canvas, proj.x, proj.y, proj.size, proj.alpha, isDark)
        }

        // 4. Jellyfish Swimming Animation Math
        val t = System.currentTimeMillis() / 1000.0
        // Pulsing cycle (contraction & expansion)
        val pulse = sin(2 * PI * t * 0.4) // 0.4 Hz cycle
        val scaleX = 1.0f + 0.08f * pulse.toFloat()
        val scaleY = 1.0f - 0.12f * pulse.toFloat()
        
        // Vertical translation (floating up and down)
        val swimOffset = (10f * sin(2 * PI * t * 0.2)).toFloat()
        
        val jellyCenterX = w / 2f
        val jellyCenterY = h / 2f - 10f + swimOffset

        // Colors for Jellyfish
        val jellyColor = if (isDark) "#C0A9FF" else "#7C3AED"

        // 5. Draw Tentacles (Wavy sine curves draping down)
        tentaclePaint.style = Paint.Style.STROKE
        tentaclePaint.strokeWidth = 5.5f
        tentaclePaint.strokeCap = Paint.Cap.ROUND
        
        val numTentacles = 3
        val spacing = 36f
        
        for (i in 0 until numTentacles) {
            val startX = jellyCenterX + (i - 1) * spacing * scaleX
            val startY = jellyCenterY + 22f * scaleY
            
            tentaclePath.reset()
            tentaclePath.moveTo(startX, startY)
            
            // Draw a segment-by-segment sine wave for fluid swaying
            val tentacleLength = 85f
            val segments = 8
            for (j in 1..segments) {
                val segY = startY + (tentacleLength / segments) * j * scaleY
                val phaseShift = j * 0.4 + i * 0.5
                val segX = startX + (12f * sin(2 * PI * t * 0.6 + phaseShift)).toFloat() * scaleX
                tentaclePath.lineTo(segX, segY)
            }

            val tColor = if (isDark) {
                Color.argb(160 - i * 30, 192, 169, 255)
            } else {
                Color.argb(160 - i * 30, 124, 58, 237)
            }
            tentaclePaint.color = tColor
            canvas.drawPath(tentaclePath, tentaclePaint)
        }

        // 6. Draw Jellyfish Bell (Dome)
        jellyPaint.style = Paint.Style.FILL
        jellyPaint.color = Color.parseColor(jellyColor)
        
        // Bell shape path
        jellyPath.reset()
        val bellWidth = 50f * scaleX
        val bellHeight = 34f * scaleY
        
        // Top semi-oval dome
        jellyPath.moveTo(jellyCenterX - bellWidth, jellyCenterY)
        jellyPath.cubicTo(
            jellyCenterX - bellWidth, jellyCenterY - bellHeight * 1.5f,
            jellyCenterX + bellWidth, jellyCenterY - bellHeight * 1.5f,
            jellyCenterX + bellWidth, jellyCenterY
        )
        // Bottom wavy edge
        jellyPath.cubicTo(
            jellyCenterX + bellWidth * 0.5f, jellyCenterY + 6f * scaleY,
            jellyCenterX + bellWidth * 0.2f, jellyCenterY - 3f * scaleY,
            jellyCenterX, jellyCenterY
        )
        jellyPath.cubicTo(
            jellyCenterX - bellWidth * 0.2f, jellyCenterY - 3f * scaleY,
            jellyCenterX - bellWidth * 0.5f, jellyCenterY + 6f * scaleY,
            jellyCenterX - bellWidth, jellyCenterY
        )
        
        // Draw outer glow shadow in dark mode for a stunning premium neon effect!
        jellyPaint.setShadowLayer(20f, 0f, 0f, Color.parseColor(jellyColor))
        canvas.drawPath(jellyPath, jellyPaint)
        jellyPaint.clearShadowLayer()

        // Draw inner lighter highlight highlights
        jellyPaint.color = if (isDark) Color.parseColor("#EBE3FF") else Color.parseColor("#C3B5FD")
        canvas.drawCircle(jellyCenterX - bellWidth * 0.3f, jellyCenterY - bellHeight * 0.4f, 6f * scaleX, jellyPaint)
        canvas.drawCircle(jellyCenterX + bellWidth * 0.2f, jellyCenterY - bellHeight * 0.5f, 3.5f * scaleX, jellyPaint)

        // 7. Loop animation at 60fps
        postInvalidateOnAnimation()
    }

    private fun drawRetroProjector(canvas: Canvas, cx: Float, cy: Float, size: Float, alpha: Int, isDark: Boolean) {
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.strokeCap = Paint.Cap.ROUND
        
        val colorVal = if (isDark) {
            Color.argb(alpha, 176, 154, 239) // Soft lavender watermark
        } else {
            Color.argb(alpha, 124, 58, 237) // Soft violet watermark
        }
        backgroundPaint.color = colorVal

        // 1. Two reels on top (circles)
        val reelRadius = size * 0.12f
        val reelY = cy - size * 0.22f
        canvas.drawCircle(cx - size * 0.14f, reelY, reelRadius, backgroundPaint)
        canvas.drawCircle(cx + size * 0.14f, reelY, reelRadius, backgroundPaint)

        // 2. Main body (rounded rectangle)
        val rectWidth = size * 0.55f
        val rectHeight = size * 0.38f
        val rLeft = cx - rectWidth / 2f
        val rTop = cy - rectHeight / 2f
        canvas.drawRoundRect(rLeft, rTop, rLeft + rectWidth, rTop + rectHeight, 6f, 6f, backgroundPaint)

        // 3. Lens on the right (trapezoid pointing right)
        pathHelper.reset()
        val lensLeft = rLeft + rectWidth
        pathHelper.moveTo(lensLeft, cy - rectHeight * 0.25f)
        pathHelper.lineTo(lensLeft + size * 0.16f, cy - rectHeight * 0.55f)
        pathHelper.lineTo(lensLeft + size * 0.16f, cy + rectHeight * 0.55f)
        pathHelper.lineTo(lensLeft, cy + rectHeight * 0.25f)
        pathHelper.close()
        canvas.drawPath(pathHelper, backgroundPaint)

        // 4. Tripod legs at the bottom (lines drawn with stroke)
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = size * 0.06f
        
        val legsTopY = rTop + rectHeight
        // Left leg
        canvas.drawLine(cx - size * 0.08f, legsTopY, cx - size * 0.22f, legsTopY + size * 0.26f, backgroundPaint)
        // Middle leg
        canvas.drawLine(cx, legsTopY, cx, legsTopY + size * 0.30f, backgroundPaint)
        // Right leg
        canvas.drawLine(cx + size * 0.08f, legsTopY, cx + size * 0.22f, legsTopY + size * 0.26f, backgroundPaint)
    }
}
