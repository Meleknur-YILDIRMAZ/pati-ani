package com.patikagunlugu.app.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class ParrotPeekingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var headTiltAngle = 0f
    private var animator: ValueAnimator? = null

    // Base Paints for outlines, borders and accents
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E1C4B") // Elegant deep indigo outline
        style = Paint.Style.STROKE
        strokeWidth = 1.8f * resources.displayMetrics.density
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

    private val irisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD54F") // Realistic gold/yellow iris
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F000000") // Soft drop shadow on the teal ledge
        style = Paint.Style.FILL
    }

    private val clawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4F5D75") // Realistic dark slate grey claws
        style = Paint.Style.FILL
    }

    init {
        startAnimationLoop()
    }

    private fun startAnimationLoop() {
        // Organic loop: starts upright (0f), tilts downwards (-25f) to look at the calendar,
        // holds the curious downward tilt, then returns to upright. Repeats continuously.
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 4200
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                headTiltAngle = when {
                    progress < 0.35f -> 0f // Upright looking forward
                    progress < 0.50f -> {  // Tilts head downwards (negative rotation)
                        val t = (progress - 0.35f) / 0.15f
                        -24f * t
                    }
                    progress < 0.80f -> -24f // Holding the downward tilt curiosly
                    else -> {               // Smoothly returns upright
                        val t = (progress - 0.80f) / 0.20f
                        -24f * (1f - t)
                    }
                }
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val density = resources.displayMetrics.density

        // Dynamic gradients for premium, realistic lighting/shading (Scarlet Macaw colors)
        val bodyGradient = LinearGradient(
            w * 0.35f, h - 55f * density,
            w * 0.60f, h - 8f * density,
            Color.parseColor("#E53935"), // Realistic rich scarlet red
            Color.parseColor("#B71C1C"), // Deep crimson shading
            Shader.TileMode.CLAMP
        )
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = bodyGradient
            style = Paint.Style.FILL
        }

        val wingRedGradient = LinearGradient(
            w * 0.44f, h - 45f * density,
            w * 0.62f, h - 20f * density,
            Color.parseColor("#EF5350"),
            Color.parseColor("#C62828"),
            Shader.TileMode.CLAMP
        )
        val wingRedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = wingRedGradient
            style = Paint.Style.FILL
        }

        val wingYellowGradient = LinearGradient(
            w * 0.46f, h - 38f * density,
            w * 0.62f, h - 22f * density,
            Color.parseColor("#FFEE58"), // Vibrant warm yellow feather band
            Color.parseColor("#F9A825"), // Deep golden shading
            Shader.TileMode.CLAMP
        )
        val wingYellowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = wingYellowGradient
            style = Paint.Style.FILL
        }

        val wingBlueGradient = LinearGradient(
            w * 0.48f, h - 32f * density,
            w * 0.60f, h - 24f * density,
            Color.parseColor("#29B6F6"), // Sleek royal blue wingtips
            Color.parseColor("#0288D1"), // Darker blue shaded feathers
            Shader.TileMode.CLAMP
        )
        val wingBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = wingBlueGradient
            style = Paint.Style.FILL
        }

        val tailGradient = LinearGradient(
            w * 0.55f, h - 35f * density,
            w * 0.85f, h - 10f * density,
            Color.parseColor("#0288D1"), // Blue feathers
            Color.parseColor("#1565C0"), // Deep sapphire tailtip
            Shader.TileMode.CLAMP
        )
        val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = tailGradient
            style = Paint.Style.FILL
        }

        // 1. Draw soft drop shadow on the ledge
        val feetY = h - 6f * density
        canvas.drawOval(RectF(w * 0.38f, feetY - 2f * density, w * 0.60f, feetY + 3f * density), shadowPaint)

        // 2. Draw sleek, realistic Tail
        val tailPath = Path().apply {
            moveTo(w * 0.56f, h - 38f * density)
            quadTo(w * 0.72f, h - 30f * density, w * 0.86f, h - 16f * density) // long elegant tail feathers
            lineTo(w * 0.78f, h - 12f * density)
            quadTo(w * 0.64f, h - 24f * density, w * 0.50f, h - 30f * density)
            close()
        }
        canvas.drawPath(tailPath, tailPaint)
        canvas.drawPath(tailPath, linePaint)

        // Tail accent feather
        val tailInnerPath = Path().apply {
            moveTo(w * 0.54f, h - 35f * density)
            quadTo(w * 0.68f, h - 28f * density, w * 0.80f, h - 15f * density)
            lineTo(w * 0.76f, h - 13f * density)
            quadTo(w * 0.64f, h - 23f * density, w * 0.52f, h - 29f * density)
            close()
        }
        val tailInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B71C1C") // Crimson red tail accent
            style = Paint.Style.FILL
        }
        canvas.drawPath(tailInnerPath, tailInnerPaint)
        canvas.drawPath(tailInnerPath, linePaint)

        // 3. Draw elegant, realistically proportioned Body
        val bodyRect = RectF(w * 0.33f, h - 52f * density, w * 0.58f, h - 8f * density)
        canvas.drawOval(bodyRect, bodyPaint)
        canvas.drawOval(bodyRect, linePaint)

        // 4. Draw sleek overlapping Wing feathers (Macaw pattern)
        // Red feather layer
        val wingPath = Path().apply {
            moveTo(w * 0.45f, h - 44f * density)
            quadTo(w * 0.60f, h - 40f * density, w * 0.64f, h - 22f * density)
            lineTo(w * 0.55f, h - 18f * density)
            quadTo(w * 0.42f, h - 28f * density, w * 0.42f, h - 36f * density)
            close()
        }
        canvas.drawPath(wingPath, wingRedPaint)
        canvas.drawPath(wingPath, linePaint)

        // Yellow band
        val wingYellowPath = Path().apply {
            moveTo(w * 0.48f, h - 39f * density)
            quadTo(w * 0.59f, h - 36f * density, w * 0.62f, h - 24f * density)
            lineTo(w * 0.56f, h - 20f * density)
            quadTo(w * 0.46f, h - 28f * density, w * 0.46f, h - 33f * density)
            close()
        }
        canvas.drawPath(wingYellowPath, wingYellowPaint)
        canvas.drawPath(wingYellowPath, linePaint)

        // Blue wingtip
        val wingBluePath = Path().apply {
            moveTo(w * 0.50f, h - 35f * density)
            quadTo(w * 0.57f, h - 32f * density, w * 0.59f, h - 26f * density)
            lineTo(w * 0.55f, h - 22f * density)
            quadTo(w * 0.48f, h - 27f * density, w * 0.48f, h - 30f * density)
            close()
        }
        canvas.drawPath(wingBluePath, wingBluePaint)
        canvas.drawPath(wingBluePath, linePaint)

        // 5. Draw realistic Feet / Claws gripping the ledge
        // Left foot claws
        canvas.drawRoundRect(RectF(w * 0.41f, feetY - 4f * density, w * 0.45f, feetY + 1f * density), 1.5f * density, 1.5f * density, clawPaint)
        canvas.drawRoundRect(RectF(w * 0.41f, feetY - 4f * density, w * 0.45f, feetY + 1f * density), 1.5f * density, 1.5f * density, linePaint)
        // Right foot claws
        canvas.drawRoundRect(RectF(w * 0.49f, feetY - 4f * density, w * 0.53f, feetY + 1f * density), 1.5f * density, 1.5f * density, clawPaint)
        canvas.drawRoundRect(RectF(w * 0.49f, feetY - 4f * density, w * 0.53f, feetY + 1f * density), 1.5f * density, 1.5f * density, linePaint)

        // 6. Draw Head and Neck (Rotatable for smooth downward tilt animation)
        val px = w * 0.46f
        val py = h - 38f * density

        canvas.save()
        // Tilt head downwards (negative rotation) organically
        canvas.rotate(headTiltAngle, px, py)

        // Head setup (smaller, realistic proportions)
        val hx = px
        val hy = py - 13f * density
        val headRadius = 12.5f * density

        val headGradient = RadialGradient(
            hx - 2f * density, hy - 3f * density,
            18f * density,
            Color.parseColor("#E53935"), // Realistic golden red highlight
            Color.parseColor("#990000"), // Realistic deep crimson shadow
            Shader.TileMode.CLAMP
        )
        val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = headGradient
            style = Paint.Style.FILL
        }

        // Draw fine feathered crest at the back of the head
        val crestPath = Path().apply {
            moveTo(hx - 2f * density, hy - headRadius + 1f * density)
            quadTo(hx - 10f * density, hy - headRadius - 7f * density, hx - 16f * density, hy - headRadius - 1f * density)
            quadTo(hx - 8f * density, hy - headRadius - 1f * density, hx - 1f * density, hy - headRadius + 1f * density)
            close()
        }
        canvas.drawPath(crestPath, headPaint)
        canvas.drawPath(crestPath, linePaint)

        // Draw Head circle
        canvas.drawCircle(hx, hy, headRadius, headPaint)
        canvas.drawCircle(hx, hy, headRadius, linePaint)

        // Realistic large white face skin patch (typical of macaws)
        val facePatch = Path().apply {
            moveTo(hx - 11f * density, hy - 6f * density)
            quadTo(hx + 2f * density, hy - 8f * density, hx + 1f * density, hy + 5f * density)
            quadTo(hx - 4f * density, hy + 9f * density, hx - 10f * density, hy + 3f * density)
            close()
        }
        canvas.drawPath(facePatch, whitePaint)
        canvas.drawPath(facePatch, linePaint)

        // Realistic small round Eye (no cartoon catchlights, just standard biological eye)
        val ex = hx - 5f * density
        val ey = hy - 1f * density
        val eyeRadius = 3.6f * density

        canvas.drawCircle(ex, ey, eyeRadius, linePaint) // Eye boundary
        canvas.drawCircle(ex, ey, eyeRadius - 0.4f * density, whitePaint)
        canvas.drawCircle(ex, ey, 2.5f * density, irisPaint) // Golden iris
        canvas.drawCircle(ex, ey, 1.4f * density, blackPaint) // Dark pupil
        canvas.drawCircle(ex - 0.6f * density, ey - 0.6f * density, 0.4f * density, whitePaint) // Lifelike pin-point catchlight

        // Realistic heavy, hooked Macaw Beak
        // Upper beak (ivory/light cream color, large hook)
        val beakGradient = LinearGradient(
            hx - 20f * density, hy - 4f * density,
            hx - 10f * density, hy + 10f * density,
            Color.parseColor("#FFFDF7"), // Matte off-white
            Color.parseColor("#ECEFF1"), // Soft light grey shading
            Shader.TileMode.CLAMP
        )
        val beakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = beakGradient
            style = Paint.Style.FILL
        }

        val beakPath = Path().apply {
            moveTo(hx - 9f * density, hy - 4f * density)
            quadTo(hx - 21f * density, hy - 1f * density, hx - 23f * density, hy + 10f * density) // sharp hook downwards
            quadTo(hx - 15f * density, hy + 11f * density, hx - 9f * density, hy + 4f * density)
            close()
        }
        canvas.drawPath(beakPath, beakPaint)
        canvas.drawPath(beakPath, linePaint)

        // Dark grey beak tip and lower beak (typical of Ara species)
        val beakTipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#37474F") // Dark charcoal grey
            style = Paint.Style.FILL
        }

        val beakTipPath = Path().apply {
            moveTo(hx - 17f * density, hy + 4f * density)
            quadTo(hx - 21f * density, hy + 3f * density, hx - 23f * density, hy + 10f * density)
            quadTo(hx - 18f * density, hy + 11f * density, hx - 16f * density, hy + 7f * density)
            close()
        }
        canvas.drawPath(beakTipPath, beakTipPaint)
        canvas.drawPath(beakTipPath, linePaint)

        // Lower beak
        val lowerBeakPath = Path().apply {
            moveTo(hx - 10f * density, hy + 4f * density)
            quadTo(hx - 14f * density, hy + 6f * density, hx - 15f * density, hy + 9f * density)
            quadTo(hx - 11f * density, hy + 9f * density, hx - 8f * density, hy + 7f * density)
            close()
        }
        canvas.drawPath(lowerBeakPath, beakTipPaint)
        canvas.drawPath(lowerBeakPath, linePaint)

        canvas.restore()
    }
}
