package com.toolboxreborn.overlay.utils
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ResourcePackExporter {
    fun exportXrayPackTemplate(context: Context): String {
        val fileName = "ToolboxReborn_XRay_1_26.mcpack"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return "Error"
            context.contentResolver.openOutputStream(uri)?.use { writePack(it) }
            "Downloads/$fileName"
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { writePack(it) }
            file.absolutePath
        }
    }

    private fun writePack(out: OutputStream) {
        val manifest = JSONObject().apply {
            put("format_version", 2)
            put("header", JSONObject().apply {
                put("name", "Toolbox Reborn X-Ray")
                put("description", "Bedrock 1.26.65")
                put("uuid", UUID.randomUUID().toString())
                put("version", JSONArray(listOf(1, 0, 1)))
                put("min_engine_version", JSONArray(listOf(1, 21, 0)))
            })
            put("modules", JSONArray().put(JSONObject().apply {
                put("type", "resources")
                put("uuid", UUID.randomUUID().toString())
                put("version", JSONArray(listOf(1, 0, 1)))
            }))
        }.toString()

        val blocks = JSONObject().apply {
            put("format_version", JSONArray(listOf(1, 1, 0)))
            listOf("stone", "deepslate", "dirt", "netherrack").forEach { b ->
                put(b, JSONObject().apply { put("textures", "transparent_xray"); put("sound", b) })
            }
        }.toString()

        val terrain = JSONObject().apply {
            put("resource_pack_name", "toolbox_reborn_xray")
            put("texture_name", "atlas.terrain")
            put("texture_data", JSONObject().apply {
                put("transparent_xray", JSONObject().put("textures", "textures/blocks/transparent_xray"))
            })
        }.toString()

        val bmp = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.TRANSPARENT) }
        val png = ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.PNG, 100, it); bmp.recycle() }.toByteArray()

        ZipOutputStream(out).use { z ->
            z.putNextEntry(ZipEntry("manifest.json")); z.write(manifest.toByteArray()); z.closeEntry()
            z.putNextEntry(ZipEntry("blocks.json")); z.write(blocks.toByteArray()); z.closeEntry()
            z.putNextEntry(ZipEntry("textures/terrain_texture.json")); z.write(terrain.toByteArray()); z.closeEntry()
            z.putNextEntry(ZipEntry("textures/blocks/transparent_xray.png")); z.write(png); z.closeEntry()
        }
    }
}
