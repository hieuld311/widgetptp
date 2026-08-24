package com.ivi.widgetptp.presentation.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivi.widgetptp.domain.model.TemperatureUnit
import com.ivi.widgetptp.domain.model.WeatherData
import com.ivi.widgetptp.domain.model.WidgetType
import com.ivi.widgetptp.domain.repository.DateTimeRepository
import com.ivi.widgetptp.domain.repository.DriverHealthRepository
import com.ivi.widgetptp.domain.repository.NavigationRepository
import com.ivi.widgetptp.domain.repository.UnitSettingsRepository
import com.ivi.widgetptp.domain.repository.VehicleDrivingStateRepository
import com.ivi.widgetptp.domain.repository.WeatherRepository
import com.ivi.widgetptp.domain.repository.WidgetConfigurationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class WidgetHostViewModel @Inject constructor(
    widgetConfigurationRepository: WidgetConfigurationRepository,
    private val dateTimeRepository: DateTimeRepository,
    private val driverHealthRepository: DriverHealthRepository,
    private val weatherRepository: WeatherRepository,
    private val navigationRepository: NavigationRepository,
    private val vehicleDrivingStateRepository: VehicleDrivingStateRepository,
    private val unitSettingsRepository: UnitSettingsRepository,
    private val dateTimeUiMapper: DateTimeUiMapper,
    private val driverHealthUiMapper: DriverHealthUiMapper,
    private val navigationUiMapper: NavigationUiMapper,
    private val weatherUiMapper: WeatherUiMapper,
) : ViewModel() {

    val uiState = widgetConfigurationRepository.selectedWidgets
        .flatMapLatest(::observeSelection)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
            initialValue = WidgetHostUiState(),
        )

    private fun observeSelection(selectedWidgets: List<WidgetType>): Flow<WidgetHostUiState> {
        val selection = selectedWidgets.toThreeSlotSelection()
        return combine(
            observeSlot(selection.first),
            observeSlot(selection.second),
            observeSlot(selection.third),
        ) { first, second, third ->
            WidgetHostUiState(first = first, second = second, third = third)
        }
    }

    private fun observeSlot(type: WidgetType?): Flow<WidgetSlotUiState> = when (type) {
        null -> flowOf(WidgetSlotUiState.Empty)
        WidgetType.DATE_TIME -> dateTimeRepository.dateTime.map(dateTimeUiMapper::map)
        WidgetType.DRIVER_HEALTH -> driverHealthRepository.driverHealth
            .map(driverHealthUiMapper::map)
        WidgetType.WEATHER -> observeWeather()
        WidgetType.TIRE_PRESSURE -> vehicleDrivingStateRepository.isDriving.map { isDriving ->
            if (isDriving) WidgetSlotUiState.TirePressureDemo else WidgetSlotUiState.DriveToDisplay
        }
        WidgetType.LOGO_DISPLAY -> flowOf(WidgetSlotUiState.LogoDisplay)
        WidgetType.NAVIGATION -> navigationRepository.navigation.map { data ->
            data?.let(navigationUiMapper::map) ?: WidgetSlotUiState.Empty
        }
    }

    private fun observeWeather(): Flow<WidgetSlotUiState> = combine(
        weatherRepository.currentWeather,
        weatherRepository.destinationWeather,
        unitSettingsRepository.temperatureUnit,
    ) { current, destination, unit ->
        WeatherInputs(current = current, destination = destination, unit = unit)
    }.flatMapLatest { inputs ->
        when {
            inputs.current == null -> flowOf(inputs.current.toUiState(inputs.unit))
            inputs.destination == null -> flowOf(inputs.current.toUiState(inputs.unit))
            else -> rotatingWeather(inputs)
        }
    }

    private fun rotatingWeather(inputs: WeatherInputs): Flow<WidgetSlotUiState> = flow {
        var showCurrent = true
        while (currentCoroutineContext().isActive) {
            val weather = if (showCurrent) inputs.current else inputs.destination
            emit(weather.toUiState(inputs.unit))
            showCurrent = !showCurrent
            delay(WEATHER_ROTATION_INTERVAL_MILLIS)
        }
    }

    private fun WeatherData?.toUiState(unit: TemperatureUnit): WidgetSlotUiState =
        weatherUiMapper.map(this, unit) ?: WidgetSlotUiState.Empty

    private data class WeatherInputs(
        val current: WeatherData?,
        val destination: WeatherData?,
        val unit: TemperatureUnit,
    )

    companion object {
        const val WEATHER_ROTATION_INTERVAL_MILLIS = 5_000L
    }
}

internal data class ThreeSlotSelection(
    val first: WidgetType?,
    val second: WidgetType?,
    val third: WidgetType?,
)

internal fun List<WidgetType>.toThreeSlotSelection(): ThreeSlotSelection = ThreeSlotSelection(
    first = getOrNull(0),
    second = getOrNull(1),
    third = getOrNull(2),
)
