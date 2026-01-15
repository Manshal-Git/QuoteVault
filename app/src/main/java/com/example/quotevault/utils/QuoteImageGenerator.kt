package com.example.quotevault.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.quotevault.ui.quotes.Quote
import java.io.File
import java.io.FileOutputStream

private const val APP_NAME = "QuoteVault"

object QuoteImageGenerator {
    
    private const val IMAGE_WIDTH = 1080
    private const val IMAGE_HEIGHT = 1080
    private const val PADDING = 120f
    private const val QUOTE_TEXT_SIZE = 48f
    private const val AUTHOR_TEXT_SIZE = 36f
    
    fun generateQuoteImage(quote: Quote): Bitmap {
        val bitmap = createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT)
        val canvas = Canvas(bitmap)
        
        // Draw gradient background
        drawGradientBackground(canvas)
        
        // Draw quote text
        drawQuoteText(canvas, quote.text)
        
        // Draw author
        drawAuthor(canvas, quote.author)
        
        // Draw category badge
        drawCategory(canvas, quote.category)
        
        return bitmap
    }
    
    private fun drawGradientBackground(canvas: Canvas) {
        val gradient = LinearGradient(
            0f, 0f, 0f, IMAGE_HEIGHT.toFloat(),
            intArrayOf(
                "#6366F1".toColorInt(), // Primary color
                "#9333EA".toColorInt()  // Gradient end
            ),
            null,
            Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = gradient
        }
        
        canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), IMAGE_HEIGHT.toFloat(), paint)
    }
    
    private fun drawQuoteText(canvas: Canvas, text: String) {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = QUOTE_TEXT_SIZE
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        // Add quotation marks
        val quotedText = "\"$text\""
        
        // Split text into lines
        val lines = wrapText(quotedText, paint, IMAGE_WIDTH - (PADDING * 2))
        
        // Calculate starting Y position to center text vertically
        val lineHeight = paint.descent() - paint.ascent()
        val totalHeight = lineHeight * lines.size
        var y = (IMAGE_HEIGHT - totalHeight) / 2 + Math.abs(paint.ascent())
        
        // Draw each line
        lines.forEach { line ->
            canvas.drawText(line, IMAGE_WIDTH / 2f, y, paint)
            y += lineHeight
        }
    }
    
    private fun drawAuthor(canvas: Canvas, author: String) {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = AUTHOR_TEXT_SIZE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val authorText = "— $author"
        val y = IMAGE_HEIGHT - PADDING - 100f
        
        canvas.drawText(authorText, IMAGE_WIDTH / 2f, y, paint)
    }
    
    private fun drawCategory(canvas: Canvas, category: String) {
        val paint = Paint().apply {
            color = "#E0E7FF".toColorInt() // Light primary color
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val categoryText = category.uppercase()
        val y = PADDING
        
        canvas.drawText(categoryText, IMAGE_WIDTH / 2f, y, paint)
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            
            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }
    
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String): Boolean {
        return try {
            val displayName = "$filename.png"
            val mimeType = "image/png"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/$APP_NAME")
                }
                
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    true
                } ?: false
            } else {
                // Use legacy storage for older Android versions
                val imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).toString() + "/$APP_NAME"
                
                val dir = File(imagesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                
                val file = File(dir, displayName)
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                
                // Notify gallery
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
