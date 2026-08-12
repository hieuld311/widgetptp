package com.ivi.widgetptp.data.repository

import com.ivi.widgetptp.data.fake.FakeUnitSettingsDataSource
import com.ivi.widgetptp.domain.model.TemperatureUnit
import com.ivi.widgetptp.domain.repository.UnitSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultUnitSettingsRepository @Inject constructor(
    private val dataSource: FakeUnitSettingsDataSource,
) : UnitSettingsRepository {
    override val temperatureUnit: Flow<TemperatureUnit> = dataSource.temperatureUnit
}
