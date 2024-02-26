package com.dodonehir.findshelter.network

import com.dodonehir.findshelter.model.ShelterPointResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val BASE_URL = "http://apis.data.go.kr/1741000/HeatWaveShelter2/"

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()


interface ShelterPointService {
    @GET("getHeatWaveShelterList2")
    fun getShelterPoint(
        @Query("serviceKey", encoded = true) key: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 5,
        @Query("type") type: String = "xml",
        @Query("year") year: Int = 2022,
        @Query("areaCd") areaCd: String,
        @Query("equptype") equptype: String
    ): Call<ShelterPointResponse>

    /**
     * equptype
     * 001:노인시설 002:복지회관 003:마을회관 004:보건소 005:주민센터
     * 006:면동사모소 007:종교시설 008:금융기관 009:정자 010:공원
     * 011:정자,파고라 012:공원 013:교량하부 014:나무그늘 015:하천둔치
     * 099:기타
     **/

    companion object {
        fun create(): ShelterPointService {

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ShelterPointService::class.java)
        }
    }


}