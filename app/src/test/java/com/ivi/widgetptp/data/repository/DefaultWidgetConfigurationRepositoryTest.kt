package com.ivi.widgetptp.data.repository

import com.ivi.widgetptp.data.fake.FakeWidgetConfigurationDataSource
import com.ivi.widgetptp.domain.model.WidgetType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultWidgetConfigurationRepositoryTest {
    @Test
    fun `default selection matches demo order`() = runTest {
        val repository = DefaultWidgetConfigurationRepository(
            FakeWidgetConfigurationDataSource(),
        )

        assertEquals(
            FakeWidgetConfigurationDataSource.DEFAULT_WIDGETS,
            repository.selectedWidgets.first(),
        )
    }

    @Test
    fun `data-source updates pass through in selection order`() = runTest {
        val dataSource = FakeWidgetConfigurationDataSource()
        val repository = DefaultWidgetConfigurationRepository(dataSource)
        val selection = listOf(
            WidgetType.WEATHER,
            WidgetType.TIRE_PRESSURE,
            WidgetType.LOGO_DISPLAY,
        )

        dataSource.updateSelectedWidgets(selection)

        assertEquals(selection, repository.selectedWidgets.first())
    }
}
