package com.ivi.widgetptp.data.repository

import android.content.Context
import android.text.format.DateFormat
import com.ivi.widgetptp.domain.model.DateTimeSnapshot
import com.ivi.widgetptp.domain.repository.DateTimeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

@Singleton
class   SystemDateTimeRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DateTimeRepository {
    override val dateTime: Flow<DateTimeSnapshot> = flow {
        while (currentCoroutineContext().isActive) {
            val now = Instant.now()
            emit(
                DateTimeSnapshot(
                    instant = now,
                    locale = Locale.getDefault(),
                    zoneId = ZoneId.systemDefault(),
                    is24HourFormat = DateFormat.is24HourFormat(context),
                ),
            )
            delay(1_000L - now.toEpochMilli().mod(1_000L))
        }
    }
}
