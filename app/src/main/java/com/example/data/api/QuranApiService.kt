package com.example.data.api

import com.example.config.AppConfig
import com.example.data.model.SurahDetailResponse
import com.example.data.model.SurahListResponse
import com.example.data.model.TafsirResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface QuranApiService {
    @GET("surat")
    suspend fun getSurahs(): Response<SurahListResponse>

    @GET("surat/{nomor}")
    suspend fun getSurahDetail(@Path("nomor") nomor: Int): Response<SurahDetailResponse>

    @GET("tafsir/{nomor}")
    suspend fun getTafsir(@Path("nomor") nomor: Int): Response<TafsirResponse>
}

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: QuranApiService by lazy {
        retrofit.create(QuranApiService::class.java)
    }
}
