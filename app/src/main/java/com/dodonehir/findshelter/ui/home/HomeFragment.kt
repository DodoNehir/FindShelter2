package com.dodonehir.findshelter.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dodonehir.findshelter.BuildConfig
import com.dodonehir.findshelter.R
import com.dodonehir.findshelter.databinding.FragmentHomeBinding
import com.dodonehir.findshelter.db.LocationData
import com.dodonehir.findshelter.db.LocationViewModel
import com.dodonehir.findshelter.model.CodeResponse
import com.dodonehir.findshelter.model.GoogleAddressResponse
import com.dodonehir.findshelter.model.ShelterResponse
import com.dodonehir.findshelter.network.DongCodeApi
import com.dodonehir.findshelter.network.GMSApi
import com.dodonehir.findshelter.network.RetrofitRepository
import com.dodonehir.findshelter.network.ShelterApi
import com.dodonehir.findshelter.ui.settings.dataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val TAG = javaClass.name
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var map: GoogleMap
    private val defaultLocation_GwanghwamunSquare = LatLng(37.575939, 126.976856)
    lateinit var lastKnownLocation: Location
    private var totalCount: Int? = null
    private var numOfRows = 3
    private var pageNumber = 1
    private var pageLoop = 1
    private var equptype = "001"
    private var areaCode = "1111"
    private var locationId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 기기의 현재 위치 검색을 위함
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        // 위치 정보 엑세스 권한 허용 또는 거부 기회 제공
        getLocationPermission()

        // HomeFragment 위에 map fragment 표시
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            Log.d(TAG, "GoogleMap ready.")
            map = it
            updateLocationUI()
            getDeviceLocation()
        }




        // dataStore에 저장된 설정값 가져오기
        val EQUPTYPE = stringPreferencesKey("equptype")
//        viewLifecycleOwner.lifecycleScope.launch { // 대체 무슨 차이인지..
        lifecycleScope.launch {
            try {
                equptype = requireContext().dataStore.data.first()[EQUPTYPE].toString()
                Log.d(TAG, "saved equptype: $equptype ")
                homeViewModel.equpType = equptype
            } catch (e: IOException) {
                Log.e(TAG, "IOException occurred: ${e.message}")
            }
        }


        // 지도 initialize 확인
        homeViewModel.isLocationInitialized.observe(viewLifecycleOwner) { initialized ->
            if (initialized) {
                var latitude = lastKnownLocation.latitude.toString()
                var longitude = lastKnownLocation.longitude.toString()
                homeViewModel.fetchData("${latitude},${longitude}")
            }
        }



//        // 현위치 한글 주소 확인
//        homeViewModel.isGetAddressSuccess.observe(viewLifecycleOwner) { success ->
//            if (success) {
//                getCode()
//            }
//        }
//
//        // 현위치 동코드 확인
//        homeViewModel.isGetCodeSuccess.observe(viewLifecycleOwner) {
//            if (it) {
//                getShelterLocations()
//            }
//        }
//
//        homeViewModel.requestUpdateMap.observe(viewLifecycleOwner) {
//            if (it) {
//                Log.d(TAG, "Update map")
//                updateMap()
//            }
//        }



        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        map.let { map ->
            homeViewModel.cameraPosition = map.cameraPosition
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateMap() {
        // shelterInfo pin point map에 표시하기
        homeViewModel.locationDataMutableList.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(it.la, it.lo))
                    .title(it.restname)
            )
        }

        // 다 끝나면 viewmodel의 update indicator, pageNumber, pageLoop를 initialize
        homeViewModel.finishedUpdateMap()
        pageNumber = 1
        pageLoop = 1
    }

