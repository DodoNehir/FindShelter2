package com.dodonehir.findshelter.ui.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition

class HomeViewModel : ViewModel() {

    private val _isLocationInitialized = MutableLiveData<Boolean>()
    val isLocationInitialized: LiveData<Boolean> = _isLocationInitialized

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location> = _lastLocation

    private val _isGetAddressSuccess = MutableLiveData<Boolean>()
    val isGetAddressSuccess: LiveData<Boolean> = _isGetAddressSuccess

    private val _isGetCodeSuccess = MutableLiveData<Boolean>()
    val isGetCodeSuccess: LiveData<Boolean> = _isGetCodeSuccess

    var locationPermissionGranted = false
    var cameraPosition: CameraPosition? = null
    lateinit var city: String
    lateinit var district: String
    lateinit var dong: String
    var code: Long = 0

    fun setLocationInitialized(init: Boolean, location: Location) {
        _isLocationInitialized.value = init
        if (init) {
            _lastLocation.value = location
        }
    }

    fun getAddressSuccess(city: String, district: String, dong: String) {
        this.city = city
        this.district = district
        this.dong = dong
        _isGetAddressSuccess.value = true
    }

    fun getCodeSuccess(code: Long) {
        this.code = code
        _isGetCodeSuccess.value = true
    }
}