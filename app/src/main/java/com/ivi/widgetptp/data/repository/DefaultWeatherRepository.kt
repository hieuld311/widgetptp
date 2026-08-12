package com.ivi.widgetptp.data.repository

import com.ivi.widgetptp.data.fake.FakeWeatherDataSource
import com.ivi.widgetptp.domain.model.WeatherData
import com.ivi.widgetptp.domain.repository.WeatherRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultWeatherRepository @Inject constructor(
    private val dataSource: FakeWeatherDataSource,
) : WeatherRepository {
    override val currentWeather: Flow<WeatherData?> = dataSource.currentWeather
    override val destinationWeather: Flow<WeatherData?> = dataSource.destinationWeather
}
