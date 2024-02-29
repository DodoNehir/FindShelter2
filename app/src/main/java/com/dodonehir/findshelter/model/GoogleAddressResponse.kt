package com.dodonehir.findshelter.model

import com.squareup.moshi.JsonClass

//@JsonClass(generateAdapter = true)
data class GoogleAddressResponse(
    val plus_code: PlusCode,
    val results: List<Result>,
    val status: String
)

////@JsonClass(generateAdapter = true)
data class PlusCode(
    val compound_code: String,
    val global_code: String
)

////@JsonClass(generateAdapter = true)
data class Result(
    val address_components: List<AddressComponent>,
    val formatted_address: String,
    val geometry: Geometry,
    val place_id: String,
    val plus_code: PlusCode,
    val types: List<String>
)
//@JsonClass(generateAdapter = true)
data class AddressComponent(
    val long_name: String,
    val short_name: String,
    val types: List<String>
)
//@JsonClass(generateAdapter = true)
data class Geometry(
    val location: Location,
    val location_type: String,
    val viewport: Viewport
)
//@JsonClass(generateAdapter = true)
data class Location(
    val lat: Double,
    val lng: Double
)
//@JsonClass(generateAdapter = true)
data class Viewport(
    val northeast: Northeast,
    val southwest: Southwest
)
//@JsonClass(generateAdapter = true)
data class Northeast(
    val lat: Double,
    val lng: Double
)
//@JsonClass(generateAdapter = true)
data class Southwest(
    val lat: Double,
    val lng: Double
)