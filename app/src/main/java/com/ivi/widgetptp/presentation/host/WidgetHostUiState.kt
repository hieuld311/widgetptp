package com.ivi.widgetptp.presentation.host

import androidx.compose.runtime.Immutable

@Immutable
data class WidgetHostUiState(
    val first: WidgetSlotUiState = WidgetSlotUiState.Empty,
    val second: WidgetSlotUiState = WidgetSlotUiState.Empty,
    val third: WidgetSlotUiState = WidgetSlotUiState.Empty,
)

@Immutable
sealed interface WidgetSlotUiState {
    data object Empty : WidgetSlotUiState

    data class DateTime(
        val timeText: String,
        val dayPeriodText: String,
        val dateText: String,
        val timeZoneText: String,
        val hourRotationDegrees: Float,
        val minuteRotationDegrees: Float,
        val secondRotationDegrees: Float,
    ) : WidgetSlotUiState

    data class DriverHealth(
        val heartRateText: String,
        val respirationRateText: String,
    ) : WidgetSlotUiState

    data class Weather(
        val locationText: String,
        val temperatureText: String,
        val temperatureUnitText: String,
        val icon: WeatherIconAsset?,
    ) : WidgetSlotUiState

    data class NavigationRoute(
        val destinationText: String,
        val distanceText: String,
        val durationText: String,
        val etaText: String,
        val progressFraction: Float,
    ) : WidgetSlotUiState

    data object TirePressureDemo : WidgetSlotUiState

    data object DriveToDisplay : WidgetSlotUiState

    data object LogoDisplay : WidgetSlotUiState
}

enum class WeatherIconAsset {
    DAY_CLEAR_SKY,
    DAY_FEW_CLOUDS,
    DAY_SCATTERED_CLOUDS,
    DAY_BROKEN_CLOUDS,
    DAY_SHOWER_RAIN,
    DAY_RAIN,
    DAY_LIGHT_RAIN,
    DAY_THUNDERSTORM,
    DAY_SNOW,
    DAY_MIST,
    DAY_DRIZZLE,
    NIGHT_CLEAR_SKY,
    NIGHT_FEW_CLOUDS,
    NIGHT_BROKEN_CLOUDS,
    NIGHT_SHOWER_RAIN,
    NIGHT_RAIN,
    NIGHT_LIGHT_RAIN,
    NIGHT_THUNDERSTORM,
    NIGHT_MIST,
    NIGHT_DRIZZLE,
}
