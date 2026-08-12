package com.ivi.widgetptp.presentation.host

import com.ivi.widgetptp.domain.model.DateTimeSnapshot
import com.ivi.widgetptp.domain.model.DriverHealthData
import com.ivi.widgetptp.domain.model.NavigationData
import com.ivi.widgetptp.domain.model.TemperatureUnit
import com.ivi.widgetptp.domain.model.WeatherCondition
import com.ivi.widgetptp.domain.model.WeatherData
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetUiMappersTest {
    @Test
    fun `navigation mapper prepares route text and clamps progress`() {
        val state = requireNotNull(
            NavigationUiMapper().map(
                NavigationData(
                    destination = "Home",
                    eta = "13:15",
                    suggestedPoi = null,
                    remainingDistanceKilometers = 3.0,
                    remainingDurationMinutes = 10,
                    routeProgress = 1.4f,
                ),
            ),
        )

        assertEquals("Home", state.destinationText)
        assertEquals("3km", state.distanceText)
        assertEquals("10 mins", state.durationText)
        assertEquals("13:15", state.etaText)
        assertEquals(1f, state.progressFraction)
    }

    @Test
    fun `driver-health mapper prepares both measurements for the first design`() {
        val state = DriverHealthUiMapper().map(
            DriverHealthData(heartRateBpm = 72, respirationRateBrpm = 14),
        )

        assertEquals("72", state.heartRateText)
        assertEquals("14", state.respirationRateText)
    }

    @Test
    fun `driver-health mapper uses zero when data is unavailable`() {
        val state = DriverHealthUiMapper().map(null)

        assertEquals("0", state.heartRateText)
        assertEquals("0", state.respirationRateText)
    }

    @Test
    fun `date-time mapper formats values and calculates clock rotations`() {
        val state = DateTimeUiMapper().map(
            DateTimeSnapshot(
                instant = Instant.parse("2026-06-20T00:00:30Z"),
                locale = Locale.US,
                zoneId = ZoneId.of("UTC"),
                is24HourFormat = true,
            ),
        )

        assertEquals("00:00", state.timeText)
        assertEquals("", state.dayPeriodText)
        assertEquals("Sat, Jun.20. 2026", state.dateText)
        assertEquals(0f, state.hourRotationDegrees)
        assertEquals(3f, state.minuteRotationDegrees)
        assertEquals(180f, state.secondRotationDegrees)
    }

    @Test
    fun `weather mapper rounds and converts temperature`() {
        val weather = WeatherData(
            locationName = "Ha Noi, Vietnam",
            temperatureCelsius = 24.5,
            condition = WeatherCondition.CLEAR_SKY,
            isDaylight = true,
        )
        val mapper = WeatherUiMapper()

        val celsius = requireNotNull(mapper.map(weather, TemperatureUnit.CELSIUS))
        val fahrenheit = requireNotNull(mapper.map(weather, TemperatureUnit.FAHRENHEIT))

        assertEquals("25", celsius.temperatureText)
        assertEquals("°C", celsius.temperatureUnitText)
        assertEquals("76", fahrenheit.temperatureText)
        assertEquals("°F", fahrenheit.temperatureUnitText)
    }

    @Test
    fun `weather mapper uses zero when data is unavailable`() {
        val state = requireNotNull(
            WeatherUiMapper().map(null, TemperatureUnit.CELSIUS),
        )

        assertEquals("0", state.temperatureText)
        assertEquals("°C", state.temperatureUnitText)
        assertEquals("", state.locationText)
        assertNull(state.icon)
    }

    @Test
    fun `weather mapper returns unavailable when exact asset is absent`() {
        val state = WeatherUiMapper().map(
            weather = WeatherData(
                locationName = "Destination",
                temperatureCelsius = 0.0,
                condition = WeatherCondition.SNOW,
                isDaylight = false,
            ),
            unit = TemperatureUnit.CELSIUS,
        )

        assertNull(state)
    }
}
