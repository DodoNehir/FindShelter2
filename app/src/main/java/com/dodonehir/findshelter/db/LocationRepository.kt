package com.dodonehir.findshelter.db

import android.app.Application

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
    fun getLocation(areacode: String, equptype: String): Location? {
        val location = locationDao.getLocation(areacode, equptype)
        return location
    }

    // 없을 때는 location insert
    fun insertLocation(location: Location) {
        locationDao.insert(location)
    }

    // id 있을 때는 location data가 있는 지 검색
    // 없으면 빈 리스트 반환
    fun getLocationData(locationId: Int): List<LocationData> {
        return locationDataDao.getAll(locationId)
    }

    // data insert 전에 있는 지 확인
    fun isExistData(locationId: Int, restName: String): Boolean {
        return locationDataDao.isExistData(locationId, restName)
    }

    // id 없을 때는 location data 추가하기
    fun insertLocationData(locationData: LocationData) {
        locationDataDao.insertLocationData(locationData)
    }

}