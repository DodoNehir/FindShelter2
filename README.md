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
  그리고 이 값을 관찰하고 있는 관찰자는 true가 된 것을 보고 getKoreanAddress()라는 다음 스탭에 필요한 메서드를 실행시킵니다.
  이런 방식으로 각 api가 값을 받아오는 것을 확인 후 다음 메서드를 진행시킵니다.
  아래 코드는 맨 처음 isLocationInitialized 가 true 될 때 observe 하고 있는 코드입니다.
</br>
```
        homeViewModel.isLocationInitialized.observe(viewLifecycleOwner) { initialized ->
            if (initialized) {
                getKoreanAddress()
            }
        }
```

</br>

- getKoreanAddress() 는 HTTP 통신으로 주소를 받아오는 메서드입니다.
  통신에는 Retrofit을 사용하고 Deserialize에는 Moshi 컨버터를 사용하였습니다.
  Retrofit 객체 생성에는 비용이 비싸다고 해서 싱글톤으로 사용하도록 했습니다.
  아래는 그 구현 내용입니다. 결과로 받아오는 GoogleAddressResponse는 json 형식대로 작성된 Data Class 입니다.

```
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface GeoService {
    @GET("maps/api/geocode/json")
    fun getResults(
        @Query("latlng") latlng: String,
        @Query("key") API_KEY: String,
        @Query("language") language: String, // ko
        @Query("result_type") resultType: String, // street_address
    ): Call<GoogleAddressResponse>
}

object GMSApi {

    val geoService: GeoService by lazy {
        retrofit.create(GeoService::class.java)
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
    viewLifecycleOwner.lifecycleScope.launch {
        equptype = requireContext().dataStore.data.first()[EQUPTYPE].toString()
    }
```
  
</br>

## 5. 주요 문제점과 해결법 & 개선점
1.
- 문제점: 매번 쉼터 위치를 request함으로 api요청 횟수를 낭비하게 되고 한 번에 많은 양을 요청하면 timeout 에러가 발생함
- 원인: request 후 데이터를 저장하지 않아서 부담이 생김
- 해결법: Room을 사용해서 한 번 request한 정보는 저장하도록 했음

2. 앱을 배포하려면 우선 FastAPI가 로컬이 아니라 외부에서도 접속 가능하도록 해야 한다. 무료 서버 호스팅 찾기.
