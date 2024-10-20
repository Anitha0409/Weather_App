package com.lyft.weatherapp

import com.lyft.weatherapp.DataModel.Current
import com.lyft.weatherapp.DataModel.ForecastDay
import com.lyft.weatherapp.DataModel.Hour
import com.lyft.weatherapp.DataModel.Location

sealed class WeatherState {
     data object Loading: WeatherState()

     data class Success(
         val location: Location?,
         val currentWeather: Current?,
         val hourlyForecast: List<Hour>?,
         val dailyForecast: List<ForecastDay>?
     ): WeatherState()

    data class Error(val message:String):WeatherState()
}