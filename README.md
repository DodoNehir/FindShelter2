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

## 4. 사용 방법
![캡처](https://github.com/DodoNehir/findshelter/assets/46012435/6e9730b9-0956-4b35-b86f-1f372cefdc22)
- 먼저 위,경도를 확인 후 그 정보로부터 주소를 찾습니다. 그 주소를 이용해서 지역코드를 찾고, 지역코드를 이용해서 쉼터위치를 찾는 순으로 진행됩니다.
</br>

- GeoRequest() 와 sherlterPointRequest()는 Retrofit을 사용합니다. interface와 응답받을 데이터 형식인 GoogleAddressResponse data class를 생성합니다. 그 후 Call 객체를 생성하고 GET통신의 결과를 addressInfo에 저장해 사용합니다.
```
interface GeoService {
    @GET("maps/api/geocode/json")
    fun getResults(    ): Call<GoogleAddressResponse>
}

geoCall = geoService.getResults(     )
geoCall.enqueue(object : Callback<GoogleAddressResponse> {
                    override fun onResponse( ) {
                        val addressInfo = response.body()
                    }
})
```

</br>

- areaCodeRequest() 는 DOM 방식으로 파싱합니다. 그래서 XML 전체를 받은 후 region_cd 태그의 값을 가져와서 사용합니다.

```
val xml: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url)

val list: NodeList = xml.getElementsByTagName("region_cd")
val n: Node = list.item(0)
myAreaCode = n.textContent
```

</br>

## 5. 주요 문제점과 해결법 & 개선점
1.
- 문제점: 매번 쉼터 위치를 request함으로 api요청 횟수를 낭비하게 되고 한 번에 많은 양을 요청하면 timeout 에러가 발생함
- 원인: request 후 데이터를 저장하지 않아서 부담이 생김
- 해결법: Room을 사용해서 한 번 request한 정보는 저장하도록 했음

2. 앱을 배포하려면 우선 FastAPI가 로컬이 아니라 외부에서도 접속 가능하도록 해야 한다. 무료 서버 호스팅 찾기.
