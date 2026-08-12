package com.ivi.widgetptp.domain.policy

import com.ivi.widgetptp.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherPoliciesTest {
    private val weatherCodePolicy = WeatherCodePolicy()
    private val daylightPolicy = DaylightPolicy()

    @Test
    fun `weather codes map to supported conditions`() {
        assertEquals(WeatherCondition.CLEAR_SKY, weatherCodePolicy.map(0))
        assertEquals(WeatherCondition.BROKEN_CLOUDS, weatherCodePolicy.map(2))
        assertEquals(WeatherCondition.MIST, weatherCodePolicy.map(45))
        assertEquals(WeatherCondition.DRIZZLE, weatherCodePolicy.map(53))
        assertEquals(WeatherCondition.RAIN, weatherCodePolicy.map(63))
        assertEquals(WeatherCondition.SNOW, weatherCodePolicy.map(73))
        assertEquals(WeatherCondition.THUNDERSTORM, weatherCodePolicy.map(95))
        assertNull(weatherCodePolicy.map(99))
    }

    @Test
    fun `daylight is from six inclusive until eighteen exclusive`() {
        assertFalse(daylightPolicy.isDaylight(5))
        assertTrue(daylightPolicy.isDaylight(6))
        assertTrue(daylightPolicy.isDaylight(17))
        assertFalse(daylightPolicy.isDaylight(18))
    }
}
