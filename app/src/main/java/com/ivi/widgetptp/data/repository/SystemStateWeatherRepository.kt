package com.ivi.widgetptp.data.repository

import android.util.Log
import com.ivi.widgetptp.data.fauto.FAutoCarConnectionManager
import com.ivi.widgetptp.domain.model.WeatherData
import com.ivi.widgetptp.domain.policy.DaylightPolicy
import com.ivi.widgetptp.domain.policy.WeatherCodePolicy
import com.ivi.widgetptp.domain.repository.WeatherRepository
import fauto.car.FAutoCar
import fauto.car.systemstate.FAutoCarSystemStateManager
import fauto.car.systemstate.api.PropertyID
import fauto.car.systemstate.api.PropertyListener
import fauto.car.systemstate.api.PropertyValue
import java.time.Duration
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class SystemStateWeatherRepository @Inject constructor(
    connectionManager: FAutoCarConnectionManager,
    private val weatherCodePolicy: WeatherCodePolicy,
    private val daylightPolicy: DaylightPolicy,
) : WeatherRepository {

    private val weatherProperties: Flow<WeatherProperties?> = connectionManager.connectedCar
        .flatMapLatest { fAutoCar ->
            fAutoCar?.observeWeatherProperties() ?: flowOf(null)
        }
        .distinctUntilChanged()

    override val currentWeather: Flow<WeatherData?> = combine(
        weatherProperties,
        observeDaylight(),
    ) { properties, isDaylight ->
        properties?.toWeatherData(isDaylight)
    }.distinctUntilChanged()

    override val destinationWeather: Flow<WeatherData?> = flowOf(null)

    private fun FAutoCar.observeWeatherProperties(): Flow<WeatherProperties?> = callbackFlow {
        val manager = runCatching {
            getIVICarManager(FAutoCarSystemStateManager.SERVICE_NAME)
                as? FAutoCarSystemStateManager
                ?: error("System State Store manager is unavailable")
        }.getOrElse { error ->
            Log.e(TAG, "Unable to get System State Store manager", error)
            trySend(null)
            close()
            return@callbackFlow
        }

        val stateLock = Any()
        var weatherCode: Int? = null
        var temperatureCelsius: Float? = null
        var locationName: String? = null

        fun publishLatest() {
            val properties = synchronized(stateLock) {
                val code = weatherCode
                val temperature = temperatureCelsius
                val location = locationName?.takeIf(String::isNotBlank)
                if (code != null && temperature != null && location != null) {
                    WeatherProperties(
                        code = code,
                        temperatureCelsius = temperature,
                        locationName = location,
                    )
                } else {
                    null
                }
            }
            trySend(properties)
        }

        val weatherCodeListener = object : PropertyListener<Int> {
            override fun onPropertyChanged(value: PropertyValue<Int>) {
                synchronized(stateLock) {
                    weatherCode = value.value
                }
                publishLatest()
            }
        }

        val temperatureListener = object : PropertyListener<Float> {
            override fun onPropertyChanged(value: PropertyValue<Float>) {
                synchronized(stateLock) {
                    temperatureCelsius = value.value
                }
                publishLatest()
            }
        }

        val locationListener = object : PropertyListener<String> {
            override fun onPropertyChanged(value: PropertyValue<String>) {
                synchronized(stateLock) {
                    locationName = value.value
                }
                publishLatest()
            }
        }

        trySend(null)
        runCatching {
            manager.registerListener(PropertyID.WEATHER_CODE, weatherCodeListener)
            manager.registerListener(PropertyID.WEATHER_TEMPERATURE, temperatureListener)
            manager.registerListener(PropertyID.WEATHER_LOCATION, locationListener)

            synchronized(stateLock) {
                weatherCode = manager.getProperty(PropertyID.WEATHER_CODE)
                temperatureCelsius = manager.getProperty(PropertyID.WEATHER_TEMPERATURE)
                locationName = manager.getProperty(PropertyID.WEATHER_LOCATION)
            }
            publishLatest()
        }.onFailure { error ->
            Log.e(TAG, "Unable to observe weather properties", error)
            trySend(null)
        }

        awaitClose {
            runCatching {
                manager.unregisterListener(PropertyID.WEATHER_CODE, weatherCodeListener)
            }
            runCatching {
                manager.unregisterListener(PropertyID.WEATHER_TEMPERATURE, temperatureListener)
            }
            runCatching {
                manager.unregisterListener(PropertyID.WEATHER_LOCATION, locationListener)
            }
        }
    }

    private fun observeDaylight(): Flow<Boolean> = flow {
        while (true) {
            val now = ZonedDateTime.now()
            emit(daylightPolicy.isDaylight(now.hour))
            delay(now.millisUntilNextDaylightBoundary())
        }
    }.distinctUntilChanged()

    private fun ZonedDateTime.millisUntilNextDaylightBoundary(): Long {
        val todayAtSix = withHour(6).withMinute(0).withSecond(0).withNano(0)
        val todayAtEighteen = withHour(18).withMinute(0).withSecond(0).withNano(0)
        val nextBoundary = when {
            isBefore(todayAtSix) -> todayAtSix
            isBefore(todayAtEighteen) -> todayAtEighteen
            else -> todayAtSix.plusDays(1)
        }
        return Duration.between(this, nextBoundary).toMillis().coerceAtLeast(1L)
    }

    private fun WeatherProperties.toWeatherData(isDaylight: Boolean): WeatherData? {
        val condition = weatherCodePolicy.map(code) ?: return null
        return WeatherData(
            locationName = locationName,
            temperatureCelsius = temperatureCelsius.toDouble(),
            condition = condition,
            isDaylight = isDaylight,
        )
    }

    private data class WeatherProperties(
        val code: Int,
        val temperatureCelsius: Float,
        val locationName: String,
    )

    private companion object {
        const val TAG = "SystemStateWeather"
    }
}
