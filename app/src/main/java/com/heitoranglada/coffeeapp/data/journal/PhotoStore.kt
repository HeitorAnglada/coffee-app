package com.heitoranglada.coffeeapp.data.journal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Fotos dos pacotes de café. Ficam só no celular, na pasta privada do app, já reduzidas
 * (lado maior de até [MAX_SIDE] px) para não ocupar espaço à toa.
 */
class PhotoStore(private val context: Context) {
    private val dir: File get() = File(context.filesDir, "beans").apply { mkdirs() }
    private val cameraDir: File get() = File(context.cacheDir, "camera").apply { mkdirs() }

    /** Miniaturas já decodificadas, para a lista rolar sem engasgar. */
    private val cache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun file(name: String): File = File(dir, name)

    /** Arquivo temporário e o endereço que a câmera usa para gravar a foto. */
    fun newCameraTarget(): Pair<File, Uri> {
        val f = File(cameraDir, "foto-${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fotos", f)
        return f to uri
    }

    /** Copia a foto (galeria ou câmera) para o app, já girada e reduzida. Retorna o nome do arquivo. */
    suspend fun import(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_SIDE)
            }
            val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: return@runCatching null
            val rotation = resolver.openInputStream(uri)?.use { input ->
                when (ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            val scaled = scaleDown(decoded, MAX_SIDE)
            val upright = if (rotation != 0f) {
                Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, Matrix().apply { postRotate(rotation) }, true)
            } else {
                scaled
            }
            val name = "${UUID.randomUUID()}.jpg"
            file(name).outputStream().use { upright.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            name
        }.getOrNull()
    }

    fun delete(name: String) {
        cache.remove(name)
        file(name).delete()
    }

    fun clearCameraTemp(f: File) {
        f.delete()
    }

    /** Lê a foto reduzida para caber em [targetPx] (lado maior). */
    suspend fun load(name: String, targetPx: Int): Bitmap? = withContext(Dispatchers.IO) {
        val key = "$name@$targetPx"
        cache.get(key)?.let { return@withContext it }
        val f = file(name)
        if (!f.exists()) return@withContext null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(f.path, bounds)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, targetPx)
        }
        BitmapFactory.decodeFile(f.path, opts)?.also { cache.put(key, it) }
    }

    companion object {
        const val MAX_SIDE = 1600

        fun sampleSize(width: Int, height: Int, target: Int): Int {
            var sample = 1
            while (maxOf(width, height) / (sample * 2) >= target) sample *= 2
            return sample
        }

        private fun scaleDown(bitmap: Bitmap, maxSide: Int): Bitmap {
            val side = maxOf(bitmap.width, bitmap.height)
            if (side <= maxSide) return bitmap
            val k = maxSide.toFloat() / side
            return Bitmap.createScaledBitmap(bitmap, (bitmap.width * k).toInt(), (bitmap.height * k).toInt(), true)
        }
    }
}
