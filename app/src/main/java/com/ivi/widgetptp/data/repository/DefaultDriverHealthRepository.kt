package com.ivi.widgetptp.data.repository

import com.ivi.widgetptp.data.fake.FakeDriverHealthDataSource
import com.ivi.widgetptp.domain.model.DriverHealthData
import com.ivi.widgetptp.domain.repository.DriverHealthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultDriverHealthRepository @Inject constructor(
    private val dataSource: FakeDriverHealthDataSource,
) : DriverHealthRepository {
    override val driverHealth: Flow<DriverHealthData?> = dataSource.driverHealth
}
