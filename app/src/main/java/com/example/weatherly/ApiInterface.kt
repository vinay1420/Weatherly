package com.example.weatherly

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("weather")
    fun getWeatherData(
        @Query("q") cityName: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Call<Weatherly>


    @GET("forecast")
    fun getFiveDayForecast(
        @Query("q") cityName: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Call<ForecastResponse>


    @GET("air_pollution")
    fun getAirQuality(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") appid: String
    ): Call<AirQualityResponse>
}