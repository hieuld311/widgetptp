package com.ivi.widgetptp.presentation.host

import com.ivi.widgetptp.domain.model.WidgetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetSelectionTest {
    @Test
    fun `selection preserves order and ignores entries after third`() {
        val selection = listOf(
            WidgetType.WEATHER,
            WidgetType.LOGO_DISPLAY,
            WidgetType.DATE_TIME,
            WidgetType.TIRE_PRESSURE,
        ).toThreeSlotSelection()

        assertEquals(WidgetType.WEATHER, selection.first)
        assertEquals(WidgetType.LOGO_DISPLAY, selection.second)
        assertEquals(WidgetType.DATE_TIME, selection.third)
    }

    @Test
    fun `selection pads missing entries with empty slots`() {
        val selection = listOf(WidgetType.DATE_TIME).toThreeSlotSelection()

        assertEquals(WidgetType.DATE_TIME, selection.first)
        assertNull(selection.second)
        assertNull(selection.third)
    }
}
