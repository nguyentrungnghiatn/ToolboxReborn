package com.toolboxreborn.overlay
import android.Manifest; import android.content.Intent; import android.content.pm.PackageManager; import android.graphics.*; import android.net.Uri; import android.os.*; import android.provider.Settings; import android.view.Gravity; import android.widget.*
import androidx.appcompat.app.AppCompatActivity; import androidx.core.app.ActivityCompat; import androidx.core.content.ContextCompat; import com.toolboxreborn.overlay.service.OverlayService
class MainActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(48, 48, 48, 48); setBackgroundColor(Color.parseColor("#0A0D0C")) }
        root.addView(TextView(this).apply { text = "TOOLBOX REBORN"; setTextColor(Color.parseColor("#00FF88")); textSize = 26f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER })
        root.addView(TextView(this).apply { text = "Minecraft Bedrock 1.26.65 Overlay (Android 10-16)"; setTextColor(Color.parseColor("#81C784")); textSize = 13f; gravity = Gravity.CENTER; setPadding(0, 12, 0, 48) })
        root.addView(Button(this).apply { text = getString(R.string.start_overlay); setBackgroundColor(Color.parseColor("#00FF88")); setTextColor(Color.parseColor("#0A0D0C")); setOnClickListener { if (!Settings.canDrawOverlays(this@MainActivity)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) else { ContextCompat.startForegroundService(this@MainActivity, Intent(this@MainActivity, OverlayService::class.java)); Toast.makeText(this@MainActivity, "Floating HUD đã bật", Toast.LENGTH_SHORT).show() } } })
        root.addView(Button(this).apply { text = getString(R.string.stop_overlay); setBackgroundColor(Color.parseColor("#182420")); setTextColor(Color.parseColor("#FF3B5C")); setOnClickListener { stopService(Intent(this@MainActivity, OverlayService::class.java)) } })
        setContentView(root)
    }
}
