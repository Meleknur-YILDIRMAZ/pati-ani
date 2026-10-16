package com.patikagunlugu.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun formatMemoryDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatMemoryTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    sealed class AgeResult {
        data class Years(val years: Int) : AgeResult()
        data class Months(val months: Int) : AgeResult()
        object Newborn : AgeResult()
    }

    fun calculateAge(birthDateTimestamp: Long?): AgeResult? {
        if (birthDateTimestamp == null) return null
        
        val birthDate = Calendar.getInstance().apply { timeInMillis = birthDateTimestamp }
        val today = Calendar.getInstance()
        
        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        
        return if (age > 0) {
            AgeResult.Years(age)
        } else {
            val months = (today.get(Calendar.YEAR) * 12 + today.get(Calendar.MONTH)) - 
                         (birthDate.get(Calendar.YEAR) * 12 + birthDate.get(Calendar.MONTH))
            if (months > 0) AgeResult.Months(months) else AgeResult.Newborn
        }
    }

    fun attachParrotToDatePicker(dialog: android.app.Dialog) {
        val window = dialog.window ?: return
        val decorView = window.decorView as? android.view.ViewGroup ?: return
        val context = dialog.context

        val parrotView = com.patikagunlugu.app.ui.custom.ParrotPeekingView(context)
        val density = context.resources.displayMetrics.density
        val parrotWidth = (90 * density).toInt()
        val parrotHeight = (100 * density).toInt()

        // Add to decor view
        decorView.addView(parrotView, android.view.ViewGroup.LayoutParams(parrotWidth, parrotHeight))
        parrotView.visibility = android.view.View.INVISIBLE // Hide until dialog lays out!

        decorView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Find the dialog card view
                val contentView = window.findViewById<android.view.View>(android.R.id.content) as? android.view.ViewGroup
                val cardView = contentView?.getChildAt(0) ?: contentView ?: return

                if (cardView.width > 0 && cardView.height > 0) {
                    // Remove listener to prevent multiple invocations
                    decorView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // Calculate positions relative to decorView
                    val cardLocation = IntArray(2)
                    cardView.getLocationOnScreen(cardLocation)

                    val decorLocation = IntArray(2)
                    decorView.getLocationOnScreen(decorLocation)

                    val cardLeft = cardLocation[0] - decorLocation[0]
                    val cardTop = cardLocation[1] - decorLocation[1]
                    val cardRight = cardLeft + cardView.width

                    // Dynamically resolve the date picker header's bottom boundary so feet sit exactly on the line
                    val headerId = context.resources.getIdentifier("date_picker_header", "id", "android")
                    val headerIdMaterial = context.resources.getIdentifier("date_picker_header_material", "id", "android")
                    
                    val headerView = when {
                        headerId != 0 && cardView.findViewById<android.view.View>(headerId) != null -> cardView.findViewById<android.view.View>(headerId)
                        headerIdMaterial != 0 && cardView.findViewById<android.view.View>(headerIdMaterial) != null -> cardView.findViewById<android.view.View>(headerIdMaterial)
                        else -> null
                    }

                    val separatorY = if (headerView != null) {
                        val headerLocation = IntArray(2)
                        headerView.getLocationOnScreen(headerLocation)
                        headerLocation[1] - decorLocation[1] + headerView.height
                    } else {
                        // Calibrated fallback: standard Material DatePicker header bottom boundary is 126dp relative to cardTop
                        cardTop + (126 * density).toInt()
                    }

                    // Position the parrot view so its feet (6dp from bottom) stand exactly inside the bottom of the teal header
                    val leftMargin = cardRight - parrotWidth - (16 * density).toInt()
                    val topMargin = separatorY - parrotHeight - (8 * density).toInt()

                    val params = parrotView.layoutParams as android.view.ViewGroup.MarginLayoutParams
                    params.leftMargin = leftMargin
                    params.topMargin = topMargin
                    parrotView.layoutParams = params
                    
                    parrotView.visibility = android.view.View.VISIBLE
                }
            }
        })
    }
}
