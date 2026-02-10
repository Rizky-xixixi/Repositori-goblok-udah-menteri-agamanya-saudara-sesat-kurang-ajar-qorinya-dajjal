package com.example.ritamesa.data.api

import android.content.Context
import android.util.Log
import com.example.ritamesa.data.pref.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "ApiClient"
    private const val ENABLE_LOGGING = false
    private const val BASE_URL = "http://192.168.0.106:8000/api/"

    @Volatile
    private var retrofit: Retrofit? = null
    
    @Volatile
    private var currentToken: String? = null

    fun getClient(context: Context): Retrofit {
        val sessionManager = SessionManager(context)
        val token = sessionManager.getAuthToken()
        
        // Rebuild client if token changed (e.g., after login/logout)
        if (retrofit == null || currentToken != token) {
            synchronized(this) {
                if (retrofit == null || currentToken != token) {
                    currentToken = token
                    retrofit = buildRetrofit(sessionManager)
                }
            }
        }
        return retrofit!!
    }
    
    private fun buildRetrofit(sessionManager: SessionManager): Retrofit {
        // Logging Interceptor - Only log in debug mode
        val logging = HttpLoggingInterceptor { message ->
            if (ENABLE_LOGGING) {
                Log.d(TAG, message)
            }
        }.apply {
            level = if (ENABLE_LOGGING) {
                HttpLoggingInterceptor.Level.BASIC // Use BASIC instead of BODY to reduce log spam
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // Auth Interceptor
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            
            sessionManager.getAuthToken()?.takeIf { it.isNotEmpty() }?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }
            
            requestBuilder.header("Accept", "application/json")
            requestBuilder.header("Content-Type", "application/json")
            requestBuilder.method(original.method, original.body)
            
            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Force rebuild of the Retrofit instance (e.g., after logout)
     */
    fun invalidate() {
        synchronized(this) {
            retrofit = null
            currentToken = null
        }
    }
}
