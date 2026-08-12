package com.ivi.widgetptp.presentation.host

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ivi.widgetptp.presentation.widgets.WidgetSlot

@Composable
fun WidgetHostScreen(
    uiState: WidgetHostUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
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
