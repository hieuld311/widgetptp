package com.ivi.widgetptp.data.fake

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FakeVehicleDrivingStateDataSource @Inject constructor() {
    private val mutableIsDriving = MutableStateFlow(true)

    val isDriving: StateFlow<Boolean> = mutableIsDriving

    fun updateDrivingState(value: Boolean) {
        mutableIsDriving.value = value
    }
}
