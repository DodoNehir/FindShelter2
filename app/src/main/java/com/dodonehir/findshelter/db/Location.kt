package com.dodonehir.findshelter.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location",
    indices = [Index(value = ["area_code", "equp_type"], unique = true)]
)
data class Location(
    @ColumnInfo(name = "area_code") var areaCode: String,
    @ColumnInfo(name = "equp_type") var equpType: String,
    @ColumnInfo(name = "total_count") var totalCount: Int
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(
    tableName = "location_data",
    foreignKeys = [ForeignKey(
        entity = Location::class,
        parentColumns = ["id"],
        childColumns = ["location_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["restname", "la", "lo"], unique = true)]
)
data class LocationData(
    @ColumnInfo(name = "restname") var restname: String,
    @ColumnInfo(name = "la") var la: Double,
    @ColumnInfo(name = "lo") var lo: Double,
    @ColumnInfo(name = "location_id")
    var locationId: Int

) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}