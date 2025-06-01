package com.pdevjay.calendar_with_schedule.features.widget

import android.content.Context
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.di.ScheduleRepositoryEntryPoint
import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate

@AndroidEntryPoint
class SchedyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SchedyWidget()
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // 위젯이 처음 활성화될 때 강제로 업데이트 요청 (옵션)
    }

}

class SchedyWidget : GlanceAppWidget() {
    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(
            SMALL_SQUARE,
            HORIZONTAL_RECTANGLE,
            BIG_SQUARE
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ScheduleRepositoryEntryPoint::class.java
        )
        val repository: ScheduleRepository = entryPoint.getScheduleRepository()
        val scheduleMap = repository.scheduleMap.value
        val holidayMap = repository.holidayMap.value
        val totalScheduleMap = scheduleMap + holidayMap
        provideContent {
            WidgetContent(scheduleMap)
        }
    }

    @Composable
    private fun WidgetContent(scheduleMap: Map<LocalDate, List<RecurringData>>) {
        val size = LocalSize.current
        val today: LocalDate = LocalDate.now()
        val todayEvents: List<RecurringData> = scheduleMap[today] ?: emptyList()
        val nextEvents = getNextWeekSchedules(scheduleMap)

        Row(
            modifier = GlanceModifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.7f)),
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize().defaultWeight().padding(4.dp),
                verticalAlignment = Alignment.Vertical.Top,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text("Today")
                Spacer(modifier = GlanceModifier.height(4.dp))
                for (schedule in todayEvents) {
                    val darkerColor = getDarkerColor(schedule.color?.let { Color(it).copy(alpha = 0.7f) }
                        ?: MaterialTheme.colorScheme.primary)

                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(color = darkerColor)
                            .cornerRadius(8.dp)
                            .padding(2.dp),

                        ) {
                        Text(
                            maxLines = 1,
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = ColorProvider(Color.White)
                            ),
                            text = "${schedule.title}",

                            )
                    }
                    Spacer(modifier = GlanceModifier.height(2.dp))
                }
            }

            if (size.width >= HORIZONTAL_RECTANGLE.width) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(4.dp)
                        .defaultWeight(),
                    verticalAlignment = Alignment.Vertical.Top,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Text("Upcoming")
                    Spacer(modifier = GlanceModifier.height(4.dp))

                    for ((date, eventsOnDate) in nextEvents) {
                        if (eventsOnDate.isEmpty()) continue
                        for ((idx, schedule) in eventsOnDate.withIndex()) {
                            val darkerColor = getDarkerColor(schedule.color?.let { Color(it).copy(alpha = 0.7f) }
                                ?: MaterialTheme.colorScheme.primary)

                            Column(
                                modifier = GlanceModifier.fillMaxWidth(),
                            ) {
                                if (idx == 0) {
                                    Text(
                                        style = TextStyle(
                                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                        ),
                                        text = "${date}"
                                    )
                                }
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Box(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .background(color = darkerColor)
                                        .cornerRadius(8.dp)
                                        .padding(2.dp),

                                    ) {

                                    Text(
                                        style = TextStyle(
                                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                            color = ColorProvider(Color.White)
                                        ),
                                        text = "${schedule.title}"
                                    )
                                }
                            }
                            if (idx == eventsOnDate.size - 1) {
                                Spacer(modifier = GlanceModifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getNextWeekSchedules(
    scheduleMap: Map<LocalDate, List<RecurringData>>
): Map<LocalDate, List<RecurringData>> {
    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    val weekLater = today.plusDays(7)

    val result = mutableMapOf<LocalDate, List<RecurringData>>()
    var d = tomorrow
    while (!d.isAfter(weekLater)) {
        // scheduleMap[d]에 이미 “d 날짜에 걸쳐 있는 이벤트들”이 있다면 꺼내오고,
        // 없으면 빈 리스트로 처리
        result[d] = scheduleMap[d]
            ?.sortedBy { ev ->
                ev.start.date.atTime(ev.start.time)
            }
            ?: emptyList()
        d = d.plusDays(1)
    }
    return result.toSortedMap() // 키(날짜)를 오름차순으로 정렬해 줌
}

fun getDarkerColor(color: Color): Color {
    return color.copy(
        red = (color.red * (1 - 0.3f)),
        green = (color.green * (1 - 0.3f)),
        blue = (color.blue * (1 - 0.3f)))
}

//@OptIn(ExperimentalGlancePreviewApi::class)
//@Preview(widthDp = 250, heightDp = 100)
//@Composable
//fun MyGlanceWidgetPreview() {
//    SchedyWidget()
//}

