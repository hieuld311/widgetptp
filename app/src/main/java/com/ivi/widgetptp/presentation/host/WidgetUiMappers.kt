package com.ivi.widgetptp.presentation.host

import com.ivi.widgetptp.domain.model.DateTimeSnapshot
import com.ivi.widgetptp.domain.model.DriverHealthData
import com.ivi.widgetptp.domain.model.NavigationData
import com.ivi.widgetptp.domain.model.TemperatureUnit
import com.ivi.widgetptp.domain.model.WeatherCondition
import com.ivi.widgetptp.domain.model.WeatherData
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import javax.inject.Inject
import kotlin.math.roundToInt

class DateTimeUiMapper @Inject constructor() {
    fun map(snapshot: DateTimeSnapshot): WidgetSlotUiState.DateTime {
        val dateTime = snapshot.instant.atZone(snapshot.zoneId)
        val timePattern = if (snapshot.is24HourFormat) "HH:mm" else "h:mm"
        val dayPeriod = if (snapshot.is24HourFormat) {
            ""
        } else {
            DateTimeFormatter.ofPattern("a", snapshot.locale).format(dateTime)
        }

        return WidgetSlotUiState.DateTime(
            timeText = DateTimeFormatter.ofPattern(timePattern, snapshot.locale).format(dateTime),
            dayPeriodText = dayPeriod,
            dateText = DateTimeFormatter
                .ofPattern("EEE, MMM.dd. yyyy", snapshot.locale)
                .format(dateTime),
            timeZoneText = snapshot.zoneId.getDisplayName(TextStyle.SHORT, snapshot.locale),
            hourRotationDegrees = (dateTime.hour.mod(12) * 30f) + (dateTime.minute * 0.5f),
            minuteRotationDegrees = (dateTime.minute * 6f) + (dateTime.second * 0.1f),
            secondRotationDegrees = dateTime.second * 6f,
        )
    }
}

class DriverHealthUiMapper @Inject constructor() {
    fun map(data: DriverHealthData?): WidgetSlotUiState.DriverHealth {
        val heartRate = data?.heartRateBpm ?: 0
        val respirationRate = data?.respirationRateBrpm ?: 0
        return WidgetSlotUiState.DriverHealth(
            heartRateText = heartRate.toString(),
            respirationRateText = respirationRate.toString(),
            isHeartRateAvailable = heartRate != 0,
            isRespirationRateAvailable = respirationRate != 0,
        )
    }
}

class NavigationUiMapper @Inject constructor() {
    fun map(data: NavigationData): WidgetSlotUiState.NavigationRoute? {
        val destination = data.destination ?: return null
        val distance = data.remainingDistanceKilometers ?: return null
        val duration = data.remainingDurationMinutes ?: return null
        val eta = data.eta ?: return null
        val progress = data.routeProgress ?: return null

        return WidgetSlotUiState.NavigationRoute(
            destinationText = destination,
            distanceText = "${formatDistance(distance)}km",
            durationText = "$duration mins",
            etaText = eta,
            progressFraction = progress.coerceIn(0f, 1f),
        )
    }

    private fun formatDistance(distanceKilometers: Double): String =
        if (distanceKilometers.mod(1.0) == 0.0) {
            distanceKilometers.toInt().toString()
        } else {
            String.format(java.util.Locale.US, "%.1f", distanceKilometers)
        }
}

class WeatherUiMapper @Inject constructor() {
    fun map(
        weather: WeatherData?,
        unit: TemperatureUnit,
    ): WidgetSlotUiState.Weather? {
        if (weather == null) {
            return WidgetSlotUiState.Weather(
                locationText = "",
                temperatureText = "0",
                temperatureUnitText = unit.symbol,
                icon = null,
            )
        }

        val icon = mapIcon(weather.condition, weather.isDaylight) ?: return null
        val temperature = when (unit) {
            TemperatureUnit.CELSIUS -> weather.temperatureCelsius
            TemperatureUnit.FAHRENHEIT -> weather.temperatureCelsius * 9.0 / 5.0 + 32.0
        }

        return WidgetSlotUiState.Weather(
            locationText = weather.locationName,
            temperatureText = temperature.roundToInt().toString(),
            temperatureUnitText = unit.symbol,
            icon = icon,
        )
    }

    private val TemperatureUnit.symbol: String
        get() = when (this) {
            TemperatureUnit.CELSIUS -> "°C"
            TemperatureUnit.FAHRENHEIT -> "°F"
        }

    private fun mapIcon(
        condition: WeatherCondition,
        isDaylight: Boolean,
    ): WeatherIconAsset? = when {
        isDaylight -> when (condition) {
            WeatherCondition.CLEAR_SKY -> WeatherIconAsset.DAY_CLEAR_SKY
            WeatherCondition.FEW_CLOUDS -> WeatherIconAsset.DAY_FEW_CLOUDS
            WeatherCondition.SCATTERED_CLOUDS -> WeatherIconAsset.DAY_SCATTERED_CLOUDS
            WeatherCondition.BROKEN_CLOUDS -> WeatherIconAsset.DAY_BROKEN_CLOUDS
            WeatherCondition.SHOWER_RAIN -> WeatherIconAsset.DAY_SHOWER_RAIN
            WeatherCondition.RAIN -> WeatherIconAsset.DAY_RAIN
            WeatherCondition.LIGHT_RAIN -> WeatherIconAsset.DAY_LIGHT_RAIN
            WeatherCondition.THUNDERSTORM -> WeatherIconAsset.DAY_THUNDERSTORM
            WeatherCondition.SNOW -> WeatherIconAsset.DAY_SNOW
            WeatherCondition.MIST -> WeatherIconAsset.DAY_MIST
            WeatherCondition.DRIZZLE -> WeatherIconAsset.DAY_DRIZZLE
        }

        condition == WeatherCondition.SNOW || condition == WeatherCondition.SCATTERED_CLOUDS -> null
        else -> when (condition) {
            WeatherCondition.CLEAR_SKY -> WeatherIconAsset.NIGHT_CLEAR_SKY
            WeatherCondition.FEW_CLOUDS -> WeatherIconAsset.NIGHT_FEW_CLOUDS
            WeatherCondition.BROKEN_CLOUDS -> WeatherIconAsset.NIGHT_BROKEN_CLOUDS
            WeatherCondition.SHOWER_RAIN -> WeatherIconAsset.NIGHT_SHOWER_RAIN
            WeatherCondition.RAIN -> WeatherIconAsset.NIGHT_RAIN
            WeatherCondition.LIGHT_RAIN -> WeatherIconAsset.NIGHT_LIGHT_RAIN
            WeatherCondition.THUNDERSTORM -> WeatherIconAsset.NIGHT_THUNDERSTORM
            WeatherCondition.MIST -> WeatherIconAsset.NIGHT_MIST
            WeatherCondition.DRIZZLE -> WeatherIconAsset.NIGHT_DRIZZLE
            WeatherCondition.SNOW,
            WeatherCondition.SCATTERED_CLOUDS,
            -> null
        }
    }
}
