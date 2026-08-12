package com.ivi.widgetptp.data.fake

import com.ivi.widgetptp.domain.model.TemperatureUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FakeUnitSettingsDataSource @Inject constructor() {
    private val mutableTemperatureUnit = MutableStateFlow(TemperatureUnit.CELSIUS)

    val temperatureUnit: StateFlow<TemperatureUnit> = mutableTemperatureUnit

    fun updateTemperatureUnit(value: TemperatureUnit) {
        mutableTemperatureUnit.value = value
    }
}
