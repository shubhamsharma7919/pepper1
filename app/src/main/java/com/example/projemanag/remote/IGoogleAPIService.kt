package com.example.projemanag.remote

import com.example.projemanag.model.PlaceDetail
import com.example.projemanag.model.myplaces
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun getNearbyPlaces(@Url url:String):Call<myplaces>

    @GET
    fun getDetailPlace(@Url url:String):Call<PlaceDetail>

    @GET("maps/api/directions/json")
    fun getDirections(@Query("origin") origin:String, @Query("destination") destination:String):Call<String>

}