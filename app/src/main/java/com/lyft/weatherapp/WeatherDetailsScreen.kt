package com.lyft.weatherapp

import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.lyft.weatherapp.DataModel.Forecast
import com.lyft.weatherapp.DataModel.ForecastDay
import com.lyft.weatherapp.DataModel.Hour
import java.time.LocalTime

@Composable
fun WeatherApp(viewModel: WeatherViewModel = androidx.lifecycle.viewmodel.compose.viewModel()){

    val weatherState by viewModel.weatherState.observeAsState()
    var searchQuery by remember { mutableStateOf("") }
    val keyBoardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.padding(16.dp)) {

        TextField(value = searchQuery,
                  modifier = Modifier.fillMaxWidth(),
                  onValueChange = {searchQuery = it},
                  label = { Text("Enter your location") },
                  trailingIcon = {
                      IconButton(onClick = { viewModel.getWeatherDetails(searchQuery)}) {
                             Icon(Icons.Default.Search, contentDescription = "Search")
                      }
                  },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if(searchQuery.isNotEmpty()){
                        viewModel.getWeatherDetails(searchQuery)
                    }
                    keyBoardController?.hide()
                }
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        when(weatherState){
            is WeatherState.Loading ->{
                LoadingScreen()

            }
            is WeatherState.Success ->{

                val state = weatherState as WeatherState.Success
                WeatherScreen(state)

            }
            is WeatherState.Error ->{
                val errorState = weatherState as WeatherState.Error
                ErrorScreen(message = errorState.message)
            }

            else -> {
               ErrorScreen(message = "Some unknown error occurred")
            }
        }

    }

}

@Composable
fun LoadingScreen(){
    Box (modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
//        CircularProgressIndicator()
        Text("Loading")
    }
}

@Composable
fun ErrorScreen(message:String){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun WeatherScreen(state: WeatherState.Success){
    val currentHour = LocalTime.now().hour
    val isDay = currentHour in 6..18
    val weatherCondition = state.currentWeather?.condition?.text ?: ""

    weatherBackground(weatherCondition = weatherCondition, isDay = isDay) {

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp))
        {
            weatherContent(state)
            Spacer(modifier = Modifier.height(16.dp))
                // Hourly forecast of the day
            hourlyWeatherList(hourlyForeCast = state.hourlyForecast ?: emptyList())
            Spacer(modifier = Modifier.height(16.dp))
            // DailyForecast and hourly forecast
                dailyForeCastList(dailyForecast = state.dailyForecast ?: emptyList())

        }

    }
}

@Composable
fun weatherBackground(weatherCondition: String, isDay: Boolean, content: @Composable ()->Unit){
     val backgroundRes = when{
         isDay && weatherCondition.contains("Partly Cloudy") -> R.drawable.partlycloudy
         isDay && weatherCondition.contains("Sunny") -> R.drawable.sunnyday
         else -> R.drawable.night
     }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(painter = painterResource(id = backgroundRes),
            contentDescription = "background" ,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier.fillMaxSize())
        {
            content()  // Display the content (weatherContent in our case)
        }

    }
}

@Composable
fun weatherContent(state: WeatherState.Success){
    // Location and current temperature

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(state.location!!.name, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White) )
        Text("Last Updated: ${state.currentWeather!!.lastUpdated}", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White))
        Text("Current temperature: ${state.currentWeather?.tempC.toString()}°C", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
    }
}

@Composable
fun hourlyWeatherList(hourlyForeCast: List<Hour>){

    LazyRow {
        items(hourlyForeCast){hour->
          Card( modifier = Modifier
              .padding(8.dp)
              .width(100.dp),
              shape = RoundedCornerShape(8.dp)
          ) {
               Column(
                   modifier = Modifier.padding(8.dp),
                   horizontalAlignment = Alignment.CenterHorizontally
               ) {

                   Text("${hour.hourTime}:00", style = MaterialTheme.typography.bodySmall)
                   Text("${hour.hourlyTempInC}°C", style = MaterialTheme.typography.bodySmall)
                   
                   Image(painter = rememberAsyncImagePainter(model = hour.condition.icon),
                       contentDescription = "Weather icon",
                       modifier = Modifier.size(24.dp))
               }
            }

        }
    }
}

@Composable
fun dailyForeCastList(dailyForecast: List<ForecastDay>){
    LazyColumn {
        items(dailyForecast){day->
          Card(modifier = Modifier.padding(8.dp),
//              elevation = 4.dp,
              shape = RoundedCornerShape(8.dp)
          ) {
              Column (modifier = Modifier
                  .padding(16.dp)
                  .fillMaxWidth()){

                  Text(day.date, style = MaterialTheme.typography.bodySmall)
                  Text("Max: ${day.day.maxTempInC}°C", style = MaterialTheme.typography.bodySmall)
                  Text("Min: ${day.day.minTempInC}°C", style = MaterialTheme.typography.bodySmall)

                  Image(painter = rememberAsyncImagePainter(model = day.day.forecastCondition.icon) ,
                      contentDescription = "Weather Icon",
                      modifier = Modifier.size(24.dp))
              }
          }
        }
    }
}
