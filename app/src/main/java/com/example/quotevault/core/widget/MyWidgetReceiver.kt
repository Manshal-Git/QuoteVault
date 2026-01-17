package com.example.quotevault.core.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DailyQuoteWidget()
    
    companion object {
        private const val TAG = "MyWidgetReceiver"
        private const val WIDGET_UPDATE_WORK_NAME = "widget_daily_update"
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "Widget enabled, scheduling daily updates")
        scheduleDailyUpdates(context)
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Widget disabled, cancelling scheduled updates")
        cancelDailyUpdates(context)
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "Widget update requested for ${appWidgetIds.size} widgets")
        
        // Update widgets immediately
        CoroutineScope(Dispatchers.Main).launch {
            try {
                glanceAppWidget.updateAll(context)
                Log.d(TAG, "Widget update completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets", e)
            }
        }
    }
    
    private fun scheduleDailyUpdates(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        // Schedule daily updates at midnight
        val dailyUpdateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(getTimeUntilMidnight(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyUpdateRequest
        )
        
        Log.d(TAG, "Daily widget updates scheduled")
    }
    
    private fun cancelDailyUpdates(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }
    
    private fun getTimeUntilMidnight(): Long {
        val calendar = java.util.Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Set to next midnight
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        return calendar.timeInMillis - now
    }
}

class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "WidgetUpdateWorker"
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting daily widget update")
            DailyQuoteWidget().updateAll(applicationContext)
            Log.d(TAG, "Daily widget update completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during daily widget update", e)
            Result.retry()
        }
    }
}