/*
    private fun getShelterLocations() {
        //  equptype은 dataStore에서 가져온 상태이고
        //  areaCode는 homeViewModel에 저장된 상태임
        areaCode = homeViewModel.code.toString()
        // 새로운 목록 저장 전에 clear
        homeViewModel.locationDataMutableList.clear()

        lifecycleScope.launch(Dispatchers.IO) {
            val location = locationViewModel.getLocation(areaCode, equptype)
            locationId = location?.id ?: 0
            if (location == null) {
                // db에 없음
                Log.d(TAG, "location Id doesn't exist.")
                callShelterRequest()
            } else if (location.totalCount == 0) {
                // 해당 장소 없음
                Log.d(TAG, "location Id is exist. but There's no result")
                withContext(Dispatchers.Main) {
                    Snackbar.make(
                        binding.root.rootView,
                        "검색 결과가 없습니다.",
                        Snackbar.LENGTH_LONG
                    )
                }
            } else {
                // 데이터 있음
                Log.d(TAG, "location Id is $locationId")
                getLocationFromDB()
            }
        }

        // requestupdateMap()은 각각 요청함. request는 시기가 다를 것 같아서..
    }

    private fun getLocationFromDB() {
        Log.d(TAG, "===== Get Location Data from DB =====")
        locationViewModel.getLocationData(locationId).forEach {
            Log.d(TAG, "restname: ${it.restname}")
            homeViewModel.locationDataMutableList.add(it)
        }
        Log.d(TAG, "=====================================")
        GlobalScope.launch(Dispatchers.Main) {
            homeViewModel.requestUpdateMap()
        }
    }

    private fun callShelterRequest() {
        // timeout 때문에 3개만 요청
        val shelterCall = ShelterApi.shelterService.getShelter(
            BuildConfig.SHELTER_ENCODING_KEY,
            pageNumber,
            numOfRows,
            "json",
            homeViewModel.code.toString(),
            equptype
        )

        shelterCall.enqueue(object : Callback<ShelterResponse> {
            override fun onResponse(
                call: Call<ShelterResponse>,
                response: Response<ShelterResponse>
            ) {

                val shelterPointResponse = response.body()

                lifecycleScope.launch(Dispatchers.IO) {
                    // 가장 처음 request할 때 totalCount를 저장하고, loop를 계산한다.
                    if (pageNumber == 1) {
                        totalCount =
                            shelterPointResponse?.HeatWaveShelter?.get(0)?.head?.get(0)?.totalCount
                        Log.d(TAG, "Call request shelter succeed. total count: $totalCount")
                        if (totalCount != null) {

                            // db에 location 객체를 넣는다
                            val location =
                                com.dodonehir.findshelter.db.Location(
                                    areaCode,
                                    equptype,
                                    totalCount!!
                                )
                            locationViewModel.insertLocation(location)

//                            locationId = locationViewModel.getLocation(areaCode, equptype)?.id ?: 0
//                            Log.d(TAG, "Insesrt new locationID. The value is $locationId")

                            pageLoop = totalCount!! / 3
                            if (totalCount!! % 3 != 0) {
                                pageLoop += 1
                            }
                        }
                    }

                    // totalCount가 null이 아닐 때 shelterInfo 저장
                    if (totalCount != null) {
                        shelterPointResponse?.HeatWaveShelter?.get(1)?.row?.forEach {
                            // la, lo 값이 0.0으로 들어올 때는 저장하지 않고
                            // 이름, la, lo가 모두 동일한 경우에도 중복해서 저장되지 않음(스키마)
                            if (locationId != 0 && it.la != 0.0) {
                                val locationData =
                                    LocationData(it.restname, it.la, it.lo, locationId)

                                // db에 중복해서 넣지 않도록 한다 & homeViewModel에도 추가한다
                                if (!locationViewModel.isExistData(locationId, it.restname)) {
                                    Log.d(TAG, "Insert one LocationData")
                                    locationViewModel.insertLocationData(locationData)
                                    homeViewModel.locationDataMutableList.add(locationData)
                                }
                            }
                        }
                        if (pageNumber < pageLoop) {
                            Log.d(TAG, "progress $pageNumber/$pageLoop")
                            pageNumber++
                            // 다시 request
                            callShelterRequest()
                        } else {
                            // request 반복 끝냄
                            Log.d(TAG, "Finish call request shelter")
                            GlobalScope.launch(Dispatchers.Main) {
                                homeViewModel.requestUpdateMap()
                            }

                        }
                    } else {
                        // resultMsg에 데이터없음 에러 라고 올 때
                        Log.d(TAG, "데이터 없음")
                        val location = com.dodonehir.findshelter.db.Location("0", "0", 0)

                        // TODO 중복으로 들어가는지 나중에 확인하기
                        //  스낵바를 띄우자..
                        Log.d(TAG, "Insert Location")
                        locationViewModel.insertLocation(location)

                        withContext(Dispatchers.Main) {
                            Snackbar.make(
                                binding.root.rootView,
                                "검색 결과가 없습니다.",
                                Snackbar.LENGTH_LONG
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ShelterResponse>, t: Throwable) {
                Log.e(TAG, "callShelterRequest failed")
                t.message?.let { Log.e(TAG, it) }
            }

        })
    }


 */

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /**
         * 내 위치 표시.
         * rare 하게 위치 못 찾을 때도 있을 수 있음
         */
        try {
            if (homeViewModel.locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 맵 카메라를 현재 위치로 이동
                        Log.d(TAG, "getDeviceLocation: 맵 카메라를 현재 위치로 이동")
                        lastKnownLocation = task.result
                        homeViewModel.setLocationInitialized(true, lastKnownLocation)
                        if (homeViewModel.cameraPosition != null) {
                            map.moveCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    homeViewModel.cameraPosition!!
                                )
                            )
                        } else {
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation.latitude,
                                        lastKnownLocation.longitude
                                    ),
                                    DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                defaultLocation_GwanghwamunSquare, DEFAULT_ZOOM.toFloat()
                            )
                        )
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    fun getLocationPermission() {
        /**
         * 위치 정보 엑세스 권한 요청.
         * 결과는 onRequestPermissionResult에서 처리한다.
         */
        if (ContextCompat.checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "getLocationPermission: permission granted 확인함")
            homeViewModel.locationPermissionGranted = true
        } else {
            Log.d(TAG, "getLocationPermission: permission 요청")
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        homeViewModel.locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    homeViewModel.locationPermissionGranted = true
                    Log.d(TAG, "onRequestPermissionsResult: locationPermission Granted ")
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            if (homeViewModel.locationPermissionGranted) {
                Log.d(TAG, "내 위치(GPS) 버튼 활성화")
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                Log.d(TAG, "내 위치(GPS) 버튼 비활성화")
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    companion object {
        /**
         * Request code for location permission
         */
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        private const val DEFAULT_ZOOM = 18
    }
}