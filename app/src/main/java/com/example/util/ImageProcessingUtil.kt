package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImageProcessingUtil {

    enum class DocumentFilter {
        ORIGINAL,
        DOCUMENT_ENHANCE, // High contrast & brightened text
        GRAYSCALE,
        BLACK_AND_WHITE
    }

    /**
     * Loads a bitmap safely from a content URI with downsampling to avoid OOM
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? {
        return try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var sampleSize = 1
            var width = options.outWidth
            var height = options.outHeight

            while (width > maxDimension || height > maxDimension) {
                width /= 2
                height /= 2
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            inputStream = context.contentResolver.openInputStream(uri)
            val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            // Correct EXIF orientation if needed
            decodedBitmap?.let { bmp ->
                try {
                    val exifStream = context.contentResolver.openInputStream(uri)
                    if (exifStream != null) {
                        val exif = ExifInterface(exifStream)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        exifStream.close()
                        val matrix = Matrix()
                        when (orientation) {
                            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        }
                        if (!matrix.isIdentity) {
                            Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                        } else {
                            bmp
                        }
                    } else {
                        bmp
                    }
                } catch (e: Exception) {
                    bmp
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Rotates a bitmap by 90 degrees clockwise
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float = 90f): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Crops bitmap according to normalized RectF (values 0.0 to 1.0)
     */
    fun cropBitmap(bitmap: Bitmap, cropRectNormalized: RectF): Bitmap {
        val left = (max(0f, cropRectNormalized.left) * bitmap.width).toInt()
        val top = (max(0f, cropRectNormalized.top) * bitmap.height).toInt()
        val right = (min(1f, cropRectNormalized.right) * bitmap.width).toInt()
        val bottom = (min(1f, cropRectNormalized.bottom) * bitmap.height).toInt()

        val width = max(10, right - left)
        val height = max(10, bottom - top)

        val safeLeft = min(left, bitmap.width - width)
        val safeTop = min(top, bitmap.height - height)

        return Bitmap.createBitmap(bitmap, safeLeft, safeTop, width, height)
    }

    /**
     * Applies enhancement filters designed for scanning documents (KTP, KK, Letters)
     */
    fun applyFilter(bitmap: Bitmap, filter: DocumentFilter): Bitmap {
        if (filter == DocumentFilter.ORIGINAL) return bitmap

        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (filter) {
            DocumentFilter.GRAYSCALE -> {
                val matrix = ColorMatrix().apply { setSaturation(0f) }
                paint.colorFilter = ColorMatrixColorFilter(matrix)
            }
            DocumentFilter.DOCUMENT_ENHANCE -> {
                // Boost contrast & brightness for clean paper scanning
                val cm = ColorMatrix(floatArrayOf(
                    1.4f, 0f, 0f, 0f, -20f,
                    0f, 1.4f, 0f, 0f, -20f,
                    0f, 0f, 1.4f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            DocumentFilter.BLACK_AND_WHITE -> {
                val cm = ColorMatrix(floatArrayOf(
                    85f, 85f, 85f, 0f, -128f * 128,
                    85f, 85f, 85f, 0f, -128f * 128,
                    85f, 85f, 85f, 0f, -128f * 128,
                    0f, 0f, 0f, 1f, 0f
                ))
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            else -> {}
        }

        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    /**
     * Compresses bitmap to high-quality JPEG byte array (balanced size vs resolution)
     */
    fun bitmapToJpegBytes(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }
}
