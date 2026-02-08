package com.example.ritamesa.data.api

import android.content.Context
import com.example.ritamesa.data.pref.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Ganti dengan IP komputer Anda jika menggunakan emulator/hp fisik
    // localhost/127.0.0.1 di emulator merujuk ke emulator itu sendiri
    // Gunakan 10.0.2.2 untuk emulator mengakses localhost komputer
    private const val BASE_URL = "http://10.0.2.2:8000/api/" 

    private var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        if (retrofit == null) {
            val sessionManager = SessionManager(context)

            // Logging Interceptor
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            // Auth Interceptor
            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                
                val token = sessionManager.getAuthToken()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                
                requestBuilder.header("Accept", "application/json")
                requestBuilder.method(original.method, original.body)
                
                val request = requestBuilder.build()
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
