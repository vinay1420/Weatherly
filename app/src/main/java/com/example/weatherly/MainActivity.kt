package com.example.weatherly

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.example.weatherly.databinding.ActivityMainBinding
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    // ---------------------------------------------------------
    // VIEW BINDING
    // ---------------------------------------------------------

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    // ---------------------------------------------------------
    // LOCATION CLIENT
    // ---------------------------------------------------------

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }


    // ---------------------------------------------------------
    // LOCATION PERMISSION LAUNCHER
    // ---------------------------------------------------------

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocation =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseLocation =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (fineLocation || coarseLocation) {

                getCurrentLocation()

            } else {

                Toast.makeText(
                    this,
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    // ---------------------------------------------------------
    // ON CREATE
    // ---------------------------------------------------------

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        // No default city/weather on app startup
        searchCity()

        binding.currentLocationButton.setOnClickListener {
            requestLocationPermission()
        }

        binding.textView2.setOnClickListener {
            val city =
                binding.cityName.text
                    .toString()
                    .trim()

            if (city.isNotBlank()) {
                fetchFiveDayForecast(city)
            } else {
                Toast.makeText(
                    this,
                    "Please search for a city first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Weather information cards

        binding.weatherCard1.setOnClickListener {
            showHumidityInformation()
        }

        binding.weatherCard2.setOnClickListener {
            showWindInformation()
        }

        binding.weatherCard3.setOnClickListener {
            showConditionInformation()
        }

        binding.weatherCard4.setOnClickListener {
            showSunriseInformation()
        }

        binding.weatherCard5.setOnClickListener {
            showSunsetInformation()
        }

        binding.weatherCard6.setOnClickListener {
            showPressureInformation()
        }
    }

    private fun showHumidityInformation() {

        val humidityText =
            binding.humidity.text.toString()

        val humidity =
            humidityText
                .replace("%", "")
                .trim()
                .toDoubleOrNull()

        if (humidity == null) {
            showInformationDialog(
                "Humidity",
                "Humidity information is currently unavailable."
            )
            return
        }

        val status: String
        val explanation: String
        val tip: String

        when {

            humidity < 30 -> {

                status = "Low humidity"

                explanation =
                    "The air is relatively dry. Low humidity can cause dry skin, dry eyes and irritation of the nose or throat."

                tip =
                    "Drink enough water and consider maintaining some indoor humidity."
            }

            humidity < 60 -> {

                status = "Comfortable"

                explanation =
                    "The humidity is generally comfortable for most people. The air contains enough moisture without usually feeling excessively damp."

                tip =
                    "No special precautions are normally required."
            }

            humidity < 70 -> {

                status = "Slightly humid"

                explanation =
                    "The air contains a noticeable amount of moisture. You may begin to feel slightly warm or sticky, especially in hot weather."

                tip =
                    "Stay hydrated and maintain good ventilation."
            }

            humidity < 85 -> {

                status = "High humidity"

                explanation =
                    "The air contains a relatively high amount of moisture. Sweat evaporates more slowly, which can make the weather feel warmer and less comfortable."

                tip =
                    "Stay hydrated, avoid prolonged strenuous activity in strong heat and keep good ventilation."
            }

            else -> {

                status = "Very high humidity"

                explanation =
                    "The air is highly saturated with moisture. High humidity can make hot weather feel considerably warmer and can reduce the body's ability to cool itself through sweating."

                tip =
                    "Stay hydrated and take breaks in a cool or well-ventilated place, especially during hot weather."
            }
        }

        val message =
            "Current humidity: ${humidityText}\n\n" +
                    "$explanation\n\n" +
                    "Status: $status\n\n" +
                    "Tip: $tip"

        showInformationDialog(
            "Humidity — $humidityText",
            message
        )
    }

    private fun showWindInformation() {

        val windText =
            binding.windSpeed.text.toString()

        val windSpeed =
            windText
                .replace("m/s", "")
                .trim()
                .toDoubleOrNull()

        if (windSpeed == null) {
            showInformationDialog(
                "Wind Speed",
                "Wind speed information is currently unavailable."
            )
            return
        }

        val status: String
        val explanation: String
        val tip: String

        when {

            windSpeed < 2 -> {

                status = "Calm"

                explanation =
                    "The air is moving very slowly. There is little noticeable wind and outdoor conditions are generally calm."

                tip =
                    "Normal outdoor activities are generally fine."
            }

            windSpeed < 6 -> {

                status = "Gentle breeze"

                explanation =
                    "A light to moderate breeze is present. It may provide some cooling and normally does not create significant weather-related concerns."

                tip =
                    "Normal outdoor activities are generally safe."
            }

            windSpeed < 11 -> {

                status = "Moderate wind"

                explanation =
                    "The wind is clearly noticeable and can make conditions feel cooler. Loose objects may move in stronger gusts."

                tip =
                    "Normal activities are generally possible, but be aware of stronger gusts."
            }

            windSpeed < 17 -> {

                status = "Strong wind"

                explanation =
                    "The wind is strong enough to noticeably affect outdoor conditions. Trees and loose objects may move significantly."

                tip =
                    "Use caution outdoors and secure lightweight objects."
            }

            else -> {

                status = "Very strong wind"

                explanation =
                    "Very strong winds can make outdoor activities difficult and may cause hazards from loose objects or falling branches."

                tip =
                    "Avoid unnecessary outdoor activity and follow local weather warnings."
            }
        }

        val message =
            "Current wind speed: ${windText}\n\n" +
                    "$explanation\n\n" +
                    "Status: $status\n\n" +
                    "Tip: $tip"

        showInformationDialog(
            "Wind Speed — $windText",
            message
        )
    }

    private fun showConditionInformation() {

        val condition =
            binding.condition.text
                .toString()
                .trim()

        val conditionLower =
            condition.lowercase(Locale.getDefault())

        val message: String

        when (conditionLower) {

            "clear" -> {

                message =
                    "The sky is currently clear with little or no cloud cover.\n\n" +
                            "Status: Generally safe\n\n" +
                            "Clear conditions are normally suitable for outdoor activities. " +
                            "However, sunlight can still produce significant UV exposure, so sunscreen, shade and adequate hydration may be useful."
            }

            "clouds" -> {

                message =
                    "Cloudy conditions mean a significant amount of cloud cover is present.\n\n" +
                            "Status: Generally safe\n\n" +
                            "Clouds can reduce direct sunlight, but they do not necessarily eliminate UV exposure. " +
                            "Outdoor conditions are usually comfortable unless other weather conditions are present."
            }

            "rain",
            "drizzle" -> {

                message =
                    "Rain or drizzle is currently occurring.\n\n" +
                            "Status: Use caution\n\n" +
                            "Wet roads and reduced visibility can make travel more difficult. " +
                            "Carry suitable rain protection and take care when travelling."
            }

            "thunderstorm" -> {

                message =
                    "Thunderstorm conditions indicate atmospheric instability with possible lightning, heavy rain and strong winds.\n\n" +
                            "Status: Potentially hazardous\n\n" +
                            "Avoid exposed outdoor areas during active thunderstorms and follow local weather warnings."
            }

            "snow" -> {

                message =
                    "Snow is currently being reported.\n\n" +
                            "Status: Use caution\n\n" +
                            "Snow can reduce visibility and make roads and walking surfaces slippery. " +
                            "Dress appropriately for cold conditions."
            }

            "mist",
            "fog" -> {

                message =
                    "Reduced visibility is being reported because of mist or fog.\n\n" +
                            "Status: Use caution\n\n" +
                            "Visibility can be significantly reduced, particularly for drivers. " +
                            "Travel carefully and use appropriate lighting."
            }

            "haze" -> {

                message =
                    "Hazy conditions reduce atmospheric visibility because of suspended particles or moisture.\n\n" +
                            "Status: Moderate caution\n\n" +
                            "People sensitive to air pollution may experience discomfort. " +
                            "Consider limiting prolonged outdoor activity if air quality is poor."
            }

            else -> {

                message =
                    "Current weather condition: $condition\n\n" +
                            "Weather conditions are currently being reported for this location. " +
                            "Check local weather alerts if severe conditions are present."
            }
        }

        showInformationDialog(
            "Weather Condition — $condition",
            message
        )
    }

    private fun showSunriseInformation() {

        val sunrise =
            binding.sunRise.text.toString()

        val message =
            "Today's sunrise: $sunrise\n\n" +
                    "Sunrise marks the beginning of daylight at this location.\n\n" +
                    "The period shortly after sunrise can provide increasing daylight and visibility for outdoor activities. " +
                    "Sunlight exposure can also contribute to UV exposure as the sun rises higher.\n\n" +
                    "Tip: If you are outdoors for a long period after sunrise, consider sun protection such as sunscreen, clothing and shade."

        showInformationDialog(
            "Sunrise — $sunrise",
            message
        )
    }

    private fun showSunsetInformation() {

        val sunset =
            binding.sunset.text.toString()

        val message =
            "Today's sunset: $sunset\n\n" +
                    "Sunset marks the transition from daylight toward night at this location.\n\n" +
                    "After sunset, visibility gradually decreases and temperatures may begin to fall depending on the weather conditions.\n\n" +
                    "Tip: If travelling or exercising outdoors around sunset, use appropriate lighting and remain aware of reduced visibility."

        showInformationDialog(
            "Sunset — $sunset",
            message
        )
    }

    private fun showPressureInformation() {

        val pressureText =
            binding.sea.text.toString()

        val pressure =
            pressureText
                .replace("hPa", "")
                .trim()
                .toDoubleOrNull()

        if (pressure == null) {
            showInformationDialog(
                "Atmospheric Pressure",
                "Pressure information is currently unavailable."
            )
            return
        }

        val status: String
        val explanation: String
        val tip: String

        when {

            pressure < 990 -> {

                status = "Very low pressure"

                explanation =
                    "Atmospheric pressure is considerably below the typical sea-level reference value. Low pressure systems are often associated with unsettled or changing weather."

                tip =
                    "Check the local forecast for rain, storms or strong winds."
            }

            pressure < 1005 -> {

                status = "Low pressure"

                explanation =
                    "Pressure is below the typical sea-level reference. Lower pressure can occur with cloudier, wetter or more unsettled weather."

                tip =
                    "Keep an eye on changing weather conditions."
            }

            pressure <= 1020 -> {

                status = "Near normal"

                explanation =
                    "Atmospheric pressure is within a commonly observed range around standard sea-level pressure. This value by itself does not indicate dangerous weather."

                tip =
                    "No special precautions are normally required based on pressure alone."
            }

            pressure <= 1035 -> {

                status = "High pressure"

                explanation =
                    "Atmospheric pressure is relatively high. High-pressure systems are often associated with more stable weather, although local conditions can vary."

                tip =
                    "Consider the complete weather forecast rather than pressure alone."
            }

            else -> {

                status = "Very high pressure"

                explanation =
                    "Atmospheric pressure is considerably above the typical sea-level reference value. High pressure can be associated with stable atmospheric conditions."

                tip =
                    "Pressure alone does not determine whether outdoor conditions are safe."
            }
        }

        val message =
            "Current atmospheric pressure: ${pressureText}\n\n" +
                    "$explanation\n\n" +
                    "Status: $status\n\n" +
                    "Tip: $tip"

        showInformationDialog(
            "Atmospheric Pressure — $pressureText",
            message
        )
    }

    private fun showInformationDialog(
        title: String,
        message: String
    ) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }


    // =========================================================
    // CITY SEARCH
    // =========================================================

    private fun searchCity() {

        val searchView = binding.searchView

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(
                    query: String?
                ): Boolean {

                    if (!query.isNullOrBlank()) {

                        fetchWeatherData(
                            query.trim()
                        )

                        searchView.clearFocus()
                    }

                    return true
                }


                override fun onQueryTextChange(
                    newText: String?
                ): Boolean {

                    return true
                }
            }
        )
    }


    // =========================================================
    // CURRENT WEATHER
    // =========================================================

    private fun fetchWeatherData(
        cityName: String
    ) {

        val retrofit =
            Retrofit.Builder()
                .baseUrl(
                    "https://api.openweathermap.org/data/2.5/"
                )
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()


        val api =
            retrofit.create(
                ApiInterface::class.java
            )


        val response =
            api.getWeatherData(
                cityName,
                appid = "4dc8051170ff249c31ddcf6b8cb25155",
                units = "metric"
            )


        response.enqueue(
            object : Callback<Weatherly> {

                override fun onResponse(
                    call: Call<Weatherly>,
                    response: Response<Weatherly>
                ) {

                    if (response.isSuccessful) {

                        val responseBody =
                            response.body()


                        if (responseBody != null) {

                            val temperature =
                                responseBody.main.temp

                            val humidity =
                                responseBody.main.humidity

                            val windSpeed =
                                responseBody.wind.speed

                            val sunRise =
                                responseBody.sys.sunrise

                            val sunSet =
                                responseBody.sys.sunset

                            val seaLevel =
                                responseBody.main.pressure

                            val condition =
                                responseBody.weather
                                    .firstOrNull()
                                    ?.main
                                    ?: "Unknown"

                            val maxTemp =
                                responseBody.main.temp_max

                            val minTemp =
                                responseBody.main.temp_min


                            // Temperature
                            binding.temp.text =
                                "$temperature °C"


                            // Weather condition
                            binding.weather.text =
                                condition

                            binding.condition.text =
                                condition


                            // Maximum temperature
                            binding.maxTemp.text =
                                "Max Temp: $maxTemp °C"


                            // Minimum temperature
                            binding.minTemp.text =
                                "Min Temp: $minTemp °C"


                            // Humidity
                            binding.humidity.text =
                                "$humidity %"


                            // Wind speed
                            binding.windSpeed.text =
                                "$windSpeed m/s"


                            // Sunrise
                            binding.sunRise.text =
                                time(sunRise)


                            // Sunset
                            binding.sunset.text =
                                time(sunSet)


                            // Pressure
                            binding.sea.text =
                                "$seaLevel hPa"


                            // Day
                            binding.day.text =
                                dayName(
                                    System.currentTimeMillis()
                                )


                            // Date
                            binding.date.text =
                                date()


                            // City
                            binding.cityName.text =
                                cityName

                            fetchAirQuality(
                                responseBody.coord.lat,
                                responseBody.coord.lon
                            )


                            // Background + animation
                            changeImagesAccordingToWeatherCondition(
                                condition
                            )
                        }

                    } else {

                        Toast.makeText(
                            this@MainActivity,
                            "City not found or weather data unavailable",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


                override fun onFailure(
                    call: Call<Weatherly>,
                    t: Throwable
                ) {

                    t.printStackTrace()

                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load weather data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }


    // =========================================================
    // WEATHER BACKGROUND AND ANIMATION
    // =========================================================

    private fun changeImagesAccordingToWeatherCondition(
        conditions: String
    ) {

        when (
            conditions.lowercase(
                Locale.getDefault()
            )
        ) {

            "clear" -> {

                binding.root.setBackgroundResource(
                    R.drawable.sunny_background
                )

                binding.lottieAnimationView.setAnimation(
                    R.raw.sun
                )
            }


            "clouds" -> {

                binding.root.setBackgroundResource(
                    R.drawable.colud_background
                )

                binding.lottieAnimationView.setAnimation(
                    R.raw.cloud
                )
            }


            "rain",
            "drizzle" -> {

                binding.root.setBackgroundResource(
                    R.drawable.rain_background
                )

                binding.lottieAnimationView.setAnimation(
                    R.raw.rain
                )
            }


            "snow" -> {

                binding.root.setBackgroundResource(
                    R.drawable.snow_background
                )

                binding.lottieAnimationView.setAnimation(
                    R.raw.snow
                )
            }


            "mist",
            "fog",
            "haze",
            "smoke" -> {

                binding.root.setBackgroundResource(
                    R.drawable.colud_background
                )

                binding.lottieAnimationView.setAnimation(
                    R.raw.cloud
                )
            }


            else -> {

                binding.root.setBackgroundResource(
                    R.drawable.sunny_background
                )

                binding.lottieAnimationView.setAnimation(
                    R.raw.sun
                )
            }
        }


        binding.lottieAnimationView.playAnimation()
    }


    // =========================================================
    // DATE
    // =========================================================

    private fun date(): String {

        val sdf =
            SimpleDateFormat(
                "dd MMMM yyyy",
                Locale.getDefault()
            )

        return sdf.format(Date())
    }


    // =========================================================
    // TIME
    // =========================================================

    private fun time(
        timestamp: Int
    ): String {

        val sdf =
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            )

        return sdf.format(
            Date(
                timestamp.toLong() * 1000
            )
        )
    }


    // =========================================================
    // DAY NAME
    // =========================================================

    private fun dayName(
        timestamp: Long
    ): String {

        val sdf =
            SimpleDateFormat(
                "EEEE",
                Locale.getDefault()
            )

        return sdf.format(
            Date(timestamp)
        )
    }


    // =========================================================
    // REQUEST LOCATION PERMISSION
    // =========================================================

    private fun requestLocationPermission() {

        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        val coarseLocationGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        if (
            fineLocationGranted ||
            coarseLocationGranted
        ) {

            getCurrentLocation()

        } else {

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    // =========================================================
    // GET CURRENT LOCATION
    // =========================================================

    private fun getCurrentLocation() {

        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        val coarseLocationGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        if (
            !fineLocationGranted &&
            !coarseLocationGranted
        ) {
            return
        }


        val priority =
            if (fineLocationGranted) {

                Priority.PRIORITY_HIGH_ACCURACY

            } else {

                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }


        val locationRequest =
            CurrentLocationRequest.Builder()
                .setPriority(priority)
                .setMaxUpdateAgeMillis(5000)
                .setDurationMillis(15000)
                .build()


        fusedLocationClient
            .getCurrentLocation(
                locationRequest,
                null
            )
            .addOnSuccessListener { location ->

                if (location != null) {

                    getCityName(
                        location.latitude,
                        location.longitude
                    )

                } else {

                    Toast.makeText(
                        this,
                        "Unable to get current location. Turn on device Location.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { exception ->

                exception.printStackTrace()

                Toast.makeText(
                    this,
                    "Location error: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }


    // =========================================================
    // GET CITY NAME FROM LATITUDE / LONGITUDE
    // =========================================================

    private fun getCityName(
        latitude: Double,
        longitude: Double
    ) {

        try {

            val geocoder =
                Geocoder(
                    this,
                    Locale.getDefault()
                )


            geocoder.getFromLocation(
                latitude,
                longitude,
                1
            ) { addresses ->

                if (addresses.isNotEmpty()) {

                    val address =
                        addresses[0]


                    val city =
                        address.locality
                            ?: address.subAdminArea
                            ?: address.adminArea


                    runOnUiThread {

                        if (!city.isNullOrBlank()) {

                            binding.cityName.text =
                                city


                            fetchWeatherData(
                                city
                            )


                            Toast.makeText(
                                this,
                                "Weather updated for $city",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            Toast.makeText(
                                this,
                                "Could not determine city",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                } else {

                    runOnUiThread {

                        Toast.makeText(
                            this,
                            "Could not determine city",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                this,
                "Unable to determine city",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // =========================================================
    // FIVE DAY FORECAST API
    // =========================================================

    private fun fetchFiveDayForecast(
        cityName: String
    ) {

        val retrofit =
            Retrofit.Builder()
                .baseUrl(
                    "https://api.openweathermap.org/data/2.5/"
                )
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()


        val api =
            retrofit.create(
                ApiInterface::class.java
            )


        val response =
            api.getFiveDayForecast(
                cityName,
                appid = "4dc8051170ff249c31ddcf6b8cb25155",
                units = "metric"
            )


        response.enqueue(
            object : Callback<ForecastResponse> {

                override fun onResponse(
                    call: Call<ForecastResponse>,
                    response: Response<ForecastResponse>
                ) {

                    if (response.isSuccessful) {

                        val forecast =
                            response.body()


                        if (forecast != null) {

                            showFiveDayForecast(
                                forecast
                            )
                        }

                    } else {

                        Toast.makeText(
                            this@MainActivity,
                            "Unable to load forecast",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }


                override fun onFailure(
                    call: Call<ForecastResponse>,
                    t: Throwable
                ) {

                    t.printStackTrace()

                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load forecast",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }


    // =========================================================
    // SHOW FIVE DAY FORECAST
    // =========================================================

    private fun showFiveDayForecast(
        forecast: ForecastResponse
    ) {

        // Group 3-hour forecast data by date
        val groupedForecast =
            forecast.list
                .groupBy {

                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(
                        Date(
                            it.dt * 1000
                        )
                    )
                }
                .values
                .take(5)


        // Inflate forecast layout
        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_forecast,
                null
            )


        // Create dialog
        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogView)
                .create()


        // TextView IDs
        val dayIds =
            arrayOf(
                R.id.day1,
                R.id.day2,
                R.id.day3,
                R.id.day4,
                R.id.day5
            )


        val conditionIds =
            arrayOf(
                R.id.condition1,
                R.id.condition2,
                R.id.condition3,
                R.id.condition4,
                R.id.condition5
            )


        val tempIds =
            arrayOf(
                R.id.temp1,
                R.id.temp2,
                R.id.temp3,
                R.id.temp4,
                R.id.temp5
            )


        // Fill the five days
        for (index in groupedForecast.indices) {

            if (index >= 5) {
                break
            }


            val dayForecast =
                groupedForecast[index]


            // Date
            val forecastDayName =
                SimpleDateFormat(
                    "EEE, dd MMM",
                    Locale.getDefault()
                ).format(
                    Date(
                        dayForecast.first().dt * 1000
                    )
                )


            // Minimum temperature
            val minTemperature =
                dayForecast.minOf { item ->
                    item.main.temp_min
                }


            // Maximum temperature
            val maxTemperature =
                dayForecast.maxOf { item ->
                    item.main.temp_max
                }


            // Weather condition
            val condition =
                dayForecast
                    .maxByOrNull { item ->
                        item.main.temp
                    }
                    ?.weather
                    ?.firstOrNull()
                    ?.main
                    ?: "Unknown"


            // Find TextViews
            val dayText =
                dialogView.findViewById<TextView>(
                    dayIds[index]
                )


            val conditionText =
                dialogView.findViewById<TextView>(
                    conditionIds[index]
                )


            val temperatureText =
                dialogView.findViewById<TextView>(
                    tempIds[index]
                )


            // Set values
            dayText.text =
                forecastDayName


            conditionText.text =
                condition


            temperatureText.text =
                "${minTemperature.toInt()}° / ${maxTemperature.toInt()}°C"
        }


        // Show dialog
        dialog.show()
    }


    private fun fetchAirQuality(
        latitude: Double,
        longitude: Double
    ) {

        val retrofit =
            Retrofit.Builder()
                .baseUrl(
                    "https://api.openweathermap.org/data/2.5/"
                )
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()

        val api =
            retrofit.create(
                ApiInterface::class.java
            )

        val response =
            api.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                appid = "4dc8051170ff249c31ddcf6b8cb25155"
            )

        response.enqueue(
            object : Callback<AirQualityResponse> {

                override fun onResponse(
                    call: Call<AirQualityResponse>,
                    response: Response<AirQualityResponse>
                ) {

                    if (response.isSuccessful) {

                        val airQuality =
                            response.body()

                        if (
                            airQuality != null &&
                            airQuality.list.isNotEmpty()
                        ) {

                            val components =
                                airQuality.list[0].components

                            val aqi =
                                calculateCpcbAqi(
                                    components
                                )

                            binding.aqiValue.text =
                                aqi.toString()

                            binding.aqiCategory.text =
                                getAqiCategory(aqi)

                        } else {

                            binding.aqiValue.text =
                                "--"

                            binding.aqiCategory.text =
                                "No AQI data"
                        }

                    } else {

                        binding.aqiValue.text =
                            "--"

                        binding.aqiCategory.text =
                            "AQI API error: ${response.code()}"

                        Toast.makeText(
                            this@MainActivity,
                            "AQI API error: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<AirQualityResponse>,
                    t: Throwable
                ) {

                    t.printStackTrace()

                    binding.aqiValue.text =
                        "--"

                    binding.aqiCategory.text =
                        "AQI unavailable"

                    Toast.makeText(
                        this@MainActivity,
                        "AQI error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }


    private fun calculateCpcbAqi(
        components: AirQualityComponents
    ): Int {

        val subIndices =
            mutableListOf<Int>()


        // PM2.5
        calculateSubIndex(
            components.pm2_5,
            doubleArrayOf(
                0.0,
                30.0,
                60.0,
                90.0,
                120.0,
                250.0
            ),
            intArrayOf(
                0,
                50,
                100,
                200,
                300,
                400
            )
        )?.let {
            subIndices.add(it)
        }


        // PM10
        calculateSubIndex(
            components.pm10,
            doubleArrayOf(
                0.0,
                50.0,
                100.0,
                250.0,
                350.0,
                430.0
            ),
            intArrayOf(
                0,
                50,
                100,
                200,
                300,
                400
            )
        )?.let {
            subIndices.add(it)
        }


        // NO2
        calculateSubIndex(
            components.no2,
            doubleArrayOf(
                0.0,
                40.0,
                80.0,
                180.0,
                280.0,
                400.0
            ),
            intArrayOf(
                0,
                50,
                100,
                200,
                300,
                400
            )
        )?.let {
            subIndices.add(it)
        }


        // SO2
        calculateSubIndex(
            components.so2,
            doubleArrayOf(
                0.0,
                40.0,
                80.0,
                380.0,
                800.0,
                1600.0
            ),
            intArrayOf(
                0,
                50,
                100,
                200,
                300,
                400
            )
        )?.let {
            subIndices.add(it)
        }


        // NH3
        calculateSubIndex(
            components.nh3,
            doubleArrayOf(
                0.0,
                200.0,
                400.0,
                800.0,
                1200.0,
                1800.0
            ),
            intArrayOf(
                0,
                50,
                100,
                200,
                300,
                400
            )
        )?.let {
            subIndices.add(it)
        }


        // CO
        // OpenWeather gives CO in μg/m³.
        // CPCB breakpoint uses mg/m³.
        val coMg =
            components.co / 1000.0

        calculateSubIndex(
            coMg,
            doubleArrayOf(
                0.0,
                0.5,
                1.0,
                2.0,
                3.0,
                3.5
            ),
            intArrayOf(
                0,
                50,
                100,
                200,
                300,
                400
            )
        )?.let {
            subIndices.add(it)
        }


        // O3
        calculateSubIndex(
            components.o3,
            doubleArrayOf(
                0.0,
                50.0,
                100.0,
                168.0,
                208.0,
                748.0
            ),
            intArrayOf(
                0,
                50,
                100,
                200,
                300,
                400
            )
        )?.let {
            subIndices.add(it)
        }


        if (subIndices.isEmpty()) {
            return 0
        }


        return subIndices.maxOrNull() ?: 0
    }


    private fun calculateSubIndex(
        concentration: Double,
        breakpoints: DoubleArray,
        aqiBreakpoints: IntArray
    ): Int? {

        if (concentration < 0) {
            return null
        }


        for (i in 0 until breakpoints.size - 1) {

            val concentrationLow =
                breakpoints[i]

            val concentrationHigh =
                breakpoints[i + 1]


            if (
                concentration >= concentrationLow &&
                concentration <= concentrationHigh
            ) {

                val aqiLow =
                    aqiBreakpoints[i]

                val aqiHigh =
                    aqiBreakpoints[i + 1]


                val subIndex =
                    (
                            (
                                    (aqiHigh - aqiLow) *
                                            (concentration - concentrationLow)
                                    ) /
                                    (
                                            concentrationHigh -
                                                    concentrationLow
                                            )
                            ) + aqiLow


                return subIndex.toInt()
            }
        }


        // Above the highest breakpoint
        if (
            concentration >
            breakpoints.last()
        ) {

            return 500
        }


        return null
    }


    private fun getAqiCategory(
        aqi: Int
    ): String {

        return when {

            aqi <= 50 ->
                "Good"

            aqi <= 100 ->
                "Satisfactory"

            aqi <= 200 ->
                "Moderate"

            aqi <= 300 ->
                "Poor"

            aqi <= 400 ->
                "Very Poor"

            else ->
                "Severe"
        }
    }
}