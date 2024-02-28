package com.dodonehir.findshelter.ui.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    var locationPermissionGranted = false
    var cameraPosition: CameraPosition? = null
    lateinit var lastKnownLocation: Location
}