package com.dodonehir.findshelter.ui.home

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dodonehir.findshelter.db.LocationData
import com.dodonehir.findshelter.network.RetrofitRepository
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.coroutines.launch

class HomeViewModel() : ViewModel() {

    private val TAG = javaClass.name
    private val repository = RetrofitRepository()


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

    var pageNo = 1
    var numOfRows = 3
    var areaCd = "10121211"
    var equpType = "001"
    var totalCount = 0


    fun setLocationInitialized(init: Boolean, location: Location) {
        _isLocationInitialized.value = init
        if (init) {
            _lastLocation.value = location
        }
    }

    fun fetchData(latlng: String) {
        // launch 는 동기
        viewModelScope.launch {
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

            // TODO 요청 전에 db와 비교해봐야 한다.
            //  있으면 db껄 쓰도록 하고   없으면 request한다.  그러면 homefragment에서 locationViewModel을 사용해야 하는 건지
            //  여기서 locationViewModel을 사용하는건지..???? 뷰모델이 뷰모델을 사용하나?
            //  서버가 상태가 좋지 않으면 기획적으로 커버하자.
            //  필터 걸어서 요청해오는 방법?
            repository.getShelter(pageNo, numOfRows, areaCd, equpType)
                .onSuccess {

                }
                .onFailure {

                }
        }
    }

    fun requestUpdateMap() {
        _requestUpdateMap.value = true
    }

    fun finishedUpdateMap() {
        _requestUpdateMap.value = false
    }
}