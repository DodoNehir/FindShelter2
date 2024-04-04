package com.dodonehir.findshelter.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface LocationDao {
    @Query("SELECT * FROM location WHERE area_code==:areaCode AND equp_type==:equpType")
    fun getLocation(areaCode: String, equpType: String): Location?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(location: Location)
}

@Dao
interface LocationDataDao {
    @Query("SELECT * FROM location_data WHERE location_id=:locationId")
    fun getAll(locationId: Int): List<LocationData>

    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM location_data WHERE location_id=:locationId AND restname=:restName) THEN 1 ELSE 0 END")
    fun isExistData(locationId: Int, restName: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLocationData(locationData: LocationData)
}