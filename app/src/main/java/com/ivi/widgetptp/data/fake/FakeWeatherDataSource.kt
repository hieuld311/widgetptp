package com.ivi.widgetptp.data.fake

import com.ivi.widgetptp.domain.model.WeatherCondition
import com.ivi.widgetptp.domain.model.WeatherData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FakeWeatherDataSource @Inject constructor() {
    private val mutableCurrentWeather = MutableStateFlow<WeatherData?>(
        WeatherData(
            locationName = "Ha Noi, Vietnam",
            temperatureCelsius = 24.0,
            condition = WeatherCondition.CLEAR_SKY,
            isDaylight = true,
        ),
    )
    val currentWeather: StateFlow<WeatherData?> = mutableCurrentWeather

    private val mutableDestinationWeather = MutableStateFlow<WeatherData?>(null)
    val destinationWeather: StateFlow<WeatherData?> = mutableDestinationWeather

    fun updateCurrentWeather(value: WeatherData?) {
        mutableCurrentWeather.value = value
    }

    fun updateDestinationWeather(value: WeatherData?) {
        mutableDestinationWeather.value = value
    }
}
