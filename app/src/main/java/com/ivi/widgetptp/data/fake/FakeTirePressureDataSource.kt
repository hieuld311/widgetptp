package com.ivi.widgetptp.data.fake

import com.ivi.widgetptp.domain.model.TirePressureData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FakeTirePressureDataSource @Inject constructor() {
    private val mutableTirePressure = MutableStateFlow<TirePressureData?>(
        TirePressureData(
            frontLeftPsi = 40,
            frontRightPsi = 40,
            rearLeftPsi = 40,
            rearRightPsi = 40,
        ),
    )

    val tirePressure: StateFlow<TirePressureData?> = mutableTirePressure

    fun updateTirePressure(value: TirePressureData?) {
        mutableTirePressure.value = value
    }
}
