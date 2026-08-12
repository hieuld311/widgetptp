package com.ivi.widgetptp.data.fake

import com.ivi.widgetptp.domain.model.DriverHealthData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FakeDriverHealthDataSource @Inject constructor() {
    private val mutableDriverHealth = MutableStateFlow<DriverHealthData?>(
        DriverHealthData(heartRateBpm = 72, respirationRateBrpm = 14),
    )

    val driverHealth: StateFlow<DriverHealthData?> = mutableDriverHealth

    fun updateDriverHealth(value: DriverHealthData?) {
        mutableDriverHealth.value = value
    }
}
