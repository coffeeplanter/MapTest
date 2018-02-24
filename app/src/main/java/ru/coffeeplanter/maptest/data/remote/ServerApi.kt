package ru.coffeeplanter.maptest.data.remote

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import ru.coffeeplanter.maptest.data.model.Marker
import ru.coffeeplanter.maptest.data.model.ServerPointsAnswer

interface ServerApi {

    @get:GET("/api/test/categories/")
    val markers: Call<List<Marker>>

    @GET("/api/test/places/")
    fun getPoints(@Query("startFrom") startFrom: Int, @Query("pageSize") pageSize: Int): Call<ServerPointsAnswer>

}
