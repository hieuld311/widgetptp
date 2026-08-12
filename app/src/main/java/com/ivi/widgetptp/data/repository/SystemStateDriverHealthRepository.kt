package com.ivi.widgetptp.data.repository

import android.util.Log
import com.ivi.widgetptp.data.fauto.FAutoCarConnectionManager
import com.ivi.widgetptp.domain.model.DriverHealthData
import com.ivi.widgetptp.domain.repository.DriverHealthRepository
import fauto.car.FAutoCar
import fauto.car.systemstate.FAutoCarSystemStateManager
import fauto.car.systemstate.api.PropertyID
import fauto.car.systemstate.api.PropertyListener
import fauto.car.systemstate.api.PropertyValue
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class SystemStateDriverHealthRepository @Inject constructor(
    connectionManager: FAutoCarConnectionManager,
) : DriverHealthRepository {

    override val driverHealth: Flow<DriverHealthData?> = connectionManager.connectedCar
        .flatMapLatest { fAutoCar ->
            fAutoCar?.observeDriverHealth() ?: flowOf(null)
        }
        .distinctUntilChanged()

    private fun FAutoCar.observeDriverHealth(): Flow<DriverHealthData?> = callbackFlow {
        val manager = runCatching {
            getIVICarManager(FAutoCarSystemStateManager.SERVICE_NAME)
                as? FAutoCarSystemStateManager
                ?: error("System State Store manager is unavailable")
        }.getOrElse { error ->
            Log.e(TAG, "Unable to get System State Store manager", error)
            trySend(null)
            close()
            return@callbackFlow
        }

        val stateLock = Any()
        var heartRateBpm: Int? = null
        var respirationRateBrpm: Int? = null

        fun publishLatest() {
            val state = synchronized(stateLock) {
                val heartRate = heartRateBpm
                val respirationRate = respirationRateBrpm
                if (heartRate != null && respirationRate != null) {
                    DriverHealthData(
                        heartRateBpm = heartRate,
                        respirationRateBrpm = respirationRate,
                    )
                } else {
                    null
                }
            }
            trySend(state)
        }

        val heartRateListener = object : PropertyListener<Int> {
            override fun onPropertyChanged(value: PropertyValue<Int>) {
                val latestHeartRate: Int? =
                    manager.getProperty(PropertyID.DRIVER_HEART_RATE)
                synchronized(stateLock) {
                    heartRateBpm = latestHeartRate
                }
                publishLatest()
            }
        }

        val respirationRateListener = object : PropertyListener<Int> {
            override fun onPropertyChanged(value: PropertyValue<Int>) {
                val latestRespirationRate: Int? =
                    manager.getProperty(PropertyID.DRIVER_BREATH_RATE)
                synchronized(stateLock) {
                    respirationRateBrpm = latestRespirationRate
                }
                publishLatest()
            }
        }

        trySend(null)
        runCatching {
            manager.registerListener(
                PropertyID.DRIVER_HEART_RATE,
                heartRateListener,
            )
            manager.registerListener(
                PropertyID.DRIVER_BREATH_RATE,
                respirationRateListener,
            )

            synchronized(stateLock) {
                heartRateBpm = manager.getProperty(PropertyID.DRIVER_HEART_RATE)
                respirationRateBrpm = manager.getProperty(PropertyID.DRIVER_BREATH_RATE)
            }
            publishLatest()
        }.onFailure { error ->
            Log.e(TAG, "Unable to observe driver health properties", error)
            trySend(null)
        }

        awaitClose {
            runCatching {
                manager.unregisterListener(
                    PropertyID.DRIVER_HEART_RATE,
                    heartRateListener,
                )
            }
            runCatching {
                manager.unregisterListener(
                    PropertyID.DRIVER_BREATH_RATE,
                    respirationRateListener,
                )
            }
        }
    }

    private companion object {
        const val TAG = "SystemStateDriverHealth"
    }
}
