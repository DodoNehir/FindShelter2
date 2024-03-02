package com.dodonehir.findshelter.network

import com.dodonehir.findshelter.model.ShelterResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val BASE_URL = "https://apis.data.go.kr/1741000/HeatWaveShelter3/"

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


interface ShelterPointService {
    @GET("getHeatWaveShelterList3")
    fun getShelterPoint(
        @Query("ServiceKey") key: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("type") type: String = "json",
//        @Query("year") year: Int = 2022,
        @Query("areaCd") areaCd: String,
        @Query("equptype") equptype: String
    ): Call<ShelterResponse>

    /**
     * equptype
     * 001:노인시설 002:복지회관 003:마을회관 004:보건소 005:주민센터
     * 006:면동사무소 007:종교시설 008:금융기관 009:정자 010:공원
     * 011:정자,파고라 012:공원 013:교량하부 014:나무그늘 015:하천둔치
     * 099:기타
     **/
}

object ShelterApi {
    val shelterPointService: ShelterPointService by lazy {
        retrofit.create(ShelterPointService::class.java)
    }
}