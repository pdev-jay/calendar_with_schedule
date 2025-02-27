package com.pdevjay.calendar_with_schedule.datamodels

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate

// 더미 Task 데이터 모델
data class Task(val id: Int, val title: String)

// 선택된 날짜에 대한 더미 투두리스트 생성 함수
fun getDummyTasksForDate(date: LocalDate): List<Task> {
    return listOf(
        Task(1, "Task 1 for $date"),
        Task(2, "Task 2 for $date"),
        Task(3, "Task 3 for $date")
    )
}

@Composable
fun DummyToDoListView(tasks: List<Task>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(tasks.size) { index ->
            Text(
                text = tasks[index].title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
