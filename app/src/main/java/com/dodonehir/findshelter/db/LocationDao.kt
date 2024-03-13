package com.dodonehir.findshelter.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {
    // area_code와 equp_type으로 total_count 검색
    // 없으면 null 반환하는건지??
    @Query("SELECT total_count FROM location WHERE area_code==:areaCode AND equp_type==:equpType")
    suspend fun getTotalCount(areaCode: String, equpType: String): Int

    // 이미 request한 결과가 있으면 저장된 걸 가져오기. 해당타입 쉼터 총 갯수,쉼터이름,위도,경도가 필요하다.
//    @Query("SELECT * FROM location WHERE area_code==:areaCode AND equp_type==:equpType")
//    fun getLocations(areaCode: String, equpType: String): Location

    // 값이 없을 때는 request하고 받아온 데이터 저장하기
    @Insert
    suspend fun insertLocation(vararg location: Location)
}