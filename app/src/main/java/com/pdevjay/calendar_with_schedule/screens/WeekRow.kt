package com.pdevjay.calendar_with_schedule.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.pdevjay.calendar_with_schedule.datamodels.CalendarWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekRow(
    modifier: Modifier = Modifier,
    isInTopBar: Boolean = false,
    week: CalendarWeek,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
    Row(
        modifier = modifier.fillMaxSize(),
    ) {
        for (day in week.days) {

            Column (modifier = Modifier.weight(1f).fillMaxHeight()){
                if (day.isFirstDayOfMonth && !isInTopBar){
                    Text(text = "${day.date.format(formatter)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (isInTopBar){
                } else {
                    Text(text = "", style = MaterialTheme.typography.titleLarge)
                }
                Box(modifier = Modifier.fillMaxWidth()){
                    DayCell(
                        modifier = Modifier.fillMaxSize(),
                        isInTopBar = isInTopBar,
                        calendarDay = day,
                        isSelected = selectedDate == day.date,
                        onDateSelected = onDateSelected
                    )
                }
            }
        }
    }
}

