package com.dodonehir.findshelter.db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    //  그냥 ViewModel상속받으면 application context를 상속받을 수가 없어서
    //  AndroidViewModel을 상속받은 것이다
    //  repository 덕분에 viewModel은 자기 일만 할 수 있다.
    private val repository = LocationRepository(application)

    fun getId(areaCode: String, equpType: String): Int {
        return repository.getId(areaCode, equpType)
    }

    fun insertLocation(location: Location) {
        repository.insertLocation(location)
    }

    fun getLocationData(locationId: Int) : List<LocationData> {
        return repository.getLocationData(locationId)
    }

    fun insertLocationData(locationData: LocationData) {
        repository.insertLocationData(locationData)
    }
}