package com.patikagunlugu.app.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.google.firebase.auth.FirebaseAuth
import com.patikagunlugu.app.R
import com.patikagunlugu.app.ui.auth.AuthActivity
import com.patikagunlugu.app.utils.LocaleHelper
import kotlin.math.sin

class SplashActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    companion object {
        private const val SPLASH_DURATION = 3200L  // Restored original 3.2s duration
        private const val REVEAL_DELAY    = 300L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val title        = findViewById<android.widget.TextView>(R.id.tvSplashTitle)
        val centerImg    = findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivSplashCenter)
        val loadingBox   = findViewById<View>(R.id.loadingContainer)
        val progressBar  = findViewById<ProgressBar>(R.id.splashProgressBar)
        val walkingCat   = findViewById<ImageView>(R.id.ivWalkingCat)
        val catShadow    = findViewById<View>(R.id.catShadow)

        // 12 beautiful flying birds
        val birds = listOf(
            findViewById<ImageView>(R.id.bird1),
            findViewById<ImageView>(R.id.bird2),
            findViewById<ImageView>(R.id.bird3),
            findViewById<ImageView>(R.id.bird4),
            findViewById<ImageView>(R.id.bird5),
            findViewById<ImageView>(R.id.bird6),
            findViewById<ImageView>(R.id.bird7),
            findViewById<ImageView>(R.id.bird8),
            findViewById<ImageView>(R.id.bird9),
            findViewById<ImageView>(R.id.bird10),
            findViewById<ImageView>(R.id.bird11),
            findViewById<ImageView>(R.id.bird12)
        )

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val activeAnimators = mutableListOf<ValueAnimator>()

        // ── 1. Animate flying birds with REAL wing flapping (Infinite Loops) ──────────
        fun flyBird(bird: ImageView, index: Int, startDelay: Long, flightDuration: Long, yWave: Float = 0f) {
            val targetAlpha = bird.alpha // Capture target XML alpha
            bird.alpha = 0f // HIDE immediately so they never wait/stay on screen!

            // FORCE starting position off-screen left, staggered so they don't overlap in a straight line
            val startX = -180f - (index * 30f)
            bird.translationX = startX

            // Horizontal flight across screen (infinite loop)
            val flyX = ObjectAnimator.ofFloat(bird, "translationX",
                startX, screenWidth + 200f).apply {
                duration = flightDuration
                this.startDelay = startDelay
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
            }

            // Sinusoidal vertical wave for natural flight path (infinite loop)
            val flyY = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = flightDuration
                this.startDelay = startDelay
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
                addUpdateListener { anim ->
                    val t = anim.animatedValue as Float
                    bird.translationY = (sin(t * Math.PI * 3.0) * yWave).toFloat()
                }
            }

            // Wing flapping: rapid scaleY oscillation to simulate flapping (infinite loop)
            val wingFlap = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 200  // one flap cycle = 200ms
                this.startDelay = startDelay
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { anim ->
                    val t = anim.animatedValue as Float
                    // scaleY goes 1.0 -> 0.6 -> 1.0 (wings up-down)
                    bird.scaleY = 0.6f + (0.4f * (1f - t))
                }
            }

            // Fade in gently as the bird actually starts flying (non-looping, runs only once!)
            val fadeIn = ObjectAnimator.ofFloat(bird, "alpha", 0f, targetAlpha).apply {
                duration = 400 // 400ms smooth fade in
                this.startDelay = startDelay
                interpolator = DecelerateInterpolator()
            }

            flyX.start()
            flyY.start()
            wingFlap.start()
            fadeIn.start()

            activeAnimators.add(flyX)
            activeAnimators.add(flyY)
            activeAnimators.add(wingFlap)
            activeAnimators.add(fadeIn)
        }

        // Widely staggered start delays (0ms to 2200ms) and varied flight durations.
        // This ensures the 12 birds are evenly distributed across the entire timeline of the splash screen,
        // flying continuously as a beautiful, natural stream rather than grouping up into a single pack!
        val birdDelays    = longArrayOf(0, 200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200)
        val birdDurations = longArrayOf(2400, 2700, 2500, 2800, 2300, 2600, 2500, 2700, 2400, 2600, 2300, 2800)
        val birdWaves     = floatArrayOf(18f, -14f, 22f, -10f, 12f, -18f, 15f, -12f, 14f, -16f, 10f, -12f)

        birds.forEachIndexed { i, bird ->
            flyBird(bird, i, birdDelays[i], birdDurations[i], birdWaves[i])
        }

        // ── 2. Hero reveal: title + illustration fade/scale in ──────
        title.alpha = 0f
        centerImg.alpha = 0f
        centerImg.scaleX = 0.7f
        centerImg.scaleY = 0.7f
        loadingBox.alpha = 0f

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).apply {
                    duration = 600; startDelay = REVEAL_DELAY
                    interpolator = DecelerateInterpolator()
                },
                ObjectAnimator.ofFloat(centerImg, "alpha", 0f, 1f).apply {
                    duration = 700; startDelay = REVEAL_DELAY + 150
                    interpolator = DecelerateInterpolator()
                },
                ObjectAnimator.ofFloat(centerImg, "scaleX", 0.7f, 1f).apply {
                    duration = 700; startDelay = REVEAL_DELAY + 150
                    interpolator = DecelerateInterpolator()
                },
                ObjectAnimator.ofFloat(centerImg, "scaleY", 0.7f, 1f).apply {
                    duration = 700; startDelay = REVEAL_DELAY + 150
                    interpolator = DecelerateInterpolator()
                },
                ObjectAnimator.ofFloat(loadingBox, "alpha", 0f, 1f).apply {
                    duration = 500; startDelay = REVEAL_DELAY + 600
                    interpolator = DecelerateInterpolator()
                }
            )
            start()
        }

        // ── 3. Progress bar + walking cat with real walking animation ──
        loadingBox.postDelayed({
            progressBar.post {
                val barWidth = progressBar.width.toFloat()

                // Walking gait cycle — highly visible stepping animation
                var stepPhase = 0.0

                val bounceCycle = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
                    duration = 350  // slower = more visible steps
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener { anim ->
                        stepPhase = (anim.animatedValue as Float).toDouble()

                        // Pronounced bounce: up on lift, down on paw-strike
                        val bounce = sin(stepPhase * 2).toFloat()
                        walkingCat.translationY = -6f + (bounce * 6f)

                        // Body rocks left-right with each step
                        val tilt = sin(stepPhase).toFloat()
                        walkingCat.rotation = tilt * 6f

                        // Squash on landing, stretch on lift
                        walkingCat.scaleX = 1f - (bounce * 0.06f)
                        walkingCat.scaleY = 1f + (bounce * 0.08f)

                        // Shadow breathes with each step
                        catShadow.scaleX = 1f + (bounce * 0.2f)
                        catShadow.scaleY = 1f - (bounce * 0.1f)
                        catShadow.alpha = 0.3f + (bounce * 0.12f)
                    }
                }
                bounceCycle.start()

                val progressAnim = ValueAnimator.ofInt(0, 100).apply {
                    duration = SPLASH_DURATION - REVEAL_DELAY - 700
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animator ->
                        val value = animator.animatedValue as Int
                        progressBar.progress = value

                        // Cat follows the leading edge
                        val catWidthPx = walkingCat.width.toFloat().coerceAtLeast(1f)
                        val fraction = value / 100f
                        val targetX = fraction * (barWidth - catWidthPx * 0.5f) - catWidthPx * 0.5f
                        walkingCat.translationX = targetX

                        // Shadow follows the cat
                        catShadow.translationX = targetX + (catWidthPx * 0.1f)
                    }
                    doOnEnd {
                        bounceCycle.cancel()
                        activeAnimators.forEach { it.cancel() } // Clean up all active bird animators
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val next = if (currentUser != null)
                            Intent(this@SplashActivity, MainActivity::class.java)
                        else
                            Intent(this@SplashActivity, AuthActivity::class.java)
                        startActivity(next)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                }
                progressAnim.start()
            }
        }, REVEAL_DELAY + 700)
    }
}
