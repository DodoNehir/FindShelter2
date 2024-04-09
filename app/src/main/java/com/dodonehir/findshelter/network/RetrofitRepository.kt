package com.dodonehir.findshelter.network

import com.dodonehir.findshelter.BuildConfig
import com.dodonehir.findshelter.model.CodeResponse
import com.dodonehir.findshelter.model.GoogleAddressResponse
import com.dodonehir.findshelter.model.ShelterResponse

class RetrofitRepository {

    suspend fun getAddressWithResult(
        latlng: String
    ): Result<GoogleAddressResponse> = kotlin.runCatching {

        val response =
            GMSApi.geoService.getResults(latlng, BuildConfig.MAPS_API_KEY, "ko", "street_address")

        if (response.isSuccessful) {
            response.body() ?: throw RuntimeException("이럴 수가...")
        } else {
            throw RuntimeException("통신 과정에서 에러가 발생했어요.")
        }
    }

    suspend fun getCode(
        city: String,
        district: String,
        dong: String
    ): Result<List<CodeResponse>> = kotlin.runCatching {

        val response = DongCodeApi.dongCodeService.getCode(city, district, dong)

        if (response.isSuccessful) {
            response.body() ?: throw RuntimeException("이럴 수가...")
        } else {
            throw RuntimeException("통신 과정에서 에러가 발생했어요.")
        }
    }


    suspend fun getShelter(
        pageNo: Int,
        numOfRows: Int,
        areaCd: String,
        equptype: String
    ): Result<ShelterResponse> = kotlin.runCatching {

        val response =
            ShelterApi.shelterService.getShelter(
                BuildConfig.SHELTER_ENCODING_KEY,
                pageNo,
                numOfRows,
                "json",
                areaCd,
                equptype
            )

        if (response.isSuccessful) {
            response.body() ?: throw RuntimeException("이럴 수가...")
        } else {
            throw RuntimeException("통신 과정에서 에러가 발생했어요.")
        }
    }
}