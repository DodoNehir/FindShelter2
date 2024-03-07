package com.dodonehir.findshelter.network

import com.dodonehir.findshelter.model.CodeResponse
import com.dodonehir.findshelter.util.NullToEmptyStringAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

//private const val BASE_URL = "http://192.168.0.144:8080/"
private const val BASE_URL = "http://172.30.1.71:8080/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .add(NullToEmptyStringAdapter())
    .build()

private val interceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
private val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface DongCodeService {
    @GET("pd")
    fun getCode(
        @Query("city") city: String,
        @Query("district") district: String,
        @Query("dong") dong: String
    ): Call<List<CodeResponse>>
}

object DongCodeApi {
    val dongCodeService: DongCodeService by lazy {
        retrofit.create(DongCodeService::class.java)
    }
}