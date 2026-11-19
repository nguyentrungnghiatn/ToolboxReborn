package com.toolboxreborn.overlay.service
import android.app.*; import android.content.Intent; import android.os.*; import android.view.WindowManager
import androidx.core.app.NotificationCompat; import com.toolboxreborn.overlay.ui.FloatingMenuManager
class OverlayService : Service() {
    private var m: FloatingMenuManager? = null
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("tb_ch", "Toolbox Reborn", NotificationManager.IMPORTANCE_LOW))
        startForeground(101, NotificationCompat.Builder(this, "tb_ch").setContentTitle("Toolbox Reborn").setContentText("HUD Active").setSmallIcon(android.R.drawable.ic_menu_compass).setOngoing(true).build())
        m = FloatingMenuManager(this, getSystemService(WINDOW_SERVICE) as WindowManager).apply { attach() }
    }
    override fun onDestroy() { m?.detach(); m = null; super.onDestroy() }
    override fun onBind(i: Intent?): IBinder? = null
}
