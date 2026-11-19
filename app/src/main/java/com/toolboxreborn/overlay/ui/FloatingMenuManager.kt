package com.toolboxreborn.overlay.ui
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.toolboxreborn.overlay.R
import com.toolboxreborn.overlay.data.ConfigManager
import com.toolboxreborn.overlay.utils.FpsMonitor
import com.toolboxreborn.overlay.utils.ResourcePackExporter
import kotlin.math.abs
class FloatingMenuManager(private val context: Context, private val windowManager: WindowManager) {
    private val config = ConfigManager(context)
    private lateinit var bubbleView: View; private lateinit var panelView: LinearLayout; private lateinit var hudView: TextView; private lateinit var crosshairView: CrosshairOverlayView
    private lateinit var bubbleParams: WindowManager.LayoutParams; private lateinit var panelParams: WindowManager.LayoutParams
    private var isPanelOpen = false; private var currentFps = 60; private val clicks = ArrayDeque<Long>()
    private val fpsMonitor = FpsMonitor { fps -> currentFps = fps; updateHud() }
    private val oType get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
    fun attach() {
        crosshairView = CrosshairOverlayView(context).apply { crosshairSize = config.crosshairSize; crosshairStyle = config.crosshairStyle; visibility = if (config.isCrosshairEnabled) View.VISIBLE else View.GONE }
        windowManager.addView(crosshairView, WindowManager.LayoutParams(120, 120, oType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER })
        hudView = TextView(context).apply { setTextColor(Color.parseColor("#00FF88")); textSize = 12f; typeface = Typeface.MONOSPACE; setPadding(18, 10, 18, 10); background = GradientDrawable().apply { setColor(Color.parseColor("#CC0A0D0C")); setStroke(2, Color.parseColor("#00FF88")); cornerRadius = 16f } }
        windowManager.addView(hudView, WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, oType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 24; y = 24 })
        setupBubble(); setupPanel()
        if (config.isFpsHudEnabled) fpsMonitor.start()
        updateHud()
    }
    fun detach() { fpsMonitor.stop(); runCatching { windowManager.removeView(bubbleView); windowManager.removeView(panelView); windowManager.removeView(hudView); windowManager.removeView(crosshairView) } }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubble() {
        bubbleView = TextView(context).apply { text = "TB"; setTextColor(Color.parseColor("#0A0D0C")); textSize = 15f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor("#00FF88")); setStroke(4, Color.WHITE) } }
        bubbleParams = WindowManager.LayoutParams(124, 124, oType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 40; y = 220 }
        var ix = 0; var iy = 0; var tx = 0f; var ty = 0f
        bubbleView.setOnTouchListener { _, e -> when (e.action) {
            MotionEvent.ACTION_DOWN -> { ix = bubbleParams.x; iy = bubbleParams.y; tx = e.rawX; ty = e.rawY; true }
            MotionEvent.ACTION_MOVE -> { bubbleParams.x = ix + (e.rawX - tx).toInt(); bubbleParams.y = iy + (e.rawY - ty).toInt(); windowManager.updateViewLayout(bubbleView, bubbleParams); true }
            MotionEvent.ACTION_UP -> { if (abs(e.rawX - tx) < 12 && abs(e.rawY - ty) < 12) { isPanelOpen = !isPanelOpen; panelView.visibility = if (isPanelOpen) View.VISIBLE else View.GONE }; true }
            else -> false
        } }
        windowManager.addView(bubbleView, bubbleParams)
    }
    private fun setupPanel() {
        panelView = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 20, 24, 20); setBackgroundResource(R.drawable.bg_neon_panel); visibility = View.GONE }
        val header = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(context).apply { text = "TOOLBOX REBORN // 1.26.65"; setTextColor(Color.parseColor("#00FF88")); textSize = 14f; typeface = Typeface.DEFAULT_BOLD; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
        header.addView(TextView(context).apply { text = " [ _ ] "; setTextColor(Color.parseColor("#FF3B5C")); textSize = 14f; setOnClickListener { isPanelOpen = false; panelView.visibility = View.GONE } })
        panelView.addView(header)
        val scroll = HorizontalScrollView(context).apply { isHorizontalScrollBarEnabled = false; setPadding(0, 12, 0, 12) }
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val body = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(8, 12, 8, 8) }
        listOf("RENDER", "PLAYER", "COMBAT", "WORLD", "UTILITY", "SETTINGS").forEach { t ->
            row.addView(TextView(context).apply { text = t; setTextColor(Color.WHITE); textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setPadding(20, 12, 20, 12); background = GradientDrawable().apply { setColor(Color.parseColor("#182420")); setStroke(2, Color.parseColor("#00FF88")); cornerRadius = 10f }; setOnClickListener { renderTab(t, body) } }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { rightMargin = 10 })
        }
        scroll.addView(row); panelView.addView(scroll); panelView.addView(body); renderTab("RENDER", body)
        panelParams = WindowManager.LayoutParams(820, WindowManager.LayoutParams.WRAP_CONTENT, oType, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER }
        windowManager.addView(panelView, panelParams)
    }
    private fun renderTab(tab: String, c: LinearLayout) {
        c.removeAllViews()
        when (tab) {
            "RENDER" -> {
                c.addView(sw("Crosshair Overlay", config.isCrosshairEnabled) { on -> config.isCrosshairEnabled = on; crosshairView.visibility = if (on) View.VISIBLE else View.GONE })
                c.addView(btn("Đổi kiểu Crosshair (Cross / Dot / Circle)") { config.crosshairStyle = (config.crosshairStyle + 1) 