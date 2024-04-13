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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dodonehir.findshelter.R
import com.dodonehir.findshelter.databinding.FragmentHomeBinding
import com.dodonehir.findshelter.ui.settings.dataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val TAG = javaClass.name
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var map: GoogleMap
    private lateinit var toast: Toast
    private val defaultLocation_GwanghwamunSquare = LatLng(37.575939, 126.976856)
    lateinit var lastKnownLocation: Location
    private var equptype = "001"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
//        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
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
        val job = lifecycleScope.launch {
            try {
                equptype = requireContext().dataStore.data.first()[EQUPTYPE].toString()
                Log.d(TAG, "saved equptype: $equptype ")
                homeViewModel.equpType = equptype
            } catch (e: IOException) {
                Log.e(TAG, "IOException occurred: ${e.message}")
            }
        }

        if (job.isCompleted)
            job.cancel()


        // 지도 initialize 확인
        homeViewModel.isLocationInitialized.observe(viewLifecycleOwner) { initialized ->
            if (initialized) {
                var latitude = lastKnownLocation.latitude.toString()
                var longitude = lastKnownLocation.longitude.toString()
                homeViewModel.fetchData("${latitude},${longitude}", equptype)
            }
        }


        homeViewModel.requestUpdateMap.observe(viewLifecycleOwner) {
            if (it) {
                Log.d(TAG, "Start update map")
                updateMap()
            }
        }

        homeViewModel.errorLiveData.observe(viewLifecycleOwner) {
            showErrorToast(it)
        }

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

    fun showErrorToast(message: String) {
        // 기존에 표시된 토스트 메시지가 있다면 취소
        if (::toast.isInitialized) {
            toast.cancel()
        }
        toast = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG)
        toast.show()
    }

    companion object {
        /**
         * Request code for location permission
         */
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        private const val DEFAULT_ZOOM = 18
    }
}