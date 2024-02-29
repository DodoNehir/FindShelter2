package com.dodonehir.findshelter.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DongCodeResponseItem(
    val 말소일자: Any,
    val 생성일자: Int,
    val 시군구명: String,
    val 시도명: String,
    val 읍면동명: String,
    val 행정동코드: Int
)