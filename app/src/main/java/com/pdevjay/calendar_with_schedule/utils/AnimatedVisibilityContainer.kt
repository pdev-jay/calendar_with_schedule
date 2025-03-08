package com.pdevjay.calendar_with_schedule.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AnimatedVisibilityContainer(
    isVisible: Boolean,
    enter: EnterTransition = fadeIn() + slideInVertically { it / 2 },
    exit: ExitTransition = fadeOut() + slideOutVertically { it / 2 },
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = enter,
        exit = exit
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
