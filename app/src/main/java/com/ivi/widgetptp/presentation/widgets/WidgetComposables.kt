package com.ivi.widgetptp.presentation.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivi.widgetptp.R
import com.ivi.widgetptp.presentation.host.WeatherIconAsset
import com.ivi.widgetptp.presentation.host.WidgetSlotUiState

private val SecondaryTextColor = Color(0xFFB7B7C2)
private val ClockFaceColor = Color(0xFF262739)
private val NavigationContentWidth = 416.dp
private val NavigationContentHeight = 119.dp
private val NavigationContentX = 75.5.dp
private val NavigationContentY = 20.5.dp
private val NavigationMarkerX = 1.5.dp
private val NavigationMarkerY = 42.dp
private val NavigationMarkerWidth = 102.dp
private val NavigationMarkerHeight = 84.dp

@Composable
fun WidgetSlot(
    state: WidgetSlotUiState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        WidgetSlotUiState.Empty -> Box(modifier = modifier)
        is WidgetSlotUiState.DateTime -> WidgetBackground(modifier) {
            DateTimeWidget(state)
        }
        is WidgetSlotUiState.DriverHealth -> WidgetBackground(modifier) {
            DriverHealthWidget(state)
        }
        is WidgetSlotUiState.Weather -> WidgetBackground(modifier) {
            WeatherWidget(state)
        }
        is WidgetSlotUiState.NavigationRoute -> WidgetBackground(modifier) {
            NavigationWidget(state)
        }
        WidgetSlotUiState.TirePressureDemo -> WidgetBackground(modifier) {
            TirePressureWidget()
        }
        WidgetSlotUiState.DriveToDisplay -> WidgetBackground(modifier) {
            Text(
                text = stringResource(R.string.drive_to_display),
                color = Color.White,
                fontSize = 24.sp,
            )
        }
        WidgetSlotUiState.LogoDisplay -> WidgetBackground(modifier) {
            LogoWidget()
        }
    }
}

@Composable
private fun WidgetBackground(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.img_cluster_widget_background_n),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        content()
    }
}

