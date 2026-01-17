package com.example.quotevault.di

import android.content.Context
import androidx.work.WorkManager
import com.example.quotevault.data.OfflineAuthCache
import com.example.quotevault.utils.NetworkConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(@ApplicationContext context: Context): NetworkConnectivityManager {
        return NetworkConnectivityManager(context)
    }
    
    @Provides
    @Singleton
    fun provideOfflineAuthCache(@ApplicationContext context: Context): OfflineAuthCache {
        return OfflineAuthCache(context)
    }
}
