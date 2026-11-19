package com.toolboxreborn.overlay.utils
import android.content.ContentValues; import android.content.Context; import android.graphics.Bitmap; import android.graphics.Color; import android.os.Build; import android.os.Environment; import android.provider.MediaStore
import java.io.*; import java.util.UUID; import java.util.zip.*
object ResourcePackExporter {
    fun exportXrayPackTemplate(context: Context): String {
        val fileName = "ToolboxReborn_XRay_1_26.mcpack"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply { put(MediaStore.Downloads.DISPLAY_NAME, fileName); put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream"); put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return "Error"
            context.contentResolver.openOutputStream(uri)?.use { writePack(it) }; "Downloads/$fileName"
        } else {
            @Suppress("DEPRECATION") val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName); FileOutputStream(file).use { writePack(it) }; file.absolutePath
        }
    }
    private fun writePack(out: OutputStream) {
        val manifest = "{"format_version":2,"header":{"name":"Toolbox Reborn X-Ray","description":"Bedrock 1.26.65","uuid":"${UUID.randomUUID()}","version":[1,0,1],"min_engine_version":[1,21,0]},"modules":[{"type":"resources","uuid":"${UUID.randomUUID()}","version":[1,0,1]}]}"
        val blocks = "{"format_version":[1,1,0],"stone":{"textures":"transparent_xray","sound":"stone"},"deepslate":{"textures":"transparent_xray","sound":"deepslate"},"dirt":{"textures":"transparent_xray","sound":"gravel"}}"
        val terrain = "{"resource_pack_name":"toolbox_reborn_xray","texture_name":"atlas.terrain","texture_data":{"transparent_xray":{"textures":"textures/blocks/transparent_xray"}}}"
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
