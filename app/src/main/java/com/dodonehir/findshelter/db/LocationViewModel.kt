package com.dodonehir.findshelter.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    //  그냥 ViewModel상속받으면 application context를 상속받을 수가 없어서
    //  AndroidViewModel을 상속받은 것이다
    //  repository 덕분에 viewModel은 자기 일만 할 수 있다.
    private val repository = LocationRepository(application)

    fun getLocation(areaCode: String, equpType: String): Location? {
        return repository.getLocation(areaCode, equpType)
    }

    fun insertLocation(location: Location) {
        repository.insertLocation(location)
    }

    fun getLocationData(locationId: Int) : List<LocationData> {
        return repository.getLocationData(locationId)
    }

    fun isExistData(locationId: Int, restName: String) : Boolean {
        return repository.isExistData(locationId, restName)
    }

    fun insertLocationData(locationData: LocationData) {
        repository.insertLocationData(locationData)
    }
}