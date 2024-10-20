package com.lyft.weatherapp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalTime


class WeatherViewModel: ViewModel() {

    // Maintaining one state that is of sealed class type

    private val _weatherState = MutableLiveData<WeatherState>()
    val weatherState:LiveData<WeatherState> get() = _weatherState

    private var _locationName: String = ""


    fun getWeatherDetails(location:String){
        _locationName = location
        fetchWeatherDetails()
    }

    private fun fetchWeatherDetails(){

        viewModelScope.launch{
            _weatherState.value = WeatherState.Loading

            try{
                // Making the API call
                val result = weatherApi.getWeatherDetails(location = _locationName)

                val todayForeCast = result.forecast.forecastDay.firstOrNull()
                val tomorrowForeCast = result.forecast.forecastDay.getOrNull(1)
                val currentTime = LocalTime.now().hour

                val todayHourlyForeCast = todayForeCast?.hour?.filter {
                    val hourInt = it.hourTime.split(" ")[1].split(":")[0].toIntOrNull() ?:0
                    hourInt >= currentTime
                } ?: emptyList()

                val tomorrowHourlyForecast = tomorrowForeCast?.hour?.filter {
                    val hourInt = it.hourTime.split(" ")[1].split(":")[0].toIntOrNull() ?:0
                    hourInt <9
                } ?: emptyList()



                val combinedForeCast = todayHourlyForeCast + tomorrowHourlyForecast

                _weatherState.value = WeatherState.Success(
                    location = result.location,
                    currentWeather = result.current,
                    hourlyForecast = combinedForeCast,
                    dailyForecast = result.forecast.forecastDay
                )
            }catch (e:Exception){
                _weatherState.value = WeatherState.Error("Failed to fetch weather details ${e.message}")

            }
        }

    }

}
