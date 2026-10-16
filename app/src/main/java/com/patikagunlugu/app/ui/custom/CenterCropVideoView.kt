package com.patikagunlugu.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

class CenterCropVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VideoView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val containerWidth = getDefaultSize(0, widthMeasureSpec)
        val containerHeight = getDefaultSize(0, heightMeasureSpec)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val videoWidth = measuredWidth
        val videoHeight = measuredHeight

        if (videoWidth > 0 && videoHeight > 0 && containerWidth > 0 && containerHeight > 0) {
            val scaleX = containerWidth.toFloat() / videoWidth.toFloat()
            val scaleY = containerHeight.toFloat() / videoHeight.toFloat()
            val scale = Math.max(scaleX, scaleY)

            val finalWidth = (videoWidth * scale).toInt()
            val finalHeight = (videoHeight * scale).toInt()

            setMeasuredDimension(finalWidth, finalHeight)
            return
        }
        setMeasuredDimension(containerWidth, containerHeight)
    }
}
