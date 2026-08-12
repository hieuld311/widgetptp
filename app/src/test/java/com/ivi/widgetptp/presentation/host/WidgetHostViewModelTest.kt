package com.ivi.widgetptp.presentation.host

import com.ivi.widgetptp.data.fake.FakeDriverHealthDataSource
import com.ivi.widgetptp.data.fake.FakeNavigationDataSource
import com.ivi.widgetptp.data.fake.FakeTirePressureDataSource
import com.ivi.widgetptp.data.fake.FakeUnitSettingsDataSource
import com.ivi.widgetptp.data.fake.FakeVehicleDrivingStateDataSource
import com.ivi.widgetptp.data.fake.FakeWeatherDataSource
import com.ivi.widgetptp.data.fake.FakeWidgetConfigurationDataSource
import com.ivi.widgetptp.data.repository.DefaultDriverHealthRepository
import com.ivi.widgetptp.data.repository.DefaultNavigationRepository
import com.ivi.widgetptp.data.repository.DefaultTirePressureRepository
import com.ivi.widgetptp.data.repository.DefaultUnitSettingsRepository
import com.ivi.widgetptp.data.repository.DefaultVehicleDrivingStateRepository
import com.ivi.widgetptp.data.repository.DefaultWeatherRepository
import com.ivi.widgetptp.data.repository.DefaultWidgetConfigurationRepository
import com.ivi.widgetptp.domain.model.DateTimeSnapshot
import com.ivi.widgetptp.domain.model.WidgetType
import com.ivi.widgetptp.domain.repository.DateTimeRepository
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WidgetHostViewModelTest {
    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `configuration change immediately replaces all three ordered slots`() = runTest(dispatcher) {
        val configurationDataSource = FakeWidgetConfigurationDataSource()
        configurationDataSource.updateSelectedWidgets(
            listOf(
                WidgetType.DRIVER_HEALTH,
                WidgetType.NAVIGATION,
                WidgetType.DATE_TIME,
            ),
        )
        val configuration = DefaultWidgetConfigurationRepository(configurationDataSource)
        val driverHealthDataSource = FakeDriverHealthDataSource()
        val driverHealth = DefaultDriverHealthRepository(driverHealthDataSource)
        val weatherDataSource = FakeWeatherDataSource()
        val weather = DefaultWeatherRepository(weatherDataSource)
        val tirePressure = DefaultTirePressureRepository(FakeTirePressureDataSource())
        val navigation = DefaultNavigationRepository(FakeNavigationDataSource())
        val drivingState = DefaultVehicleDrivingStateRepository(
            FakeVehicleDrivingStateDataSource(),
        )
        val unitSettings = DefaultUnitSettingsRepository(FakeUnitSettingsDataSource())
        val dateTimeFlow = MutableStateFlow(
            DateTimeSnapshot(
                instant = Instant.parse("2026-06-20T00:00:00Z"),
                locale = Locale.US,
                zoneId = ZoneId.of("UTC"),
                is24HourFormat = true,
            ),
        )
        val viewModel = WidgetHostViewModel(
            widgetConfigurationRepository = configuration,
            dateTimeRepository = object : DateTimeRepository {
                override val dateTime = dateTimeFlow
            },
            driverHealthRepository = driverHealth,
            weatherRepository = weather,
            tirePressureRepository = tirePressure,
            navigationRepository = navigation,
            vehicleDrivingStateRepository = drivingState,
            unitSettingsRepository = unitSettings,
            dateTimeUiMapper = DateTimeUiMapper(),
            driverHealthUiMapper = DriverHealthUiMapper(),
            navigationUiMapper = NavigationUiMapper(),
            weatherUiMapper = WeatherUiMapper(),
        )
        val collection = backgroundScope.launch(dispatcher) {
            viewModel.uiState.collect {}
        }
        runCurrent()

        assertTrue(viewModel.uiState.value.first is WidgetSlotUiState.DriverHealth)
        assertTrue(viewModel.uiState.value.second is WidgetSlotUiState.NavigationRoute)
        assertTrue(viewModel.uiState.value.third is WidgetSlotUiState.DateTime)

        driverHealthDataSource.updateDriverHealth(null)
        runCurrent()

        val unavailableHealth = viewModel.uiState.value.first as WidgetSlotUiState.DriverHealth
        assertEquals("0", unavailableHealth.heartRateText)
        assertEquals("0", unavailableHealth.respirationRateText)

        configurationDataSource.updateSelectedWidgets(
            listOf(
                WidgetType.WEATHER,
                WidgetType.TIRE_PRESSURE,
                WidgetType.LOGO_DISPLAY,
            ),
        )
        runCurrent()

        assertTrue(viewModel.uiState.value.first is WidgetSlotUiState.Weather)
        assertTrue(viewModel.uiState.value.second is WidgetSlotUiState.TirePressureDemo)
        assertTrue(viewModel.uiState.value.third is WidgetSlotUiState.LogoDisplay)

        weatherDataSource.updateCurrentWeather(null)
        runCurrent()

        val unavailableWeather = viewModel.uiState.value.first as WidgetSlotUiState.Weather
        assertEquals("0", unavailableWeather.temperatureText)
        assertEquals("", unavailableWeather.locationText)
        collection.cancel()
    }
}
