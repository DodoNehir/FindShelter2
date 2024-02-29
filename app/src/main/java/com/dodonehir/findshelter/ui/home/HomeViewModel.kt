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

    var locationPermissionGranted = false
    var cameraPosition: CameraPosition? = null

    fun setLocationInitialized(init: Boolean, location: Location) {
        _isLocationInitialized.value = init
        if (init) {
            _lastLocation.value = location
        }
    }
}