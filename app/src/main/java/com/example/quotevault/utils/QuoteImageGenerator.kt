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

object QuoteImageGenerator {
    
    private const val IMAGE_WIDTH = 1080
    private const val IMAGE_HEIGHT = 1080
    private const val PADDING = 120f
    private const val QUOTE_TEXT_SIZE = 48f
    private const val AUTHOR_TEXT_SIZE = 36f
    
    fun generateQuoteImage(
        quote: Quote, 
        style: QuoteCardStyle = QuoteCardStyle.GRADIENT,
        fontSizeScale: Float = 1.0f
    ): Bitmap {
        val bitmap = createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT)
        val canvas = Canvas(bitmap)
        
        when (style) {
            QuoteCardStyle.GRADIENT -> drawGradientStyle(canvas, quote, fontSizeScale)
            QuoteCardStyle.MINIMAL -> drawMinimalStyle(canvas, quote, fontSizeScale)
            QuoteCardStyle.ELEGANT -> drawElegantStyle(canvas, quote, fontSizeScale)
        }
        
        return bitmap
    }
    
    private fun drawGradientStyle(canvas: Canvas, quote: Quote, fontSizeScale: Float) {
        // Draw gradient background
        val gradient = LinearGradient(
            0f, 0f, 0f, IMAGE_HEIGHT.toFloat(),
            intArrayOf(
                "#6366F1".toColorInt(),
                "#9333EA".toColorInt()
            ),
            null,
            Shader.TileMode.CLAMP
        )
        
        canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), IMAGE_HEIGHT.toFloat(), Paint().apply {
            shader = gradient
        })
        
        drawCategory(canvas, quote.category, "#E0E7FF".toColorInt())
        drawQuoteText(canvas, quote.text, Color.WHITE, QUOTE_TEXT_SIZE * fontSizeScale, Typeface.SERIF)
        drawAuthor(canvas, quote.author, Color.WHITE)
    }
    
    private fun drawMinimalStyle(canvas: Canvas, quote: Quote, fontSizeScale: Float) {
        canvas.drawColor(Color.WHITE)
        
        // Draw border
        val borderPaint = Paint().apply {
            color = "#E5E7EB".toColorInt()
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawRect(40f, 40f, IMAGE_WIDTH - 40f, IMAGE_HEIGHT - 40f, borderPaint)
        
        drawCategory(canvas, quote.category, "#6366F1".toColorInt())
        drawQuoteText(canvas, quote.text, "#1F2937".toColorInt(), QUOTE_TEXT_SIZE * fontSizeScale, Typeface.SERIF)
        drawAuthor(canvas, quote.author, "#6B7280".toColorInt())
    }
    
    private fun drawElegantStyle(canvas: Canvas, quote: Quote, fontSizeScale: Float) {
        // Dark gradient background
        val gradient = LinearGradient(
            0f, 0f, IMAGE_WIDTH.toFloat(), IMAGE_HEIGHT.toFloat(),
            intArrayOf(
                "#1F2937".toColorInt(),
                "#111827".toColorInt()
            ),
            null,
            Shader.TileMode.CLAMP
        )
        
        canvas.drawRect(0f, 0f, IMAGE_WIDTH.toFloat(), IMAGE_HEIGHT.toFloat(), Paint().apply {
            shader = gradient
        })
        
        drawCornerDecoration(canvas)
        drawCategory(canvas, quote.category, "#F59E0B".toColorInt())
        drawQuoteText(canvas, quote.text,
            "#FCD34D".toColorInt(), (QUOTE_TEXT_SIZE + 4f) * fontSizeScale, Typeface.SERIF)
        drawAuthor(canvas, quote.author, "#FDE68A".toColorInt())
    }
    
    private fun drawCornerDecoration(canvas: Canvas) {
        val paint = Paint().apply {
            color = "#F59E0B".toColorInt()
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        
        val cornerSize = 80f
        
        // Top-left
        canvas.drawLine(PADDING, PADDING, PADDING + cornerSize, PADDING, paint)
        canvas.drawLine(PADDING, PADDING, PADDING, PADDING + cornerSize, paint)
        
        // Top-right
        canvas.drawLine(IMAGE_WIDTH - PADDING, PADDING, IMAGE_WIDTH - PADDING - cornerSize, PADDING, paint)
        canvas.drawLine(IMAGE_WIDTH - PADDING, PADDING, IMAGE_WIDTH - PADDING, PADDING + cornerSize, paint)
        
        // Bottom-left
        canvas.drawLine(PADDING, IMAGE_HEIGHT - PADDING, PADDING + cornerSize, IMAGE_HEIGHT - PADDING, paint)
        canvas.drawLine(PADDING, IMAGE_HEIGHT - PADDING, PADDING, IMAGE_HEIGHT - PADDING - cornerSize, paint)
        
        // Bottom-right
        canvas.drawLine(IMAGE_WIDTH - PADDING, IMAGE_HEIGHT - PADDING, IMAGE_WIDTH - PADDING - cornerSize, IMAGE_HEIGHT - PADDING, paint)
        canvas.drawLine(IMAGE_WIDTH - PADDING, IMAGE_HEIGHT - PADDING, IMAGE_WIDTH - PADDING, IMAGE_HEIGHT - PADDING - cornerSize, paint)
    }
    
    private fun drawQuoteText(canvas: Canvas, text: String, color: Int, textSize: Float, typeface: Typeface) {
        val paint = Paint().apply {
            this.color = color
            this.textSize = textSize
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val quotedText = "\"$text\""
        val lines = wrapText(quotedText, paint, IMAGE_WIDTH - (PADDING * 2))
        
        val lineHeight = paint.descent() - paint.ascent()
        val totalHeight = lineHeight * lines.size
        var y = (IMAGE_HEIGHT - totalHeight) / 2 + Math.abs(paint.ascent())
        
        lines.forEach { line ->
            canvas.drawText(line, IMAGE_WIDTH / 2f, y, paint)
            y += lineHeight
        }
    }
    
    private fun drawAuthor(canvas: Canvas, author: String, color: Int) {
        val paint = Paint().apply {
            this.color = color
            textSize = AUTHOR_TEXT_SIZE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText("— $author", IMAGE_WIDTH / 2f, IMAGE_HEIGHT - PADDING - 100f, paint)
    }
    
    private fun drawCategory(canvas: Canvas, category: String, color: Int) {
        val paint = Paint().apply {
            this.color = color
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText(category.uppercase(), IMAGE_WIDTH / 2f, PADDING, paint)
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
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuoteVault")
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
                val imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).toString() + "/QuoteVault"
                
                val dir = File(imagesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                
                val file = File(dir, displayName)
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                
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
