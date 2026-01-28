package com.kgapp.kccjUltra.data.network

import com.kgapp.kccjUltra.data.api.ScoreApi
import com.kgapp.kccjUltra.data.store.UserPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://www.sales1.top/score/web/"

    fun createApi(preferences: UserPreferences): ScoreApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val headers = preferences.headersSnapshot()
            val requestBuilder = original.newBuilder()
            headers
                .filter { it.key.isNotBlank() }
                .forEach { item ->
                    requestBuilder.addHeader(item.key, item.value)
                }
            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
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
