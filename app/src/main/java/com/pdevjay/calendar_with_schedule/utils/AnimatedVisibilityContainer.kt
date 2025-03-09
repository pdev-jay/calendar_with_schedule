package com.pdevjay.calendar_with_schedule.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

@Composable
fun SlideInHorizontallyContainer(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(200)),
        exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
@Composable
fun SlideInVerticallyContainerFromBottom(
    isVisible: Boolean,
    enter: EnterTransition = slideInVertically(initialOffsetY = { it }, animationSpec = tween(200)),
    exit: ExitTransition = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)),
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
@Composable
fun SlideInVerticallyContainerFromTop(
    isVisible: Boolean,
    enter: EnterTransition = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(200)),
    exit: ExitTransition = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(200)),
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

@Composable
fun ExpandVerticallyContainerFromTop(
    isVisible: Boolean,
    enter: EnterTransition = expandVertically(initialHeight = { -it }),
    exit: ExitTransition = shrinkVertically(targetHeight = { -it }),
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
@Composable
fun ExpandVerticallyContainerFromBottom(
    isVisible: Boolean,
    enter: EnterTransition = expandVertically(),
    exit: ExitTransition = shrinkVertically(),
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

@Composable
fun ScaleInContainer(
    isVisible: Boolean,
    content: @Composable () -> Unit
){
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(animationSpec = tween(200)),
        exit = fadeOut() + scaleOut(animationSpec = tween(200))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}