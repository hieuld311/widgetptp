package com.ivi.widgetptp.data.repository

import android.util.Log
import com.ivi.widgetptp.data.fauto.FAutoCarConnectionManager
import com.ivi.widgetptp.domain.model.NavigationData
import com.ivi.widgetptp.domain.repository.NavigationRepository
import fauto.car.FAutoCar
import fauto.car.systemstate.FAutoCarSystemStateManager
import fauto.car.systemstate.api.PropertyID
import fauto.car.systemstate.api.PropertyListener
import fauto.car.systemstate.api.PropertyValue
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class SystemStateNavigationRepository @Inject constructor(
    connectionManager: FAutoCarConnectionManager,
) : NavigationRepository {

    override val navigation: Flow<NavigationData?> = connectionManager.connectedCar
        .flatMapLatest { fAutoCar ->
            Log.i(TAG, "connectedCar emitted: fAutoCar=${if (fAutoCar == null) "null" else "connected"}")
            fAutoCar?.observeNavigationProperties() ?: flowOf(PLACEHOLDER)
        }
        .distinctUntilChanged()

    private fun FAutoCar.observeNavigationProperties(): Flow<NavigationData?> = callbackFlow {
        val manager = runCatching {
            getIVICarManager(FAutoCarSystemStateManager.SERVICE_NAME)
                as? FAutoCarSystemStateManager
                ?: error("System State Store manager is unavailable")
        }.getOrElse { error ->
            Log.e(TAG, "Unable to get System State Store manager", error)
            trySend(PLACEHOLDER)
            close()
            return@callbackFlow
        }
        Log.i(TAG, "System State Store manager acquired: $manager")

        val stateLock = Any()
        var active: Boolean = false
        var destination: String? = null
        var etaSeconds: Int? = null
        var remainingDistanceMeters: Float? = null
        var totalDistanceMetersBaseline: Float? = null

        fun routeProgress(): Float {
            val total = totalDistanceMetersBaseline
            val remaining = remainingDistanceMeters
            if (total == null || remaining == null || total <= 0f) return PLACEHOLDER.routeProgress!!
            return ((total - remaining) / total).coerceIn(0f, 1f)
        }

        fun publishLatest() {
            val data = synchronized(stateLock) {
                if (!active) {
                    Log.d(TAG, "publishLatest: NAVIGATION_ACTIVE=false, emitting PLACEHOLDER")
                    PLACEHOLDER
                } else {
                    NavigationData(
                        destination = destination ?: PLACEHOLDER.destination,
                        eta = etaSeconds?.let(::formatEta) ?: PLACEHOLDER.eta,
                        suggestedPoi = null,
                        remainingDistanceKilometers = remainingDistanceMeters
                            ?.let { it / 1000.0 }
                            ?: PLACEHOLDER.remainingDistanceKilometers,
                        remainingDurationMinutes = etaSeconds
                            ?.let { if (it <= 0) 0 else max(1, it / 60) }
                            ?: PLACEHOLDER.remainingDurationMinutes,
                        routeProgress = routeProgress(),
                    ).also {
                        Log.d(TAG, "publishLatest: active=true destination=$destination " +
                            "etaSeconds=$etaSeconds remainingDistanceMeters=$remainingDistanceMeters " +
                            "baseline=$totalDistanceMetersBaseline -> $it")
                    }
                }
            }
            trySend(data)
        }

        val activeListener = object : PropertyListener<Int> {
            override fun onPropertyChanged(value: PropertyValue<Int>) {
                Log.i(TAG, "NAVIGATION_ACTIVE changed: ${value.value}")
                synchronized(stateLock) {
                    val nowActive = value.value != 0
                    if (nowActive && !active) {
                        totalDistanceMetersBaseline = null
                    }
                    active = nowActive
                }
                publishLatest()
            }
        }

        val destinationListener = object : PropertyListener<String> {
            override fun onPropertyChanged(value: PropertyValue<String>) {
                Log.i(TAG, "NAVIGATION_DESTINATION_NAME changed: ${value.value}")
                synchronized(stateLock) { destination = value.value }
                publishLatest()
            }
        }

        val etaListener = object : PropertyListener<Int> {
            override fun onPropertyChanged(value: PropertyValue<Int>) {
                Log.i(TAG, "NAVIGATION_ETA_SECONDS changed: ${value.value}")
                synchronized(stateLock) { etaSeconds = value.value }
                publishLatest()
            }
        }

        val distanceListener = object : PropertyListener<Float> {
            override fun onPropertyChanged(value: PropertyValue<Float>) {
                Log.i(TAG, "NAVIGATION_REMAINING_DISTANCE changed: ${value.value}")
                synchronized(stateLock) {
                    remainingDistanceMeters = value.value
                    if (totalDistanceMetersBaseline == null) {
                        totalDistanceMetersBaseline = value.value
                    }
                }
                publishLatest()
            }
        }

        trySend(PLACEHOLDER)
        runCatching {
            manager.registerListener(PropertyID.NAVIGATION_ACTIVE, activeListener)
            manager.registerListener(PropertyID.NAVIGATION_DESTINATION_NAME, destinationListener)
            manager.registerListener(PropertyID.NAVIGATION_ETA_SECONDS, etaListener)
            manager.registerListener(PropertyID.NAVIGATION_REMAINING_DISTANCE, distanceListener)
            Log.i(TAG, "registerListener calls completed for all 4 navigation properties")

            synchronized(stateLock) {
                active = (manager.getProperty<Int>(PropertyID.NAVIGATION_ACTIVE) ?: 0) != 0
                destination = manager.getProperty(PropertyID.NAVIGATION_DESTINATION_NAME)
                etaSeconds = manager.getProperty(PropertyID.NAVIGATION_ETA_SECONDS)
                remainingDistanceMeters = manager.getProperty(PropertyID.NAVIGATION_REMAINING_DISTANCE)
                totalDistanceMetersBaseline = remainingDistanceMeters
                Log.i(TAG, "Initial getProperty snapshot: active=$active destination=$destination " +
                    "etaSeconds=$etaSeconds remainingDistanceMeters=$remainingDistanceMeters")
            }
            publishLatest()
        }.onFailure { error ->
            Log.e(TAG, "Unable to observe navigation properties", error)
            trySend(PLACEHOLDER)
        }

        awaitClose {
            Log.i(TAG, "observeNavigationProperties: awaitClose, unregistering listeners")
            runCatching { manager.unregisterListener(PropertyID.NAVIGATION_ACTIVE, activeListener) }
            runCatching {
                manager.unregisterListener(PropertyID.NAVIGATION_DESTINATION_NAME, destinationListener)
            }
            runCatching { manager.unregisterListener(PropertyID.NAVIGATION_ETA_SECONDS, etaListener) }
            runCatching {
                manager.unregisterListener(PropertyID.NAVIGATION_REMAINING_DISTANCE, distanceListener)
            }
        }
    }

    private fun formatEta(etaSeconds: Int): String {
        val arrival = LocalTime.now().plusSeconds(max(etaSeconds, 0).toLong())
        return arrival.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
    }

    private companion object {
        const val TAG = "SystemStateNavigation"

        val PLACEHOLDER = NavigationData(
            destination = "NOT SET",
            eta = "0",
            suggestedPoi = null,
            remainingDistanceKilometers = 0.0,
            remainingDurationMinutes = 0,
            routeProgress = 0f,
        )
    }
}
