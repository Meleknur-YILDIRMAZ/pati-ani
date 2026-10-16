package com.patikagunlugu.app.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CatPeekingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE // Fluffy white cat
        style = Paint.Style.FILL
    }

    private val pinkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD1D1") // Soft baby pink inner ears / tongue
        style = Paint.Style.FILL
    }

    private val nosePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B6B") // Cute coral heart nose
        style = Paint.Style.FILL
    }

    private val blushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#33FFB6C1") // Rosy blush cheeks
        style = Paint.Style.FILL
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5D4037") // Soft warm brown lines
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
    }

    private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val irisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4DA6FF") // Sparkling sky blue eyes
        style = Paint.Style.FILL
    }

    private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A1A")
        style = Paint.Style.FILL
    }

    private val sparklePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val density = resources.displayMetrics.density

        // 1. Draw Tail (hanging down behind body)
        val tailPath = Path().apply {
            moveTo(w * 0.52f, h - 15f * density)
            quadTo(w * 0.72f, h - 18f * density, w * 0.78f, h - 35f * density)
            quadTo(w * 0.82f, h - 42f * density, w * 0.75f, h - 44f * density)
            quadTo(w * 0.68f, h - 42f * density, w * 0.68f, h - 32f * density)
            quadTo(w * 0.64f, h - 22f * density, w * 0.52f, h - 18f * density)
            close()
        }
        canvas.drawPath(tailPath, whitePaint)
        canvas.drawPath(tailPath, linePaint)

        // 2. Draw Body (clinging and stretched vertically)
        val bodyRect = RectF(w * 0.32f, h - 38f * density, w * 0.68f, h - 12f * density)
        canvas.drawOval(bodyRect, whitePaint)
        canvas.drawOval(bodyRect, linePaint)

        // Draw little back paws hanging down on the body
        canvas.drawCircle(w * 0.36f, h - 15f * density, 6f * density, whitePaint)
        canvas.drawCircle(w * 0.36f, h - 15f * density, 6f * density, linePaint)
        canvas.drawCircle(w * 0.64f, h - 15f * density, 6f * density, whitePaint)
        canvas.drawCircle(w * 0.64f, h - 15f * density, 6f * density, linePaint)

        // 3. Draw Ears
        // Left Ear pointing up
        val leftEarPath = Path().apply {
            moveTo(w * 0.34f, h - 46f * density)
            lineTo(w * 0.24f, h - 72f * density)
            lineTo(w * 0.46f, h - 55f * density)
            close()
        }
        canvas.drawPath(leftEarPath, whitePaint)
        canvas.drawPath(leftEarPath, linePaint)

        // Left Inner Ear
        val leftInnerEarPath = Path().apply {
            moveTo(w * 0.36f, h - 48f * density)
            lineTo(w * 0.28f, h - 66f * density)
            lineTo(w * 0.43f, h - 53f * density)
            close()
        }
        canvas.drawPath(leftInnerEarPath, pinkPaint)

        // Right Ear pointing up
        val rightEarPath = Path().apply {
            moveTo(w * 0.66f, h - 46f * density)
            lineTo(w * 0.76f, h - 72f * density)
            lineTo(w * 0.54f, h - 55f * density)
            close()
        }
        canvas.drawPath(rightEarPath, whitePaint)
        canvas.drawPath(rightEarPath, linePaint)

        // Right Inner Ear
        val rightInnerEarPath = Path().apply {
            moveTo(w * 0.64f, h - 48f * density)
            lineTo(w * 0.72f, h - 66f * density)
            lineTo(w * 0.57f, h - 53f * density)
            close()
        }
        canvas.drawPath(rightInnerEarPath, pinkPaint)

        // 4. Draw Head
        val headRect = RectF(w * 0.22f, h - 58f * density, w * 0.78f, h - 18f * density)
        canvas.drawOval(headRect, whitePaint)
        canvas.drawOval(headRect, linePaint)

        // 5. Draw Blush cheeks
        canvas.drawCircle(w * 0.32f, h - 32f * density, 8f * density, blushPaint)
        canvas.drawCircle(w * 0.70f, h - 32f * density, 8f * density, blushPaint)

        // 6. Draw Eyes looking UP at the user (struggling and cute!)
        val eyeRadius = 7f * density
        val leftEyeCenterX = w * 0.38f
        val rightEyeCenterX = w * 0.62f
        val eyeCenterY = h - 36f * density

        // Left Eye base
        canvas.drawCircle(leftEyeCenterX, eyeCenterY, eyeRadius, eyeWhitePaint)
        canvas.drawCircle(leftEyeCenterX, eyeCenterY, eyeRadius, linePaint)

        // Right Eye base
        canvas.drawCircle(rightEyeCenterX, eyeCenterY, eyeRadius, eyeWhitePaint)
        canvas.drawCircle(rightEyeCenterX, eyeCenterY, eyeRadius, linePaint)

        // Irises (looking UP: offset y by -2dp)
        val leftIrisX = leftEyeCenterX
        val rightIrisX = rightEyeCenterX
        val irisY = eyeCenterY - 2f * density
        val irisRadius = 4.5f * density

        canvas.drawCircle(leftIrisX, irisY, irisRadius, irisPaint)
        canvas.drawCircle(rightIrisX, irisY, irisRadius, irisPaint)

        // Pupils
        val pupilRadius = 2.5f * density
        canvas.drawCircle(leftIrisX, irisY - 0.5f * density, pupilRadius, pupilPaint)
        canvas.drawCircle(rightIrisX, irisY - 0.5f * density, pupilRadius, pupilPaint)

        // Double Sparkle highlights (extremely cute glass reflections)
        canvas.drawCircle(leftIrisX - 1.2f * density, irisY - 1.2f * density, 1.2f * density, sparklePaint)
        canvas.drawCircle(leftIrisX + 1.2f * density, irisY + 1.2f * density, 0.6f * density, sparklePaint)

        canvas.drawCircle(rightIrisX - 1.2f * density, irisY - 1.2f * density, 1.2f * density, sparklePaint)
        canvas.drawCircle(rightIrisX + 1.2f * density, irisY + 1.2f * density, 0.6f * density, sparklePaint)

        // Cute eye lash curves
        canvas.drawArc(
            RectF(leftEyeCenterX - eyeRadius, eyeCenterY - eyeRadius, leftEyeCenterX + eyeRadius, eyeCenterY + eyeRadius),
            180f, 180f, false, linePaint
        )
        canvas.drawArc(
            RectF(rightEyeCenterX - eyeRadius, eyeCenterY - eyeRadius, rightEyeCenterX + eyeRadius, eyeCenterY + eyeRadius),
            180f, 180f, false, linePaint
        )

        // 7. Draw Nose (small heart)
        val nosePath = Path().apply {
            moveTo(w * 0.50f - 2.5f * density, h - 30f * density)
            lineTo(w * 0.50f + 2.5f * density, h - 30f * density)
            lineTo(w * 0.50f, h - 27f * density)
            close()
        }
        canvas.drawPath(nosePath, nosePaint)
        canvas.drawPath(nosePath, linePaint)

        // 8. Draw Mouth (worried/cute small open mewing circle!)
        val mouthRect = RectF(w * 0.48f, h - 25f * density, w * 0.52f, h - 21f * density)
        canvas.drawOval(mouthRect, pinkPaint)
        canvas.drawOval(mouthRect, linePaint)

        // Cute whisker lines (clinging to edge, so whiskers are flared out!)
        canvas.drawLine(w * 0.26f, h - 30f * density, w * 0.12f, h - 32f * density, linePaint)
        canvas.drawLine(w * 0.25f, h - 27f * density, w * 0.10f, h - 27f * density, linePaint)
        canvas.drawLine(w * 0.26f, h - 24f * density, w * 0.13f, h - 21f * density, linePaint)

        canvas.drawLine(w * 0.74f, h - 30f * density, w * 0.88f, h - 32f * density, linePaint)
        canvas.drawLine(w * 0.75f, h - 27f * density, w * 0.90f, h - 27f * density, linePaint)
        canvas.drawLine(w * 0.74f, h - 24f * density, w * 0.87f, h - 21f * density, linePaint)

        // 9. Draw Front Paws HOOKED over the top edge!
        // We draw them at the top of the head (around y = h - 54dp) but extending slightly upward to look like they grip the edge.
        // Left Paw
        canvas.drawCircle(w * 0.35f, h - 54f * density, 8f * density, whitePaint)
        canvas.drawCircle(w * 0.35f, h - 54f * density, 8f * density, linePaint)
        // Left paw claws gripping
        canvas.drawLine(w * 0.32f, h - 58f * density, w * 0.32f, h - 50f * density, linePaint)
        canvas.drawLine(w * 0.35f, h - 60f * density, w * 0.35f, h - 48f * density, linePaint)
        canvas.drawLine(w * 0.38f, h - 58f * density, w * 0.38f, h - 50f * density, linePaint)

        // Right Paw
        canvas.drawCircle(w * 0.65f, h - 54f * density, 8f * density, whitePaint)
        canvas.drawCircle(w * 0.65f, h - 54f * density, 8f * density, linePaint)
        // Right paw claws gripping
        canvas.drawLine(w * 0.62f, h - 58f * density, w * 0.62f, h - 50f * density, linePaint)
        canvas.drawLine(w * 0.65f, h - 60f * density, w * 0.65f, h - 48f * density, linePaint)
        canvas.drawLine(w * 0.68f, h - 58f * density, w * 0.68f, h - 50f * density, linePaint)
    }
}
