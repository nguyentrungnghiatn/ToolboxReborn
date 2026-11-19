package com.toolboxreborn.overlay.ui
import android.annotation.SuppressLint; import android.content.*; import android.graphics.*; import android.graphics.drawable.GradientDrawable; import android.os.Build; import android.view.*; import android.widget.*
import com.toolboxreborn.overlay.R; import com.toolboxreborn.overlay.data.ConfigManager; import com.toolboxreborn.overlay.utils.*; import kotlin.math.abs
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
        setupBubble(); setupPanel(); if (config.isFpsHudEnabled) fpsMonitor.start(); updateHud()
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
                c.addView(btn("Đổi kiểu Crosshair (Cross / Dot / Circle)") { config.crosshairStyle = (config.crosshairStyle + 1) % 3; crosshairView.crosshairStyle = config.crosshairStyle; crosshairView.invalidate() })
                c.addView(btn("Xuất X-Ray Resource Pack (.mcpack)") { Toast.makeText(context, "Đã tạo: ${ResourcePackExporter.exportXrayPackTemplate(context)}", Toast.LENGTH_LONG).show() })
                c.addView(note("ESP: Overlay độc lập không đọc RAM game. Chỉ hoạt động qua WebSocket /wsserver."))
            }
            "PLAYER" -> {
                c.addView(sw("FPS Counter HUD", config.isFpsHudEnabled) { on -> config.isFpsHudEnabled = on; if (on) fpsMonitor.start() else fpsMonitor.stop(); updateHud() })
                c.addView(sw("Coordinates HUD (WebSocket)", config.isCoordsHudEnabled) { on -> config.isCoordsHudEnabled = on; updateHud() })
                c.addView(note("Gõ /wsserver 127.0.0.1:${config.wsPort} trong Minecraft để đồng bộ."))
            }
            "COMBAT" -> {
                c.addView(sw("CPS Counter HUD", config.isCpsHudEnabled) { on -> config.isCpsHudEnabled = on; updateHud() })
                c.addView(btn("Chạm thử đo CPS (Test Tap Pad)") { val now = System.currentTimeMillis(); clicks.addLast(now); while (clicks.isNotEmpty() && now - clicks.first() > 1000L) clicks.removeFirst(); updateHud() })
                c.addView(note("Android chặn đọc chạm xuyên ứng dụng để bảo mật; CPS đo trên vùng tương tác hợp lệ."))
            }
            "WORLD" -> {
                val n = EditText(context).apply { hint = "Tên Waypoint"; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE) }
                val xyz = EditText(context).apply { hint = "Tọa độ X Y Z (VD: 100 64 -200)"; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE) }
                c.addView(n); c.addView(xyz)
                c.addView(btn("Lưu Waypoint") { val p = xyz.text.toString().trim().split(" "); if (p.size >= 3) { config.saveWaypoint(n.text.toString().ifBlank { "WP" }, p[0], p[1], p[2]); renderTab("WORLD", c) } })
                val wps = config.getWaypoints(); for (i in 0 until wps.length()) { val w = wps.getJSONObject(i); c.addView(note("• ${w.getString("name")}: [${w.getString("x")}, ${w.getString("y")}, ${w.getString("z")}]")) }
            }
            "UTILITY" -> {
                c.addView(btn("Sao chép lệnh /wsserver") { val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; cb.setPrimaryClip(ClipData.newPlainText("ws", "/wsserver 127.0.0.1:${config.wsPort}")); Toast.makeText(context, "Đã sao chép", Toast.LENGTH_SHORT).show() })
                c.addView(note("Ứng dụng độc lập, không chỉnh sửa APK gốc của Minecraft 1.26.65."))
            }
            "SETTINGS" -> {
                c.addView(btn("Xuất cấu hình JSON ra Clipboard") { val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; cb.setPrimaryClip(ClipData.newPlainText("cfg", config.exportProfileJson())); Toast.makeText(context, "Đã xuất JSON", Toast.LENGTH_SHORT).show() })
                c.addView(btn("Nhập cấu hình JSON từ Clipboard") { val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; val ok = config.importProfileJson(cb.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()); Toast.makeText(context, if (ok) "Đã nhập" else "Lỗi JSON", Toast.LENGTH_SHORT).show() })
            }
        }
    }
    private fun sw(t: String, init: Boolean, cb: (Boolean) -> Unit) = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; addView(TextView(context).apply { text = t; setTextColor(Color.WHITE); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }); addView(Switch(context).apply { isChecked = init; setOnCheckedChangeListener { _, b -> cb(b) } }) }
    private fun btn(t: String, cb: () -> Unit) = TextView(context).apply { text = t; setTextColor(Color.parseColor("#0A0D0C")); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(16, 14, 16, 14); background = GradientDrawable().apply { setColor(Color.parseColor("#00FF88")); cornerRadius = 10f }; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8; bottomMargin = 6 }; setOnClickListener { cb() } }
    private fun note(t: String) = TextView(context).apply { text = t; setTextColor(Color.parseColor("#81C784")); textSize = 11f; setPadding(4, 6, 4, 6) }
    private fun updateHud() { val now = System.currentTimeMillis(); while (clicks.isNotEmpty() && now - clicks.first() > 1000L) clicks.removeFirst(); val p = mutableListOf<String>(); if (config.isFpsHudEnabled) p.add("FPS: $currentFps"); if (config.isCpsHudEnabled) p.add("CPS: ${clicks.size}"); if (config.isCoordsHudEnabled) p.add("XYZ: WS_WAIT"); if (p.isEmpty()) hudView.visibility = View.GONE else { hudView.visibility = View.VISIBLE; hudView.text = p.joinToString(" | ") } }
}
