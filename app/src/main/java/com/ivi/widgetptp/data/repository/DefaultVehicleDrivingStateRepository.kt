package com.ivi.widgetptp.data.repository

import com.ivi.widgetptp.data.fake.FakeVehicleDrivingStateDataSource
import com.ivi.widgetptp.domain.repository.VehicleDrivingStateRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultVehicleDrivingStateRepository @Inject constructor(
    private val dataSource: FakeVehicleDrivingStateDataSource,
) : VehicleDrivingStateRepository {
    override val isDriving: Flow<Boolean> = dataSource.isDriving
}
