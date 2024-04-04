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


    // TODO: 검색을 어떻게 하지?
    //  areacode와 equptype을 알면 id를 알 수 있다.
    //  알 수 없으면 db에 없는 것이므로 requeest를 요청하고 결과를 db에 저장해야 한다.
    //  알 수 있으면 db에 있는 것이므로 id 검색한다.
    //  (Location) id == (LocationData) location_id
    //  location_id로 검색되는 모든 것들이 해당 area_code와 equp_type에 해당하는 쉼터 위치들임!
}