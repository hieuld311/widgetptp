package com.ivi.widgetptp.presentation.host

import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ivi.widgetptp.R
import com.ivi.widgetptp.presentation.widgets.WidgetSlot

private const val TAG = "WidgetHostScreen"

@Composable
fun WidgetHostScreen(
    uiState: WidgetHostUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        BackgroundVideo(modifier = Modifier.fillMaxSize())

        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .width(1_701.dp)
                        .height(160.dp),
                ) {
                    WidgetSlot(
                        state = uiState.first,
                        modifier = Modifier.size(width = 567.dp, height = 160.dp),
                    )
                    WidgetSlot(
                        state = uiState.second,
                        modifier = Modifier.size(width = 567.dp, height = 160.dp),
                    )
                    WidgetSlot(
                        state = uiState.third,
                        modifier = Modifier.size(width = 567.dp, height = 160.dp),
                    )
                }
            }
        }
    }
}

/** Looping, muted background video - drop the file at res/raw/phud_dashboard_3840x208.mp4. */
@Composable
private fun BackgroundVideo(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoView = remember {
        VideoView(context).apply {
            setVideoURI(
                Uri.parse("android.resource://${context.packageName}/${R.raw.phud_dashboard_3840x208}")
            )
            setOnPreparedListener { player ->
                player.isLooping = true
                player.setVolume(0f, 0f)
            }
            setOnErrorListener { _, what, extra ->
                Log.w(TAG, "Background video playback error: what=$what, extra=$extra")
                true
            }
        }
    }

    DisposableEffect(lifecycleOwner, videoView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (!videoView.isPlaying) videoView.start()
                Lifecycle.Event.ON_PAUSE -> videoView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(factory = { videoView }, modifier = modifier)
}
