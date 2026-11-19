package com.toolboxreborn.overlay.data
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
class ConfigManager(context: Context) {
    private val prefs = context.getSharedPreferences("toolbox_reborn_cfg", Context.MODE_PRIVATE)
    var isCrosshairEnabled: Boolean get() = prefs.getBoolean("crosshair_enabled", false); set(v) = prefs.edit().putBoolean("crosshair_enabled", v).apply()
    var crosshairSize: Int get() = prefs.getInt("crosshair_size", 24); set(v) = prefs.edit().putInt("crosshair_size", v).apply()
    var crosshairStyle: Int get() = prefs.getInt("crosshair_style", 0); set(v) = prefs.edit().putInt("crosshair_style", v).apply()
    var isFpsHudEnabled: Boolean get() = prefs.getBoolean("fps_hud_enabled", true); set(v) = prefs.edit().putBoolean("fps_hud_enabled", v).apply()
    var isCpsHudEnabled: Boolean get() = prefs.getBoolean("cps_hud_enabled", true); set(v) = prefs.edit().putBoolean("cps_hud_enabled", v).apply()
    var isCoordsHudEnabled: Boolean get() = prefs.getBoolean("coords_hud_enabled", false); set(v) = prefs.edit().putBoolean("coords_hud_enabled", v).apply()
    var wsPort: Int get() = prefs.getInt("ws_port", 19134); set(v) = prefs.edit().putInt("ws_port", v).apply()
    fun saveWaypoint(n: String, x: String, y: String, z: String) {
        val a = getWaypoints()
        a.put(JSONObject().apply { put("name", n); put("x", x); put("y", y); put("z", z) })
        prefs.edit().putString("waypoints_json", a.toString()).apply()
    }
    fun getWaypoints(): JSONArray = try { JSONArray(prefs.getString("waypoints_json", "[]") ?: "[]") } catch (e: Exception) { JSONArray() }
    fun exportProfileJson(): String = JSONObject().apply { put("crosshair_enabled", isCrosshairEnabled); put("crosshair_style", crosshairStyle); put("fps_hud_enabled", isFpsHudEnabled); put("cps_hud_enabled", isCpsHudEnabled); put("ws_port", wsPort); put("waypoints", getWaypoints()) }.toString(2)
    fun importProfileJson(s: String): Boolean = try { val o = JSONObject(s); isCrosshairEnabled = o.optBoolean("crosshair_enabled", false); crosshairStyle = o.optInt("crosshair_style", 0); isFpsHudEnabled = o.optBoolean("fps_hud_enabled", true); isCpsHudEnabled = o.optBoolean("cps_hud_enabled", true); wsPort = o.optInt("ws_port", 19134); true } catch (e: Exception) { false }
}
