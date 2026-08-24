package com.ivi.widgetptp.di

import com.ivi.widgetptp.data.repository.DefaultUnitSettingsRepository
import com.ivi.widgetptp.data.repository.DefaultVehicleDrivingStateRepository
import com.ivi.widgetptp.data.repository.DefaultWidgetConfigurationRepository
import com.ivi.widgetptp.data.repository.SystemDateTimeRepository
import com.ivi.widgetptp.data.repository.SystemStateDriverHealthRepository
import com.ivi.widgetptp.data.repository.SystemStateNavigationRepository
import com.ivi.widgetptp.data.repository.SystemStateWeatherRepository
import com.ivi.widgetptp.domain.repository.DateTimeRepository
import com.ivi.widgetptp.domain.repository.DriverHealthRepository
import com.ivi.widgetptp.domain.repository.NavigationRepository
import com.ivi.widgetptp.domain.repository.UnitSettingsRepository
import com.ivi.widgetptp.domain.repository.VehicleDrivingStateRepository
import com.ivi.widgetptp.domain.repository.WeatherRepository
import com.ivi.widgetptp.domain.repository.WidgetConfigurationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWidgetConfigurationRepository(
        implementation: DefaultWidgetConfigurationRepository,
    ): WidgetConfigurationRepository

    @Binds
    @Singleton
    abstract fun bindDateTimeRepository(
        implementation: SystemDateTimeRepository,
    ): DateTimeRepository

    @Binds
    @Singleton
    abstract fun bindDriverHealthRepository(
        implementation: SystemStateDriverHealthRepository,
    ): DriverHealthRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        implementation: SystemStateWeatherRepository,
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindNavigationRepository(
        implementation: SystemStateNavigationRepository,
    ): NavigationRepository

    @Binds
    @Singleton
    abstract fun bindVehicleDrivingStateRepository(
        implementation: DefaultVehicleDrivingStateRepository,
    ): VehicleDrivingStateRepository

    @Binds
    @Singleton
    abstract fun bindUnitSettingsRepository(
        implementation: DefaultUnitSettingsRepository,
    ): UnitSettingsRepository
}
