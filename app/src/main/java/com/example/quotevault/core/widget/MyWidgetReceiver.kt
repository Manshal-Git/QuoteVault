package com.example.quotevault.core.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class MyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = QuoteWidget()
}
