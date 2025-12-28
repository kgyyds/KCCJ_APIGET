package com.kgapp.kccjapi.net

import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Net {

    private const val BASE_URL = "https://www.sales1.top/score/interface/"

    // 🚀 关键：放开 OkHttp 并发限制
    private val dispatcher = Dispatcher().apply {
        maxRequests = 512
        maxRequestsPerHost = 512
    }

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            // 🚀 大连接池，减少 TCP 建立
            .connectionPool(
                ConnectionPool(
                    128,
                    5,
                    TimeUnit.MINUTES
                )
            )
            // 超时别太小
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            // 🚫 并发测速时，禁止日志
            // .addInterceptor(HttpLoggingInterceptor().apply {
            //     level = HttpLoggingInterceptor.Level.BASIC
            // })
            .retryOnConnectionFailure(true)
            .build()
    }

    val api: ScoreApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScoreApi::class.java)
    }
}