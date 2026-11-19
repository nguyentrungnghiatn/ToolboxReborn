package com.toolboxreborn.overlay.ui
import android.content.Context; import android.graphics.*; import android.view.View
class CrosshairOverlayView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00FF88"); strokeWidth = 3f; style = Paint.Style.STROKE }
    var crosshairSize = 24; var crosshairStyle = 0
    override fun onDraw(c: Canvas) {
        super.onDraw(c); val cx = width / 2f; val cy = height / 2f; val h = crosshairSize / 2f
        when (crosshairStyle) {
            0 -> { paint.style = Paint.Style.STROKE; c.drawLine(cx - h, cy, cx + h, cy, paint); c.drawLine(cx, cy - h, cx, cy + h, paint) }
            1 -> { paint.style = Paint.Style.FILL; c.drawCircle(cx, cy, h / 3f, paint) }
            else -> { paint.style = Paint.Style.STROKE; c.drawCircle(cx, cy, h, paint); paint.style = Paint.Style.FILL; c.drawCircle(cx, cy, 3f, paint) }
        }
    }
}
