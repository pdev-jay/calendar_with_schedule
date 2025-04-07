package com.pdevjay.calendar_with_schedule.screens.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pdevjay.calendar_with_schedule.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmBottomSheet(
    title: String,
    description: String,
    single: String,
    future: String,
    isSingleAvailable: Boolean? = true,
    isFutureAvailable: Boolean? = true,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSingle: () -> Unit,
    onFuture: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (isVisible) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    enabled = isSingleAvailable ?: true,
                    onClick = {
                        scope.launch {
                            try {
                                bottomSheetState.hide()
                                onSingle()
                                onDismiss()
                            } catch (e: Exception) {
                                e.printStackTrace() // 예외 로깅
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(single)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    enabled = isFutureAvailable ?: true,
                    onClick = {
                        scope.launch {
                            try {
                                bottomSheetState.hide()
                                onFuture()
                                onDismiss()
                            } catch (e: Exception) {
                                e.printStackTrace() // 예외 로깅
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(future)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                bottomSheetState.hide()
                                onDismiss()
                            } catch (e: Exception) {
                                e.printStackTrace() // 예외 로깅
                            }
                        }
                              },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmBottomSheet(
    title: String,
    description: String,
    single: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSingle: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (isVisible) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                bottomSheetState.hide()
                                onSingle()
                                onDismiss()
                            } catch (e: Exception) {
                                e.printStackTrace() // 예외 로깅
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(single)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                bottomSheetState.hide()
                                onDismiss()
                            } catch (e: Exception) {
                                e.printStackTrace() // 예외 로깅
                            }
                        }
                              },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        }
    }
}
