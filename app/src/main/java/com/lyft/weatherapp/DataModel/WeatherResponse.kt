package com.lyft.weatherapp.DataModel

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast

)

@Serializable
data class Location(
    val name: String
)

@Serializable
data class Current(
    @SerialName("last_updated") val lastUpdated: String,
    @SerialName("temp_c") val tempC: Double,
    val condition: Condition,
    val humidity: Int,
    @SerialName("feelslike_c") val feelsLikeC: Double
)

@Serializable
data class Condition(
    val text: String? = null,
    val icon: String? = null
)

@Serializable
data class Forecast(
    @SerialName("forecastday") val forecastDay: List<ForecastDay>
)

@Serializable
data class ForecastDay (
    val date: String,
    val day: Day,
    val hour: List<Hour>
)

@Serializable
data class Day(
    @SerialName("maxtemp_c") val maxTempInC: Double,
    @SerialName("mintemp_c") val minTempInC: Double,
    @SerialName("condition") val forecastCondition: Condition
)

@Serializable
data class Hour(
    @SerialName("time") val hourTime: String,
    @SerialName("temp_c") val hourlyTempInC: Double,
    @SerialName("condition") val condition: Condition
)


