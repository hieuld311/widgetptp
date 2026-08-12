package com.ivi.widgetptp.data.fauto

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import fauto.car.FAutoCar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn

@Singleton
class FAutoCarConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val connectedCar: Flow<FAutoCar?> = callbackFlow {
        var fAutoCar: FAutoCar? = null

        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                trySend(fAutoCar)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                trySend(null)
            }
        }

        trySend(null)
        runCatching {
            fAutoCar = FAutoCar.createFAutoCar(context, serviceConnection)
            fAutoCar?.connect()
        }.onFailure { error ->
            Log.e(TAG, "Unable to connect to FAutoCar", error)
            trySend(null)
        }

        awaitClose {
            runCatching { fAutoCar?.disconnect() }
        }
    }
        .distinctUntilChanged()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            replay = 1,
        )

    private companion object {
        const val TAG = "FAutoCarConnection"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
