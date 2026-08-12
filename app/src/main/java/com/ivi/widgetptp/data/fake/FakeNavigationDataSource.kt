package com.ivi.widgetptp.data.fake

import com.ivi.widgetptp.domain.model.NavigationData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FakeNavigationDataSource @Inject constructor() {
    private val mutableNavigation = MutableStateFlow<NavigationData?>(
        NavigationData(
            destination = "Home",
            eta = "13:15",
            suggestedPoi = null,
            remainingDistanceKilometers = 3.0,
            remainingDurationMinutes = 10,
            routeProgress = 0.64f,
        ),
    )

    val navigation: StateFlow<NavigationData?> = mutableNavigation

    fun updateNavigation(value: NavigationData?) {
        mutableNavigation.value = value
    }
}
