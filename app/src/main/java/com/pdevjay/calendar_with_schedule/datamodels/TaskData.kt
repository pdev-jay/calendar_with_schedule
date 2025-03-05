package com.pdevjay.calendar_with_schedule.datamodels

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class DateTimePeriod(
    val date: LocalDate,
    val time: LocalTime
)

// 단일 이벤트(일정)를 나타내는 데이터 클래스
data class ScheduleData(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val location: String? = null,
    val start: DateTimePeriod,
    val end: DateTimePeriod
)

/** 레인 배정 결과: 어떤 이벤트가 몇 번 레인에 들어가는지 */
data class LaneData(
    val event: ScheduleData,
    val laneIndex: Int
)

val dummyCalendarEvents = listOf(
    ScheduleData(
        title = "Overnight Meeting",
        location = "Conference Room",
        start = DateTimePeriod(
            date = LocalDate.of(2025, 3, 1),
            time = LocalTime.of(22, 0)
        ),
        end = DateTimePeriod(
            date = LocalDate.of(2025, 3, 2),
            time = LocalTime.of(2, 0)
        )
    ),
    ScheduleData(
        title = "Team Standup",
        location = "Zoom",
        start = DateTimePeriod(
            date = LocalDate.of(2025, 3, 1),
            time = LocalTime.of(9, 0)
        ),
        end = DateTimePeriod(
            date = LocalDate.of(2025, 3, 1),
            time = LocalTime.of(9, 30)
        )
    ),
    ScheduleData(
        title = "Project Discussion",
        location = "Office",
        start = DateTimePeriod(
            date = LocalDate.of(2025, 3, 1),
            time = LocalTime.of(14, 0)
        ),
        end = DateTimePeriod(
            date = LocalDate.of(2025, 3, 1),
            time = LocalTime.of(15, 30)
        )
    ),
    ScheduleData(
        title = "Workshop",
        location = "Training Room",
        start = DateTimePeriod(
            date = LocalDate.of(2025, 3, 1),
            time = LocalTime.of(16, 0)
        ),
        end = DateTimePeriod(
            date = LocalDate.of(2025, 3, 3),
            time = LocalTime.of(10, 0)
        )
    )
)
