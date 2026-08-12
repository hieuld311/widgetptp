package com.ivi.widgetptp.domain.repository

import com.ivi.widgetptp.domain.model.DateTimeSnapshot
import com.ivi.widgetptp.domain.model.DriverHealthData
import com.ivi.widgetptp.domain.model.NavigationData
import com.ivi.widgetptp.domain.model.TemperatureUnit
import com.ivi.widgetptp.domain.model.TirePressureData
import com.ivi.widgetptp.domain.model.WeatherData
import com.ivi.widgetptp.domain.model.WidgetType
import kotlinx.coroutines.flow.Flow

interface WidgetConfigurationRepository {
    val selectedWidgets: Flow<List<WidgetType>>
}

interface DateTimeRepository {
    val dateTime: Flow<DateTimeSnapshot>
}

interface DriverHealthRepository {
    val driverHealth: Flow<DriverHealthData?>
}

interface WeatherRepository {
    val currentWeather: Flow<WeatherData?>
    val destinationWeather: Flow<WeatherData?>
}

interface TirePressureRepository {
    val tirePressure: Flow<TirePressureData?>
}

interface NavigationRepository {
    val navigation: Flow<NavigationData?>
}

interface VehicleDrivingStateRepository {
    val isDriving: Flow<Boolean>
}

interface UnitSettingsRepository {
    val temperatureUnit: Flow<TemperatureUnit>
}
