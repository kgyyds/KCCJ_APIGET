package com.kgapp.kccjUltra.data.network

import com.kgapp.kccjUltra.data.api.ScoreApi
import com.kgapp.kccjUltra.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://www.sales1.top/score/web/"

    fun createApi(): ScoreApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val cookieInterceptor = Interceptor { chain ->
            val original = chain.request()
            val sessionId = SessionManager.getSession()
            val requestBuilder = original.newBuilder()
            if (!sessionId.isNullOrBlank()) {
                requestBuilder.addHeader("Cookie", "JSESSIONID=$sessionId")
            }
            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(cookieInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ScoreApi::class.java)
    }
}
