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
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
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
import com.pdevjay.calendar_with_schedule.MainActivity
import com.pdevjay.calendar_with_schedule.data.repository.ScheduleRepository
import com.pdevjay.calendar_with_schedule.di.ScheduleRepositoryEntryPoint
import com.pdevjay.calendar_with_schedule.features.calendar.data.HolidayData
import com.pdevjay.calendar_with_schedule.features.calendar.data.toBaseSchedule
import com.pdevjay.calendar_with_schedule.features.calendar.data.toRecurringData
import com.pdevjay.calendar_with_schedule.features.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.features.schedule.data.RecurringData
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

@AndroidEntryPoint
class SchedyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SchedyWidget()
}

class SchedyWidget : GlanceAppWidget() {

//    companion object {
//        private val SMALL_BOX = DpSize(90.dp, 90.dp)
//        private val BIG_BOX = DpSize(180.dp, 180.dp)
//        private val VERY_BIG_BOX = DpSize(300.dp, 300.dp)
//        private val ROW = DpSize(180.dp, 48.dp)
//        private val LARGE_ROW = DpSize(300.dp, 48.dp)
//        private val COLUMN = DpSize(48.dp, 180.dp)
//        private val LARGE_COLUMN = DpSize(48.dp, 300.dp)
//    }
//
//    override val sizeMode =
//        SizeMode.Responsive(setOf(SMALL_BOX, BIG_BOX, ROW, LARGE_ROW, COLUMN, LARGE_COLUMN))

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
        Log.e("widget", "provideGlance called")
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ScheduleRepositoryEntryPoint::class.java
        )
        val repository: ScheduleRepository = entryPoint.getScheduleRepository()
        val scheduleMap = repository.scheduleMap.first { it.isNotEmpty() }
        val holidayMap = repository.holidayMap.first { it.isNotEmpty() }

        provideContent {
            WidgetContent(scheduleMap, holidayMap)
        }
    }

    @Composable
    private fun WidgetContent(
        scheduleMap: Map<LocalDate, List<RecurringData>>,
        holidayMap: Map<LocalDate, List<HolidayData>>
    ) {
        val size = LocalSize.current
        val totalTodayEvents = getTodaySchedules(scheduleMap, holidayMap)
        val nextEvents = getNextWeekSchedules(scheduleMap, holidayMap)

        val widgetAction = actionStartActivity<MainActivity>()

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(widgetAction)
                    .background(Color(0xFFDCDCDC).copy(alpha = 0.7f)),
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .defaultWeight()
                        .padding(8.dp),
                    verticalAlignment = Alignment.Vertical.Top,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.Start
                    ) {
                        Text(
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            ),
                            text = "${LocalDate.now()}"
                            //                        text = "${LocalDate.now().dayOfMonth}"
                        )
                        Text(
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.labelMedium.fontSize,
                            ),
                            text = LocalDate.now().dayOfWeek.getDisplayName(
                                java.time.format.TextStyle.FULL,
                                Locale.getDefault()
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }

                    LazyColumn(
                    ) {
                        items(totalTodayEvents) { schedule ->
                            TodayEventGroup(schedule, widgetAction)
                        }
                    }

                    if (totalTodayEvents.isEmpty()) {
                        Column(
                            modifier = GlanceModifier.fillMaxWidth().clickable(widgetAction),
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                        ) {
                            Text("No events")
                        }
                    }
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .clickable(widgetAction)
                    ){}
                }

                if (size.width >= HORIZONTAL_RECTANGLE.width) {
                    Column(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .defaultWeight()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        LazyColumn(
                        ) {
                            items(nextEvents.toList()) { (date, eventsOnDate) ->
                                if (eventsOnDate.isNotEmpty()) {
                                    Column {
                                        for ((idx, schedule) in eventsOnDate.withIndex()) {
                                            DateEventGroup(
                                                schedule,
                                                idx,
                                                date,
                                                eventsOnDate,
                                                widgetAction
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = GlanceModifier.fillMaxSize().clickable(widgetAction)
                        ){}
                    }
                }
            }
    }

    @Composable
    private fun TodayEventGroup(schedule: BaseSchedule, widgetAction: Action) {

        val darkerColor = getDarkerColor(schedule.color?.let { Color(it).copy(alpha = 0.7f) }
            ?: MaterialTheme.colorScheme.primary)
        Column(
            modifier = GlanceModifier.clickable(widgetAction)
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(color = darkerColor)
                    .cornerRadius(8.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp),

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

    @Composable
    private fun DateEventGroup(
        schedule: RecurringData,
        idx: Int,
        date: LocalDate,
        eventsOnDate: List<RecurringData>,
        widgetAction: Action
    ) {
        val darkerColor = getDarkerColor(schedule.color?.let { Color(it).copy(alpha = 0.7f) }
            ?: MaterialTheme.colorScheme.primary)

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(widgetAction),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            if (idx == 0) {
                Text(
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    ),
                    text = "${date} (${
                        date.dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.SHORT,
                            Locale.getDefault()
                        )
                    })"
                )
            }
            Spacer(modifier = GlanceModifier.height(2.dp))
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(color = darkerColor)
                    .cornerRadius(8.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp),

                ) {

                Text(
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        color = ColorProvider(Color.White)
                    ),
                    maxLines = 1,
                    text = "${schedule.title}"
                )
            }
        }

        if (idx == eventsOnDate.size - 1) {
            Spacer(modifier = GlanceModifier.height(4.dp))
        }
    }

    @Composable
    fun ColumnHeader(label: String) {
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(label)
            Spacer(modifier = GlanceModifier.height(4.dp))
        }
    }

    private fun getTodaySchedules(
        scheduleMap: Map<LocalDate, List<RecurringData>>,
        holidayMap: Map<LocalDate, List<HolidayData>>
    ): List<BaseSchedule> {
        val today: LocalDate = LocalDate.now()
        val now: LocalDateTime = LocalDateTime.now()
        val todayEvents: List<RecurringData> = (scheduleMap[today] ?: emptyList())
            .filter { ev ->
                // 이벤트의 종료 시각
                val endDateTime = ev.end.date.atTime(ev.end.time)
                !endDateTime.isBefore(now)  // endDateTime >= now 인 것만 남김
            }
            .sortedBy { ev ->
                ev.start.date.atTime(ev.start.time)
            }
        val todayHolidays: List<BaseSchedule> =
            (holidayMap[today] ?: emptyList()).map { it.toBaseSchedule() }
        val totalTodayEvents = todayHolidays + todayEvents
        return totalTodayEvents
    }

    private fun getNextWeekSchedules(
        scheduleMap: Map<LocalDate, List<RecurringData>>,
        holidayMap: Map<LocalDate, List<HolidayData>>
    ): Map<LocalDate, List<RecurringData>> {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val weekLater = today.plusDays(7)

        val result = mutableMapOf<LocalDate, List<RecurringData>>()
        var d = tomorrow
        while (!d.isAfter(weekLater)) {
            // 1) 원래 스케줄 이벤트 리스트
            val eventsOnDate: List<RecurringData> = (scheduleMap[d] ?: emptyList()).sortedBy { ev ->
                ev.start.date.atTime(ev.start.time)
            }

            // 2) 해당 날짜의 HolidayData를 RecurringData로 변환 (HolidayData.toRecurringData() 사용)
            //    HolidayData.toRecurringData() 확장 함수는 아래 파일에서 확인할 수 있음
            val holidaysOnDate: List<RecurringData> = holidayMap[d]
                ?.map { it.toRecurringData() }
                ?: emptyList()

            // 3) 두 리스트를 합쳐서, 시작 시각(LocalDateTime) 기준으로 오름차순 정렬
            val combinedSorted: List<RecurringData> = holidaysOnDate + eventsOnDate

            result[d] = combinedSorted
            d = d.plusDays(1)
        }
        // 4) 키(날짜)를 오름차순 정렬하여 최종 맵 반환
        return result.toSortedMap()
    }

    private fun getDarkerColor(color: Color): Color {
        return color.copy(
            red = (color.red * (1 - 0.3f)),
            green = (color.green * (1 - 0.3f)),
            blue = (color.blue * (1 - 0.3f))
        )
    }
}


//@OptIn(ExperimentalGlancePreviewApi::class)
//@Preview(widthDp = 250, heightDp = 100)
//@Composable
//fun MyGlanceWidgetPreview() {
//    SchedyWidget()
//}

