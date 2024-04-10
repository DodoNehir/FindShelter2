# :pushpin: findshelter
> 무더위 쉼터 위치 찾기
> </br>

## 1. 제작 기간 & 참여 인원
- 2024년 2월 20일 ~ 진행중
- 개인 프로젝트

</br>

## 2. 사용 언어, 라이브러리, API
  - Kotlin
  - Retrofit, Moshi(serialize), Room
  - Google Maps API, 공공데이터 포털 API, FastAPI(local server)

</br>

## 3. 핵심 기능
이 서비스는 무더위 쉼터로 지정된 곳을 지도 위에 표시합니다.
</br>
앱을 실행하면 내 위치에 대한 위도와 경도, 주소를 알게 됩니다. 그리고 사용자가 쉼터 종류 중 하나를 선택하면 그에 맞는 쉼터의 위치를 찾아서 지도에 표시합니다.

</br>

## 4. 구현 방법
- 지도 화면은 HomeFragment에서 표시하고 쉼터 종류는 SettingsFragment에서 선택합니다.
</br>

- 앱을 실행하면 처음에는 위치 정보(위도, 경도)를 가져오려 시도합니다.
  위치 확인이 되면 HomeViewModel 클래스에 있는 isLocationInitialized 값을 true로 바꿉니다.
  그리고 이 값을 관찰하고 있는 관찰자는 true가 된 것을 보고 fetchData() 를 실행합니다.
  
</br>

```
        homeViewModel.isLocationInitialized.observe(viewLifecycleOwner) { initialized ->
            if (initialized) {
                var latitude = lastKnownLocation.latitude.toString()
                var longitude = lastKnownLocation.longitude.toString()
                homeViewModel.fetchData("${latitude},${longitude}", equptype)
            }
        }
```

</br>

- fetchData() 는 쉼터 위치를 받아오는 메서드입니다.
  처음에는 Google Maps API로 현재 위치의 주소를 받아오고
  그 다음에는 Fast API로 동코드를 받아옵니다.
  그 후에 DB에 위치정보가 있는 지 검색합니다. DB에 있으면 로컬 데이터를 받아오고, 없으면 Retrofit을 사용하여 공공 API 에 위치를 요청합니다.
  모든 요청이 끝난 뒤에는 맵을 업데이트합니다.
  
```
    fun fetchData() {
        
        viewModelScope.launch {
            repository.getAddressWithResult() ...

            repository.getCode() ...

            // DB get
            val location = repository.getLocation(areaCode.toString(), equpType)
            if (location == null) {
                callShelterRequest()
            } else {
                getLocationFromDB(location.id)
            }

            requestUpdateMap()
        }

    }
```
</br>

- SettingsFragment에서는 쉼터 종류를 하나 선택할 수 있습니다.
  여기서 선택된 값은 간단하기 때문에 Preferences Datastore를 사용하여 저장했습니다.
```
/* SettingsFragment 최상단에 선언 */
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/* SettingsViewModel 에서 값을 쓸 수 있도록 제공한 메서드 */
    val EQUPTYPE = stringPreferencesKey("equptype")
    fun updateEquptype(context: Context, selectedEquptype: String) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[EQUPTYPE] = selectedEquptype
            }
        }
    }

/* 값 읽기 */
    val EQUPTYPE = stringPreferencesKey("equptype")
    lifecycleScope.launch {
        equptype = requireContext().dataStore.data.first()[EQUPTYPE].toString()
    }
```
  
</br>

## 5. 주요 문제점과 해결법 & 개선점
1.
- 문제점: 매번 쉼터 위치를 request함으로 api요청 횟수를 낭비하게 되고 한 번에 많은 양을 요청하면 timeout 에러가 발생함
- 원인: request 후 데이터를 저장하지 않음
- 해결법: Room을 사용해서 한 번 request한 정보는 저장하도록 변경함

2. 앱을 배포하려면 우선 FastAPI가 로컬이 아니라 외부에서도 접속 가능하도록 해야 한다. 무료 서버 호스팅 찾기.

3.
- 문제점: 로컬 DB나 네트워크 통신을 할 때는 메인 스레드가 아니라 백그라운드에서 실행되어야 하는데, 코루틴이 2개가 만들어지고 있음
- 원인: 두 개의 Fragment 에서 코루틴이 사용되고 있기 때문에 한 곳에서의 코루틴이 사라지지 않아서 발생한다고 예상됨
