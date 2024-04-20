package com.dodonehir.findshelter.repository

import android.app.Application
import com.dodonehir.findshelter.BuildConfig
import com.dodonehir.findshelter.db.AppDatabase
import com.dodonehir.findshelter.db.Location
import com.dodonehir.findshelter.db.LocationDao
import com.dodonehir.findshelter.db.LocationData
import com.dodonehir.findshelter.db.LocationDataDao
import com.dodonehir.findshelter.model.CodeResponse
import com.dodonehir.findshelter.model.GoogleAddressResponse
import com.dodonehir.findshelter.model.ShelterResponse
import com.dodonehir.findshelter.network.DongCodeApi
import com.dodonehir.findshelter.network.GMSApi
import com.dodonehir.findshelter.network.ShelterApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository(application: Application) {
    private val locationDao: LocationDao
    private val locationDataDao: LocationDataDao
//    private val locationList: LiveData<List<LocationData>>

    init {
        var db = AppDatabase.getInstance(application)
        locationDao = db.locationDao()
        locationDataDao = db.locationDataDao()
    }

    // db에 있는 지 없는 지 확인할 수 있는 검색.
    // 없으면 0 반환
    suspend fun getLocation(areacode: String, equptype: String): Location? {
        return withContext(Dispatchers.IO) {
            locationDao.getLocation(areacode, equptype)
        }
    }

    // 없을 때는 location insert
    suspend fun insertLocation(location: Location) {
        withContext(Dispatchers.IO) {
            locationDao.insert(location)
        }
    }

    // id 있을 때는 location data가 있는 지 검색
    // 없으면 빈 리스트 반환
    suspend fun getLocationData(locationId: Int): List<LocationData> {
        return withContext(Dispatchers.IO) {
            locationDataDao.getAll(locationId)
        }
    }

    // data insert 전에 있는 지 확인
    suspend fun isExistData(locationId: Int, restName: String): Boolean {
        return withContext(Dispatchers.IO) {
            locationDataDao.isExistData(locationId, restName)
        }
    }

    // id 없을 때는 location data 추가하기
    suspend fun insertLocationData(locationData: LocationData) {
        withContext(Dispatchers.IO) {
            locationDataDao.insertLocationData(locationData)
        }
    }


    suspend fun getAddressWithResult(
        latlng: String
    ): Result<GoogleAddressResponse> = kotlin.runCatching {

        val response =
            GMSApi.geoService.getResults(
                latlng,
                BuildConfig.MAPS_API_KEY,
                "ko",
                "sublocality_level_2"
            )

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