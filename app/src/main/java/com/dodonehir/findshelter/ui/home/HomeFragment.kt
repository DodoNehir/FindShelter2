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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dodonehir.findshelter.BuildConfig
import com.dodonehir.findshelter.R
import com.dodonehir.findshelter.databinding.FragmentHomeBinding
import com.dodonehir.findshelter.model.CodeResponse
import com.dodonehir.findshelter.model.GoogleAddressResponse
import com.dodonehir.findshelter.model.ShelterInfo
import com.dodonehir.findshelter.model.ShelterResponse
import com.dodonehir.findshelter.network.DongCodeApi
import com.dodonehir.findshelter.network.GMSApi
import com.dodonehir.findshelter.network.ShelterApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val TAG = javaClass.name
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var map: GoogleMap
    private val defaultLocation_GwanghwamunSquare = LatLng(37.575939, 126.976856)
    lateinit var lastKnownLocation: Location
    private var totalCount: Int? = null
    private var pageNumber = 1
    private var pageLoop = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
        homeViewModel.isLocationInitialized.observe(viewLifecycleOwner) { initialized ->
            if (initialized) {
                getKoreanAddress()
            }
        }

        homeViewModel.isGetAddressSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                getCode()
            }
        }

        homeViewModel.isGetCodeSuccess.observe(viewLifecycleOwner) {
            if (it) {
                getShelterLocations()
            }
        }

        homeViewModel.requestUpdateMap.observe(viewLifecycleOwner) {
            if (it) {
                Log.d(TAG, "Update map")
                updateMap()
            }
        }

        // Fragment에 map fragment를 표시
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync {
            Log.d(TAG, "GoogleMap ready.")
            map = it
            updateLocationUI()
            getDeviceLocation()
        }

        // 기기의 현재 위치 검색을 위함
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        // 위치 정보 엑세스 권한 허용 또는 거부 기회 제공
        getLocationPermission()

        return root
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
        homeViewModel.shelterInfoList.forEach {
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(it.la, it.lo))
                    .title(it.restName)
            )
        }

        // 다 끝나면 viewmodel의 update indicator, pageNumber, pageLoop를 initialize
        homeViewModel.finishedUpdateMap()
        pageNumber = 1
        pageLoop = 1
    }

    private fun getShelterLocations() {
        val shelterCall = ShelterApi.shelterService.getShelter(
            BuildConfig.SHELTER_ENCODING_KEY,
            pageNumber,
            3,
            "json",
            homeViewModel.code.toString(),
            "010"
        )

        shelterCall.enqueue(object : Callback<ShelterResponse> {
            override fun onResponse(
                call: Call<ShelterResponse>,
                response: Response<ShelterResponse>
            ) {
                Log.d(TAG, "getShelterLocations: succeed")
                val shelterPointResponse = response.body()
                if (pageNumber == 1) {
                    // 가장 처음 request할 때 total count를 저장하고, loop를 계산한다.
                    totalCount =
                        shelterPointResponse?.HeatWaveShelter?.get(0)?.head?.get(0)?.totalCount
                    Log.d(TAG, "total count: $totalCount")
                    if (totalCount != null) {
                        pageLoop = totalCount!! / 3
                        if (totalCount!! % 3 != 0) {
                            pageLoop += 1
                        }
                    }
                }
                // totalCount가 null이 아닐 때 shelterInfo 저장
                if (totalCount != null) {
                    shelterPointResponse?.HeatWaveShelter?.get(1)?.row?.forEach {
                        val shelterInfo = ShelterInfo(
                            it.restname,
                            it.la,
                            it.lo
                        )
                        homeViewModel.shelterInfoList.add(shelterInfo)
                    }
                    Log.d(TAG, "3 shelter info saved")
                    if (pageNumber < pageLoop) {
                        pageNumber++
                        getShelterLocations()
                    } else {
                        homeViewModel.requestUpdateMap()
                    }
                } else {
                    // resultMsg에 데이터없음 에러 라고 올 때
                    Snackbar.make(binding.root.rootView, "검색 결과가 없습니다.", Snackbar.LENGTH_LONG)
                }
            }

            override fun onFailure(call: Call<ShelterResponse>, t: Throwable) {
                Log.e(TAG, "getShelterLocations: failed")
                t.message?.let { Log.e(TAG, it) }
            }

        })
    }

    private fun getCode() {
        val codeCall = DongCodeApi.dongCodeService.getCode(
            homeViewModel.city,
            homeViewModel.district,
            homeViewModel.dong
        )

        codeCall.enqueue(object : Callback<List<CodeResponse>> {
            override fun onResponse(
                call: Call<List<CodeResponse>>,
                response: Response<List<CodeResponse>>
            ) {
                val codeResponse = response.body()
                val code = codeResponse?.get(0)?.code
                if (code != null) {
                    Log.d(TAG, "getCode: $code")
                    homeViewModel.getCodeSuccess(code)
                }
            }

            override fun onFailure(call: Call<List<CodeResponse>>, t: Throwable) {
                Log.e(TAG, "getCode: failed")
                t.message?.let { Log.e(TAG, it) }
            }

        })
    }

    private fun getKoreanAddress() {
        var latitude = lastKnownLocation.latitude.toString()
        var longitude = lastKnownLocation.longitude.toString()
        val geoCall = GMSApi.geoService.getResults(
            "${latitude},${longitude}",
            BuildConfig.MAPS_API_KEY,
            "ko",
            "street_address"
        )
        geoCall.enqueue(object : Callback<GoogleAddressResponse> {
            override fun onResponse(
                call: Call<GoogleAddressResponse>,
                response: Response<GoogleAddressResponse>
            ) {
                val googleAddressResponse = response.body()

                if (googleAddressResponse != null) {
                    val addressParts =
                        (googleAddressResponse.results[0].formatted_address).split(" ")
                    val city = addressParts[1]
                    val district = addressParts[2]
                    val dong = addressParts[3]
                    Log.d(TAG, "getKoreanAddress: ${city}, ${district}, ${dong}")
                    homeViewModel.getAddressSuccess(city, district, dong)
                } else {
                    Log.d(TAG, "getKoreanAddress: 응답 내용 status 가 null입니다")
                }
            }

            override fun onFailure(call: Call<GoogleAddressResponse>, t: Throwable) {
                Log.e(TAG, "getKoreanAddress: Failed")
                t.message?.let { Log.e(TAG, it) }
            }

        })
    }

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