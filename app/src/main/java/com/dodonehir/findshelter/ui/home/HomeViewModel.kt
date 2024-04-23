package com.dodonehir.findshelter.ui.home

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dodonehir.findshelter.db.LocationData
import com.dodonehir.findshelter.repository.LocationRepository
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = javaClass.name
    private val repository = LocationRepository(application)


    private val _isLocationInitialized = MutableLiveData<Boolean>()
    val isLocationInitialized: LiveData<Boolean> = _isLocationInitialized

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location> = _lastLocation


    val locationDataMutableList = mutableListOf<LocationData>()

    private val _requestUpdateMap = MutableLiveData<Boolean>()
    val requestUpdateMap: LiveData<Boolean> = _requestUpdateMap

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    var locationPermissionGranted = false
    var cameraPosition: CameraPosition? = null

    lateinit var city: String
    lateinit var district: String
    lateinit var dong: String

    var areaCode: Long = 0

    //    var pageNo = 1
    var numOfRows = 4
    lateinit var equpType: String
    var totalCount: Int? = 0

    var locationId: Int = 0

    private var isFetching = false

    fun setLocationInitialized(init: Boolean, location: Location) {
        _isLocationInitialized.value = init
        if (init) {
            _lastLocation.value = location
        }
    }

    fun fetchData(latlng: String, equp: String) {
        if (isFetching) {
            return
        }
        isFetching = true

        equpType = equp
        locationDataMutableList.clear()

        // launch 는 동기
        viewModelScope.launch {
            try {
                repository.getAddressWithResult(latlng)
                    .onSuccess { googleAddressResponse ->
                        Log.d(TAG, "get Address Response: $googleAddressResponse")

                        val addressParts =
                            (googleAddressResponse.results[0].formatted_address).split(" ")
                        city = addressParts[1]
                        district = addressParts[2]
                        dong = addressParts[3]
                        Log.d(TAG, "getKoreanAddress: ${city}, ${district}, ${dong}")
                    }
                    .onFailure {
                        Log.e(TAG, "get korean address failed: ${it.message}")
                        this@launch.cancel()
                    }


                repository.getCode(city, district, dong)
                    .onSuccess {
                        Log.d(TAG, "get Code Response: ${it.get(0)}")

                        val codeResponse = it.get(0)
                        areaCode = codeResponse.code
                    }
                    .onFailure {
                        Log.e(TAG, "get code failed: ${it.message}")
                        this@launch.cancel()
                    }

                // DB get
                val location = repository.getLocation(areaCode.toString(), equpType)
                if (location == null) {
                    Log.d(TAG, "Location ID doesn't exist.")
                    callShelterRequest()
                } else if (location.totalCount == 0) {
                    Log.d(TAG, "Location ID is exist. but There's no result")
                    _errorLiveData.value = "해당되는 쉼터가 없습니다. 다른 유형을 선택해주세요"
                } else {
                    Log.d(TAG, "Location ID is exist. ID: ${location.id}")
                    getLocationFromDB(location.id)
                }

                // TODO
                //  통신 후에도, db에서 가져온 후에도 순서대로 잘 되는 지 확인하기
                requestUpdateMap()

            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred: ${e.message}")
                _errorLiveData.value = "Exception occurred: ${e.message}"
            } finally {
                isFetching = false
            }

        }

    }

    suspend fun callShelterRequest() {
        // TODO
        //  서버가 상태가 좋지 않으면 기획적으로 커버하자.
        //  필터 걸어서 요청해오는 방법?
        repository.getShelter(1, numOfRows, areaCode.toString(), equpType)
            .onSuccess {
                totalCount = it.HeatWaveShelter?.get(0)?.head?.get(0)?.totalCount
                Log.d(TAG, "get Shelter Response - totalCount: $totalCount")

                // totalCount 값이 Null 이라면 해당 쉼터가 없으므로 0으로 대체해서 넣는다
                val location =
                    com.dodonehir.findshelter.db.Location(
                        areaCode.toString(),
                        equpType,
                        totalCount ?: 0
                    )

                // db insert
                repository.insertLocation(location)
                Log.d(TAG, "Insesrt new location to DB")

                // db get (확인하기)
                locationId = repository.getLocation(areaCode.toString(), equpType)?.id ?: 0
                Log.d(TAG, "this location and equpType ID: $locationId")

                // db에 locationData 저장
                it.HeatWaveShelter?.get(1)?.row?.forEach {
                    // 값이 0.0 일 때나 중복될 떄는 저장하지 않는다.
//                    if (locationId != 0 && it.la != 0.0) {
                    if (it.la != 0.0) {
                        val locationData =
                            LocationData(it.restname, it.la, it.lo, locationId)

                        // 그리고 ViewModel 의 MutableList 에도 저장한다.
                        if (!repository.isExistData(locationId, it.restname)) {
                            Log.d(TAG, "Inserted shelter's name is: ${it.restname}")
                            repository.insertLocationData(locationData)
                            locationDataMutableList.add(locationData)
                        }
                    }
                }

                // 남은 페이지가 있으면 반복
                if (totalCount != null) {
                    if (totalCount!! > numOfRows) {
                        callShelterLoop(totalCount!!)
                    }
                } else {
                    Log.d(TAG, "데이터 없음")
                    _errorLiveData.value = "해당되는 쉼터가 없습니다. 다른 유형을 선택해주세요"
                }

            }
            .onFailure {
                Log.e(TAG, "callShelterRequest failed")
                it.message?.let { Log.e(TAG, it) }
            }
    }

    suspend fun callShelterLoop(totalCount: Int) {
        var pageLoop = (totalCount + numOfRows - 1) / numOfRows

        for (i in 2..pageLoop) {
            repository.getShelter(i, numOfRows, areaCode.toString(), equpType)
                .onSuccess {
                    // db에 locationData 저장
                    it.HeatWaveShelter?.get(1)?.row?.forEach {
                        if (it.la != 0.0) {
                            val locationData =
                                LocationData(it.restname, it.la, it.lo, locationId)

                            if (!repository.isExistData(locationId, it.restname)) {
                                Log.d(TAG, "Inserted shelter's name is: ${it.restname}")
                                repository.insertLocationData(locationData)
                                locationDataMutableList.add(locationData)
                            }
                        }
                    }
                }
                .onFailure {
                    Log.e(TAG, "callShelterRequest failed")
                    it.message?.let { Log.e(TAG, it) }
                }
        }
    }

    suspend fun getLocationFromDB(locationId: Int) {
        Log.d(TAG, "===== Get Location Data from DB =====")
        repository.getLocationData(locationId).forEach {
            Log.d(TAG, "restname: ${it.restname}")
            locationDataMutableList.add(it)
        }
        Log.d(TAG, "=====================================")
    }

    fun requestUpdateMap() {
        _requestUpdateMap.value = true
    }

    fun finishedUpdateMap() {
        _requestUpdateMap.value = false
    }
}