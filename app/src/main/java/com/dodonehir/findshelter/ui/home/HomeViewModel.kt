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

    var locationPermissionGranted = false
    var cameraPosition: CameraPosition? = null

    lateinit var city: String
    lateinit var district: String
    lateinit var dong: String

    var areaCode: Long = 0

    //    var pageNo = 1
    var numOfRows = 3
    lateinit var equpType: String
    var totalCount: Int? = 0

    var locationId: Int = 0


    fun setLocationInitialized(init: Boolean, location: Location) {
        _isLocationInitialized.value = init
        if (init) {
            _lastLocation.value = location
        }
    }

    fun fetchData(latlng: String, equp: String) {
        equpType = equp
        // launch 는 동기
        val job = viewModelScope.launch {
            // Make the network call and suspend execution until it finished
            // TODO
            //  launch 는 예외가 발생하면 부모에게 전달한다.
            //  그래서 부모 코루틴도 자식에게서 발생한 오류와 똑같은 오류로 취소된다.
            //  이로 인해 부모의 나머지 자식도 모두 취소된다.
            //  자식들이 모두 취소되고 나면 부모는 코루틴 트리의 윗부분으로 예외를 전달한다.
            repository.getAddressWithResult(latlng)
                .onSuccess { googleAddressResponse ->
                    Log.d(TAG, "코루틴 + Result 예시: $googleAddressResponse")

                    val addressParts =
                        (googleAddressResponse.results[0].formatted_address).split(" ")
                    city = addressParts[1]
                    district = addressParts[2]
                    dong = addressParts[3]
                    Log.d(TAG, "getKoreanAddress: ${city}, ${district}, ${dong}")
                }
                .onFailure {
                    Log.e(TAG, "get korean address failed")
                }


            repository.getCode(city, district, dong)
                .onSuccess {
                    Log.d(TAG, "coroutine + Result : ${it.get(0)}")

                    val codeResponse = it.get(0)
                    areaCode = codeResponse.code
                }
                .onFailure {
                    Log.e(TAG, "get code failed")
                }

            // DB get
            val location = repository.getLocation(areaCode.toString(), equpType)
            if (location == null) {
                Log.d(TAG, "location Id doesn't exist.")
                callShelterRequest()
            } else if (location.totalCount == 0) {
                Log.d(TAG, "location Id is exist. but There's no result")
                // TODO Main에서 검색 결과 없다고 메시지 띄우도록 flow 만들어서 보내기
            } else {
                Log.d(TAG, "location Id is ${location.id}")
                getLocationFromDB(location.id)
            }

            // TODO
            //  통신 후에도, db에서 가져온 후에도 순서대로 잘 되는 지 확인하기
            requestUpdateMap()

        }

        // 한 번만 실행되도록
        if (job.isCompleted)
            job.cancel()
    }

    suspend fun callShelterRequest() {
        // TODO
        //  서버가 상태가 좋지 않으면 기획적으로 커버하자.
        //  필터 걸어서 요청해오는 방법?
        repository.getShelter(1, numOfRows, areaCode.toString(), equpType)
            .onSuccess {
                totalCount = it.HeatWaveShelter?.get(0)?.head?.get(0)?.totalCount
                Log.d(TAG, "TotalCount: $totalCount")

                // totalCount 값이 Null 이라면 해당 쉼터가 없으므로 0으로 대체해서 넣는다
                val location =
                    com.dodonehir.findshelter.db.Location(
                        areaCode.toString(),
                        equpType,
                        totalCount ?: 0
                    )

                // db insert
                repository.insertLocation(location)
                Log.d(TAG, "Insesrt new location")

                // db get (확인하기)
                locationId = repository.getLocation(areaCode.toString(), equpType)?.id ?: 0
                Log.d(TAG, "Get Location. new value id is $locationId")

                // db에 locationData 저장
                it.HeatWaveShelter?.get(1)?.row?.forEach {
                    // 값이 0.0 일 때나 중복될 떄는 저장하지 않는다.
//                    if (locationId != 0 && it.la != 0.0) {
                    if (it.la != 0.0) {
                        val locationData =
                            LocationData(it.restname, it.la, it.lo, locationId)

                        // 그리고 ViewModel 의 MutableList 에도 저장한다.
                        if (!repository.isExistData(locationId, it.restname)) {
                            Log.d(TAG, "Insert one LocationData")
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
                    // TODO Main 스레드에서 알 수 있도록 flow 로 전달해야 한다
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
                                Log.d(TAG, "Insert one LocationData")
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
        Log.d(TAG, "request update map")
    }

    fun finishedUpdateMap() {
        _requestUpdateMap.value = false
    }
}