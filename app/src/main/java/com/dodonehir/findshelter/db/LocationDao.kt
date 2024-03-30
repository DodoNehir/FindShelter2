package com.dodonehir.findshelter.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Query("SELECT * FROM location WHERE area_code==:areaCode AND equp_type==:equpType")
    fun getLocation(areaCode: String, equpType: String): Location?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(location: Location)
}

@Dao
interface LocationDataDao {
    @Query("SELECT * FROM location_data WHERE location_id=:locationId")
    fun getAll(locationId: Int): List<LocationData>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertLocationData(locationData: LocationData)
}