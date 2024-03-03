package com.dodonehir.findshelter.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShelterResponse(
    val HeatWaveShelter: List<HeatWaveShelter>
)

@JsonClass(generateAdapter = true)
data class HeatWaveShelter(
    val head: List<Head>?,
    val row: List<Row>?
)

@JsonClass(generateAdapter = true)
data class Head(
    @Json(name = "totalCount")
    val totalCount: Int?,
    @Json(name = "numOfRows")
    val numOfRows: String?,
    @Json(name = "pageNo")
    val pageNo: String?,
    @Json(name = "type")
    val type: String?,
    @Json(name = "RESULT")
    val shelterResult: ShelterResult?
)

@JsonClass(generateAdapter = true)
data class ShelterResult(
    val resultCode: String,
    val resultMsg: String
)

@JsonClass(generateAdapter = true)
data class Row(
    val ar: Int,
    val areaCd: String,
    val areaNm: String,
    val chckMatterNightOpnAt: String,
    val chckMatterStayngPsblAt: String,
    val chckMatterWkendHdayOpnAt: String,
    val colrHoldArcndtn: Int,
    val colrHoldElefn: Int,
    val creDttm: String,
    val dtlAdres: String,
    val equptype: String,
    val fclty_ty_nm: String,
    val la: Double,
    val lo: Double,
    val mngdptCd: String,
    val mngdpt_cd: String,
    val mngdpt_nm: String,
    val operBeginDe: String,
    val operEndDe: String,
    val restSeqNo: String,
    val restaddr: String,
    val restname: String,
    val rm: String,
    val updtDttm: String,
    val usePsblNmpr: Int,
    val useYn: String,
    val xcord: Double,
    val ycord: Double,
    val year: String
)