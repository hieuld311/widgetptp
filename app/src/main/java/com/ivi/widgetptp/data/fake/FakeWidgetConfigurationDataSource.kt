package com.ivi.widgetptp.data.fake

import com.ivi.widgetptp.domain.model.WidgetType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FakeWidgetConfigurationDataSource @Inject constructor() {
    private val mutableSelectedWidgets = MutableStateFlow(DEFAULT_WIDGETS)

    val selectedWidgets: StateFlow<List<WidgetType>> = mutableSelectedWidgets

    fun updateSelectedWidgets(widgets: List<WidgetType>) {
        mutableSelectedWidgets.value = widgets.toList()
    }

    companion object {
        val DEFAULT_WIDGETS = listOf(
            WidgetType.DRIVER_HEALTH,
            WidgetType.DATE_TIME,
            WidgetType.NAVIGATION,
        )
    }
}
