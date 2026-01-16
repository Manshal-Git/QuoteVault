package com.example.quotevault.core.auth

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor() {
    val client =
        createSupabaseClient(
            supabaseUrl = "https://pqyacgtkcmdhztubcqur.supabase.co",
            supabaseKey = "sb_publishable_s2YEH3BMToB0ceBFeSgdaQ_GxAHX7c9"
        ) {
            install(Auth)
            install(Postgrest)
        }
}