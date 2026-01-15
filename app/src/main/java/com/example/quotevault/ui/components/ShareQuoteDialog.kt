package com.example.quotevault.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.quotevault.ui.quotes.Quote
import com.example.quotevault.ui.theme.GradientEnd
import com.example.quotevault.ui.theme.GradientStart
import com.example.quotevault.utils.QuoteImageGenerator
import java.io.File
import java.io.FileOutputStream

@Composable
fun ShareQuoteDialog(
    quote: Quote,
    onDismiss: () -> Unit,
    onShareComplete: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Share Quote",
                style = MaterialTheme.typography.headlineLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quote Preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(GradientStart, GradientEnd)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = quote.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = androidx.compose.ui.graphics.Color.White,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "— ${quote.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                
                Text(
                    text = "Choose how to share:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share as Text
                TextButton(
                    onClick = {
                        shareAsText(context, quote)
                        onShareComplete("Shared as text")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Share as Text")
                }
                
                // Share as Image
                TextButton(
                    onClick = {
                        shareAsImage(context, quote) { success ->
                            if (success) {
                                onShareComplete("Shared as image")
                            } else {
                                onShareComplete("Failed to share image")
                            }
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Share as Image")
                }
                
                // Save as Image
                TextButton(
                    onClick = {
                        saveAsImage(context, quote) { success ->
                            if (success) {
                                onShareComplete("Saved to gallery")
                            } else {
                                onShareComplete("Failed to save image")
                            }
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Save to Gallery")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        },
    )
}

private fun shareAsText(context: Context, quote: Quote) {
    val shareText = "\"${quote.text}\"\n\n— ${quote.author}\n\n#QuoteVault"
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Quote by ${quote.author}")
    }
    
    context.startActivity(Intent.createChooser(intent, "Share Quote"))
}

private fun shareAsImage(context: Context, quote: Quote, onResult: (Boolean) -> Unit) {
    try {
        val bitmap = QuoteImageGenerator.generateQuoteImage(quote)
        val file = saveBitmapToCache(context, bitmap, "quote_${quote.id}.png")
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share Quote Image"))
        onResult(true)
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(false)
    }
}

private fun saveAsImage(
    context: Context,
    quote: Quote,
    onResult: (Boolean) -> Unit
) {
    try {
        val bitmap = QuoteImageGenerator.generateQuoteImage(quote)
        QuoteImageGenerator.saveBitmapToGallery(context, bitmap, "quote_${quote.id}")
        onResult(true)
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(false)
    }
}

private fun saveBitmapToCache(
    context: Context,
    bitmap: Bitmap,
    filename: String
): File {
    val cacheDir = File(context.cacheDir, "shared_images")
    if (!cacheDir.exists()) {
        cacheDir.mkdirs()
    }
    
    val file = File(cacheDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    
    return file
}
