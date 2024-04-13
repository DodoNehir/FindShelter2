package com.dodonehir.findshelter.network

import com.dodonehir.findshelter.model.GoogleAddressResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://maps.googleapis.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
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

interface GeoService {
    @GET("maps/api/geocode/json")
    suspend fun getResults(
        @Query("latlng") latlng: String,
        @Query("key") API_KEY: String,
        @Query("language") language: String, // ko
        @Query("result_type") resultType: String, // sublocality_level_2
    ): Response<GoogleAddressResponse>
}

object GMSApi {

    val geoService: GeoService by lazy {
        retrofit.create(GeoService::class.java)
    }

}