@Composable
fun NavigationWidget(state: WidgetSlotUiState.NavigationRoute) {
    val progress = state.progressFraction.coerceIn(0f, 1f)
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .offset(x = NavigationContentX, y = NavigationContentY)
                .size(
                    width = NavigationContentWidth,
                    height = NavigationContentHeight,
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            NavigationDestination(state.destinationText)
            NavigationProgressBar(progress)
            NavigationRouteDetails(state)
        }

        // The arrow tip at x=74dp follows the progress endpoint.
        Image(
            painter = painterResource(R.drawable.img_launcher_navigation_tracking_route),
            contentDescription = stringResource(R.string.navigation_route_position),
            modifier = Modifier
                .offset(
                    x = NavigationMarkerX + NavigationContentWidth * progress,
                    y = NavigationMarkerY,
                )
                .size(
                    width = NavigationMarkerWidth,
                    height = NavigationMarkerHeight,
                ),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun NavigationDestination(destinationText: String) {
    Row(
        modifier = Modifier.size(width = 135.dp, height = 41.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ico_widget_tbt_current_position),
            contentDescription = stringResource(R.string.navigation_current_position),
            modifier = Modifier.size(41.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = destinationText,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NavigationProgressBar(progressFraction: Float) {
    Box(
        modifier = Modifier.size(width = NavigationContentWidth, height = 35.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFF393B4A)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(progressFraction)
                .height(6.dp)
                .background(Color(0xFF00D7DB)),
        )
    }
}

@Composable
private fun NavigationRouteDetails(state: WidgetSlotUiState.NavigationRoute) {
    Row(
        modifier = Modifier.size(width = NavigationContentWidth, height = 35.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = state.distanceText,
            color = SecondaryTextColor,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Text(
            text = state.durationText,
            color = SecondaryTextColor,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = state.etaText,
            color = SecondaryTextColor,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

@Composable
fun DriverHealthWidget(state: WidgetSlotUiState.DriverHealth) {
    Row(
        modifier = Modifier.size(width = 440.dp, height = 104.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HealthMetric(
            iconResource = R.drawable.ico_cluster_widget_driver_health_heart_rate_n,
            iconDescription = stringResource(R.string.heart_rate_icon),
            valueText = state.heartRateText,
            unitText = stringResource(R.string.heart_rate_unit),
            labelText = stringResource(R.string.heart_rate_label),
        )
        HealthMetric(
            iconResource = R.drawable.ico_cluster_widget_driver_health_breathe_rate_n,
            iconDescription = stringResource(R.string.respiration_rate_icon),
            valueText = state.respirationRateText,
            unitText = stringResource(R.string.respiration_rate_unit),
            labelText = stringResource(R.string.respiration_rate_label),
        )
    }
}

@Composable
private fun HealthMetric(
    @DrawableRes iconResource: Int,
    iconDescription: String,
    valueText: String,
    unitText: String,
    labelText: String,
) {
    Row(
        modifier = Modifier.size(width = 208.dp, height = 104.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(iconResource),
            contentDescription = iconDescription,
            modifier = Modifier.size(72.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.width(128.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row {
                Text(
                    text = valueText,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 54.sp,
                    modifier = Modifier.alignByBaseline(),
                    maxLines = 1,
                    softWrap = false,
                )
                Text(
                    text = unitText,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.alignByBaseline(),
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Text(
                text = labelText,
                color = SecondaryTextColor,
                fontSize = 14.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun WeatherWidget(state: WidgetSlotUiState.Weather) {
    Row(
        modifier = Modifier.size(width = 380.dp, height = 160.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.icon?.let { icon ->
            Image(
                painter = painterResource(icon.drawableResource()),
                contentDescription = stringResource(R.string.weather_icon),
                modifier = Modifier.size(width = 186.dp, height = 160.dp),
                contentScale = ContentScale.Fit,
            )
        } ?: Spacer(modifier = Modifier.size(width = 186.dp, height = 160.dp))
        Spacer(modifier = Modifier.width(24.dp))
        Column(
            modifier = Modifier.width(170.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = state.temperatureText,
                    color = Color.White,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    lineHeight = 64.sp,
                )
                Text(
                    text = state.temperatureUnitText,
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                )
            }
            Text(
                text = state.locationText,
                color = SecondaryTextColor,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun DateTimeWidget(state: WidgetSlotUiState.DateTime) {
    Row(
        modifier = Modifier.size(width = 310.dp, height = 110.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnalogClock(state)
        Spacer(modifier = Modifier.width(30.dp))
        Column(
            modifier = Modifier.size(width = 170.dp, height = 107.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.size(width = 170.dp, height = 82.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.timeText,
                    color = Color.White,
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                ) {
                    if (state.dayPeriodText.isNotEmpty()) {
                        Text(
                            text = state.dayPeriodText,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = state.timeZoneText,
                        color = SecondaryTextColor,
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }
            }
            Box(
                modifier = Modifier.size(width = 170.dp, height = 25.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = state.dateText,
                    color = SecondaryTextColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

@Composable
private fun AnalogClock(state: WidgetSlotUiState.DateTime) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .background(color = ClockFaceColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        ClockHand(
            drawableResource = R.drawable.img_cluster_widget_clock_hour_hand_n,
            rotationDegrees = state.hourRotationDegrees,
        )
        ClockHand(
            drawableResource = R.drawable.img_cluster_widget_clock_minute_hand_n,
            rotationDegrees = state.minuteRotationDegrees,
        )
        ClockHand(
            drawableResource = R.drawable.img_cluster_widget_clock_second_hand_n,
            rotationDegrees = state.secondRotationDegrees,
        )
    }
}

@Composable
private fun ClockHand(
    @DrawableRes drawableResource: Int,
    rotationDegrees: Float,
) {
    Image(
        painter = painterResource(drawableResource),
        contentDescription = null,
        modifier = Modifier
            .size(width = 22.dp, height = 110.dp)
            .rotate(rotationDegrees),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun TirePressureWidget() {
    Image(
        painter = painterResource(R.drawable.img_ptop_tire_pressure_n),
        contentDescription = stringResource(R.string.tire_pressure_demo),
        modifier = Modifier.size(width = 320.25.dp, height = 120.dp),
        contentScale = ContentScale.Fit,
    )
}

@Composable
fun LogoWidget() {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.img_ptop_logo_carux_n),
            contentDescription = stringResource(R.string.carux_logo),
            modifier = Modifier.size(width = 211.dp, height = 94.dp),
            contentScale = ContentScale.Fit,
        )
        Image(
            painter = painterResource(R.drawable.img_ptop_logo_pioneer_n),
            contentDescription = stringResource(R.string.pioneer_logo),
            modifier = Modifier.size(width = 211.dp, height = 94.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@DrawableRes
private fun WeatherIconAsset.drawableResource(): Int = when (this) {
    WeatherIconAsset.DAY_CLEAR_SKY ->
        R.drawable.img_widget_widget_weather_daylight_clear_sky_s
    WeatherIconAsset.DAY_FEW_CLOUDS ->
        R.drawable.img_widget_weather_daylight_few_cloud_s
    WeatherIconAsset.DAY_SCATTERED_CLOUDS ->
        R.drawable.img_widget_weather_daylight_scattered_clouds_s
    WeatherIconAsset.DAY_BROKEN_CLOUDS ->
        R.drawable.img_widget_weather_daylight_broken_clouds_s
    WeatherIconAsset.DAY_SHOWER_RAIN ->
        R.drawable.img_widget_weather_daylight_shower_rain_s
    WeatherIconAsset.DAY_RAIN ->
        R.drawable.img_widget_weather_daylight_rain_s
    WeatherIconAsset.DAY_LIGHT_RAIN ->
        R.drawable.img_widget_weather_daylight_sight_rain_s
    WeatherIconAsset.DAY_THUNDERSTORM ->
        R.drawable.img_widget_weather_daylight_thunderstorm_s
    WeatherIconAsset.DAY_SNOW ->
        R.drawable.img_widget_weather_daylight_snow_s
    WeatherIconAsset.DAY_MIST ->
        R.drawable.img_widget_weather_daylight_mist_s
    WeatherIconAsset.DAY_DRIZZLE ->
        R.drawable.img_widget_weather_daylight_drizzle_s
    WeatherIconAsset.NIGHT_CLEAR_SKY ->
        R.drawable.img_widget_widget_weather_night_clear_sky_s
    WeatherIconAsset.NIGHT_FEW_CLOUDS ->
        R.drawable.img_widget_weather_night_few_clouds_s
    WeatherIconAsset.NIGHT_BROKEN_CLOUDS ->
        R.drawable.img_widget_weather_night_broken_clouds_s
    WeatherIconAsset.NIGHT_SHOWER_RAIN ->
        R.drawable.img_widget_weather_night_shower_rain_s
    WeatherIconAsset.NIGHT_RAIN ->
        R.drawable.img_widget_weather_night_rain_s
    WeatherIconAsset.NIGHT_LIGHT_RAIN ->
        R.drawable.img_widget_weather_night_light_rain_s
    WeatherIconAsset.NIGHT_THUNDERSTORM ->
        R.drawable.img_widget_weather_night_thunderstorm_s
    WeatherIconAsset.NIGHT_MIST ->
        R.drawable.img_widget_weather_night_mist_s
    WeatherIconAsset.NIGHT_DRIZZLE ->
        R.drawable.img_widget_weather_night_drizzle_s
}
