package com.ivi.widgetptp.data.repository

import com.ivi.widgetptp.data.fake.FakeNavigationDataSource
import com.ivi.widgetptp.domain.model.NavigationData
import com.ivi.widgetptp.domain.repository.NavigationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class DefaultNavigationRepository @Inject constructor(
    private val dataSource: FakeNavigationDataSource,
) : NavigationRepository {
    override val navigation: Flow<NavigationData?> = dataSource.navigation
}
