package com.dodonehir.findshelter.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "location", primaryKeys = ["area_code", "equp_type"])
data class Location(
    @ColumnInfo(name = "area_code") val areaCode: String,
    @ColumnInfo(name = "equp_type") val equpType: String,
    @ColumnInfo(name = "total_count") val totalCount: Int
//    @ColumnInfo(name = "rest_name") val restName: String,
//    val la: Double,
//    val lo: Double
)
