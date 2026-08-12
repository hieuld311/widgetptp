package com.ivi.widgetptp.domain.policy

import com.ivi.widgetptp.domain.model.WeatherCondition
import javax.inject.Inject

class WeatherCodePolicy @Inject constructor() {
    fun map(code: Int): WeatherCondition? = when (code) {
        0 -> WeatherCondition.CLEAR_SKY
        1, 2, 3 -> WeatherCondition.BROKEN_CLOUDS
        45, 48 -> WeatherCondition.MIST
        51, 53, 55 -> WeatherCondition.DRIZZLE
        61, 63, 65 -> WeatherCondition.RAIN
        71, 73, 75 -> WeatherCondition.SNOW
        95 -> WeatherCondition.THUNDERSTORM
        else -> null
    }
}

class DaylightPolicy @Inject constructor() {
    fun isDaylight(hourOfDay: Int): Boolean = hourOfDay in 6 until 18
}
