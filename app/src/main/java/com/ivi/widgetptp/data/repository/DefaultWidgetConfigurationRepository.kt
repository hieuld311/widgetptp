package com.ivi.widgetptp.data.repository

import com.ivi.widgetptp.data.fake.FakeWidgetConfigurationDataSource
import com.ivi.widgetptp.domain.model.WidgetType
import com.ivi.widgetptp.domain.repository.WidgetConfigurationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultWidgetConfigurationRepository @Inject constructor(
    private val dataSource: FakeWidgetConfigurationDataSource,
) : WidgetConfigurationRepository {
    override val selectedWidgets: Flow<List<WidgetType>> = dataSource.selectedWidgets
}
