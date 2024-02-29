package com.dodonehir.findshelter.network

import com.dodonehir.findshelter.model.GoogleAddressResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://maps.googleapis.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface GeoService {
    @GET("maps/api/geocode/json")
    fun getResults(
        @Query("latlng") lalo: String,
        @Query("key") API_KEY: String,
        @Query("language") language: String, // ko
        @Query("location-type") locationType: String, // ROOFTOP
        // "ROOFTOP"는 상세 주소 수준까지 Google의 위치 정보가 정확한 주소만 반환합니다.
    ): Call<GoogleAddressResponse>
}

object GMSApi {

    val geoService: GeoService by lazy {
        retrofit.create(GeoService::class.java)
    }

}