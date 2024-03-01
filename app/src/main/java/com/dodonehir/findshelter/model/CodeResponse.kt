package com.dodonehir.findshelter.model

import com.dodonehir.findshelter.util.NullToEmptyString
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CodeResponse(
    @Json(name = "행정동코드")
    val code: Long,
    @Json(name = "시도명")
    val city: String,
    @Json(name = "시군구명")
    val district: String,
    @Json(name = "읍면동명")
    val dong: String,
    @Json(name = "생성일자")
    val createTime: Long,
    @Json(name = "말소일자")
    @NullToEmptyString
    val expireTime: String?,
)