package com.ivi.widgetptp.data.repository

import com.ivi.widgetptp.data.fake.FakeTirePressureDataSource
import com.ivi.widgetptp.domain.model.TirePressureData
import com.ivi.widgetptp.domain.repository.TirePressureRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultTirePressureRepository @Inject constructor(
    private val dataSource: FakeTirePressureDataSource,
) : TirePressureRepository {
    override val tirePressure: Flow<TirePressureData?> = dataSource.tirePressure
}
