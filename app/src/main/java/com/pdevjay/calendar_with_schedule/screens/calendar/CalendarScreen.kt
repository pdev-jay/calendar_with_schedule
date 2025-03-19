package com.pdevjay.calendar_with_schedule.screens.calendar

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarDay
import com.pdevjay.calendar_with_schedule.screens.calendar.data.CalendarMonth
import com.pdevjay.calendar_with_schedule.screens.calendar.intents.CalendarIntent
import com.pdevjay.calendar_with_schedule.screens.calendar.viewmodels.CalendarViewModel
import com.pdevjay.calendar_with_schedule.screens.schedule.ScheduleView
import com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule
import com.pdevjay.calendar_with_schedule.screens.schedule.intents.ScheduleIntent
import com.pdevjay.calendar_with_schedule.screens.schedule.viewmodels.ScheduleViewModel
import com.pdevjay.calendar_with_schedule.utils.ExpandVerticallyContainerFromTop
import com.pdevjay.calendar_with_schedule.utils.JsonUtils
import com.pdevjay.calendar_with_schedule.utils.LocalDateAdapter
import com.pdevjay.calendar_with_schedule.utils.LocalTimeAdapter
import com.pdevjay.calendar_with_schedule.utils.SlideInVerticallyContainerFromBottom
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.stream.Collectors.toList
import kotlin.math.abs

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    calendarViewModel: CalendarViewModel,
    scheduleViewModel: ScheduleViewModel,
) {
    val calendarState by calendarViewModel.state.collectAsState()

    val isLoading = remember { mutableStateOf(false) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = 6) // 현재 달을 기준으로 앞뒤로 12개월씩 로드

    val monthListState by calendarViewModel.months.collectAsState()
    val currentVisibleMonth by rememberCurrentVisibleMonth(listState, monthListState)

    // 중앙에 보이는 부분이 어느 달인지
    LaunchedEffect(currentVisibleMonth) {
        currentVisibleMonth?.let { month ->
        Log.e("selected", "currentVisibleMonth : ${currentVisibleMonth} ")
            calendarViewModel.processIntent(CalendarIntent.MonthChanged(month.yearMonth))
        }
    }

    LaunchedEffect(calendarState.selectedDate) {
        if (calendarState.selectedDate != null) {
            val selectedDate = calendarState.selectedDate ?: return@LaunchedEffect
            val currentMonthIndex = monthListState.indexOfFirst { month ->
                month.yearMonth == YearMonth.from(selectedDate)
            }

            if (currentMonthIndex != listState.firstVisibleItemIndex) { // ✅ 중복 실행 방지
//                listState.scrollToItem(currentMonthIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            CalendarTopBar(calendarViewModel, listState, navController)
        }
    ) { innerPadding ->

        BoxWithConstraints(
            modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SlideInVerticallyContainerFromBottom (
                isVisible = calendarState.selectedDate == null,
            ) {
                key(calendarState.selectedDate) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {

                            items(monthListState, key = { it.yearMonth.toString() }) { month ->
                                    MonthItem(
                                        month,
                                        calendarState.scheduleMap
                                    ) { date ->
                                        if (calendarState.selectedDate == null || calendarState.selectedDate != date.date) {
                                            calendarViewModel.processIntent(
                                                CalendarIntent.DateSelected(
                                                    date.date
                                                )
                                            )
                                        } else {
                                            calendarViewModel.processIntent(CalendarIntent.DateUnselected)
                                        }
                                    }
                            }

                            item {
                                if (isLoading.value) {
                                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                    }
                }
            }
            ExpandVerticallyContainerFromTop (
                isVisible = calendarState.selectedDate != null,
            ) {
                SchedulePager(
                    modifier = Modifier.height(maxHeight),
                    calendarViewModel = calendarViewModel,
                    scheduleViewModel = scheduleViewModel,
                    onEventClick = { event ->
                        val jsonSchedule = URLEncoder.encode(JsonUtils.gson.toJson(event), "UTF-8")
                        navController.navigate("scheduleDetail/${URLEncoder.encode(jsonSchedule, "UTF-8")}")
                    },
                    onBackButtonClicked = {
                        calendarViewModel.processIntent(CalendarIntent.DateUnselected)
                    }
                )
            }
        }
    }

    // 아래 끝 감지 → 미래 데이터 추가 로드
    LaunchedEffect(listState) {
        // ✅ 첫 번째 아이템 감지 → 이전 달 데이터 로드
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(100)
            .collectLatest { firstVisibleIndex ->
                Log.e("LazyRow", "🔼 현재 첫 번째 아이템 인덱스: $firstVisibleIndex") // ✅ 디버깅 로그 추가

                if (firstVisibleIndex <= 1 && !isLoading.value) {
                    loadPreviousMonths(monthListState, isLoading, listState, calendarViewModel)
                }
            }
    }

    LaunchedEffect(listState) {
        // ✅ 마지막 아이템 감지 → 다음 달 데이터 로드
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }
            .filterNotNull() // ✅ Null 방지
            .distinctUntilChanged()
            .debounce(100)
            .collectLatest { lastVisibleIndex ->
                if (lastVisibleIndex == monthListState.lastIndex && !isLoading.value) {
                    Log.e("LazyRow", "🔽 현재 마지막 아이템 인덱스: $lastVisibleIndex") // ✅ 디버깅 로그 추가
                    loadNextMonths(monthListState, isLoading, calendarViewModel)
                }
            }
    }



}

suspend fun loadNextMonths(
    monthList: MutableList<CalendarMonth>,
    isLoading: MutableState<Boolean>,
    viewModel: CalendarViewModel
) {
    if (isLoading.value) return

    isLoading.value = true
    viewModel.loadNextMonth()

    isLoading.value = false
}

suspend fun loadPreviousMonths(
    monthList: MutableList<CalendarMonth>,
    isLoading: MutableState<Boolean>,
    listState: LazyListState,
    viewModel: CalendarViewModel
) {
    if (isLoading.value) return

    isLoading.value = true
    val newMonths = viewModel.loadPreviousMonth() // 6개월 추가됨
    isLoading.value = false
}


//@Composable
//fun rememberCurrentVisibleMonth(
//    listState: LazyListState,
//    monthList: List<CalendarMonth>
//): State<CalendarMonth?> {
//    val visibleMonth = remember { mutableStateOf<CalendarMonth?>(null) }
//    var lastValidMiddleItem by rememberSaveable { mutableStateOf<Int?>(null) }
//
//    val updatedMonthList = rememberUpdatedState(monthList)
//
//    LaunchedEffect(listState, updatedMonthList.value) {
//        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
//            .onStart {
//                awaitFrame() // ✅ UI 업데이트를 보장하기 위해 다음 프레임까지 대기
//            }
//            .collectLatest { visibleItems ->
//                if (visibleItems.isEmpty()) {
//                    lastValidMiddleItem?.let {
//                        visibleMonth.value = updatedMonthList.value.getOrNull(it)
//                    }
//                    return@collectLatest
//                }
//
//                val screenCenter = listState.layoutInfo.viewportSize.height / 2
//                val middleItem = visibleItems.minByOrNull { item ->
//                    val itemCenter = item.offset + (item.size / 2)
//                    abs(itemCenter - screenCenter)
//                }?.index ?: listState.firstVisibleItemIndex
//
//                if (middleItem != lastValidMiddleItem) {
//                    lastValidMiddleItem = middleItem
//                    visibleMonth.value = updatedMonthList.value.getOrNull(middleItem)
//                    Log.e("LazyRow", "🆕 보여지는 month 변경 : updated currentVisibleMonth: $visibleMonth")
//                }
//            }
//    }
//
//    return visibleMonth
//}

@Composable
fun rememberCurrentVisibleMonth(
    listState: LazyListState,
    monthList: List<CalendarMonth>
): State<CalendarMonth?> {
    val visibleMonth = remember { mutableStateOf<CalendarMonth?>(null) }
    var lastValidMiddleItem by rememberSaveable { mutableStateOf<Int?>(null) } // recomposition에서도 유지
    val updatedMonthList = rememberUpdatedState(monthList)

    LaunchedEffect(listState, updatedMonthList.value) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .combine(snapshotFlow { listState.layoutInfo.visibleItemsInfo }) { viewportHeight, visibleItems ->

                // 🛑 viewportHeight == 0 또는 visibleItems가 없으면 lastValidMiddleItem을 변경하지 않음
                if (viewportHeight == 0 || visibleItems.isEmpty()) {
                    return@combine lastValidMiddleItem ?: listState.firstVisibleItemIndex
                }

                val screenCenter = viewportHeight / 2
                val middleItem = visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + (item.size / 2)
                    abs(itemCenter - screenCenter)
                }?.index ?: listState.firstVisibleItemIndex

                // middleItem이 실제로 달라진 경우에만 업데이트
                if (middleItem != lastValidMiddleItem) {
                    lastValidMiddleItem = middleItem
                }

                middleItem
            }
            .distinctUntilChanged()
            .collectLatest { index ->
                visibleMonth.value = monthList.getOrNull(index)
            }
    }

    return visibleMonth
}

@Composable
fun SchedulePager(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel,
    scheduleViewModel: ScheduleViewModel,
    onEventClick: (BaseSchedule) -> Unit,
    onBackButtonClicked: () -> Unit
) {
    val calendarState by calendarViewModel.state.collectAsState() // ViewModel의 State 구독

    // ✅ `PagerState`를 remember로 유지 (이제 `initialPage` 제거)
    val index =
        calendarState.scheduleMap.toList().indexOfFirst { it.first == calendarState.selectedDate }
    val pagerState =
        rememberPagerState(initialPage = index, pageCount = { calendarState.scheduleMap.size })

    LaunchedEffect(calendarState.selectedDate){
        snapshotFlow { calendarState.selectedDate }
            .distinctUntilChanged()
            .collect{
                val page = calendarState.scheduleMap.toList().indexOfFirst { it.first == calendarState.selectedDate }
                Log.e("","CalendarIntent.DateSelected1 : page -> $page")
                Log.e("","CalendarIntent.DateSelected1 : calendarState.selectedDate -> ${calendarState.selectedDate}")

                if (calendarState.selectedDate != null && pagerState.currentPage != page) {
                    pagerState.animateScrollToPage(page)
                }
            }
    }
    LaunchedEffect(pagerState){
        snapshotFlow{pagerState.targetPage}
            .distinctUntilChanged()
            .debounce(200)
//            .filter { targetPage ->
//                abs(targetPage - pagerState.currentPage) <= 1 // 1페이지 이상 변화할 때만 반영
//            }
            .collectLatest { page ->
                val page1 = calendarState.scheduleMap.toList().indexOfFirst { it.first == calendarState.selectedDate }

                Log.e("","CalendarIntent.DateSelected : page -> $page")
                Log.e("","CalendarIntent.DateSelected : page1 -> $page1")
                Log.e("","CalendarIntent.DateSelected : page2 -> ${calendarState.selectedDate}")
                Log.e("","CalendarIntent.DateSelected : ${calendarState.scheduleMap.toList()[page].first}")
                calendarViewModel.processIntent(CalendarIntent.DateSelected(calendarState.scheduleMap.toList()[page].first))

                if (pagerState.currentPage == 0){
                    calendarViewModel.loadPreviousMonth()
                } else if(pagerState.currentPage == pagerState.pageCount - 1) {
                    calendarViewModel.loadNextMonth()
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
//            flingBehavior = PagerDefaults.flingBehavior(
//                state = pagerState,
//                snapPositionalThreshold =  0.3f,
//                decayAnimationSpec = exponentialDecay(0.2f)
//            ),
            key = { index -> calendarState.scheduleMap.toList()[index].first.toString() }, // 🔥 페이지 고유 키 설정
        ) { page ->
            ScheduleView(
                modifier = modifier,
                selectedDay = calendarState.scheduleMap.toList()[page].first,
                scheduleViewModel = scheduleViewModel,
                schedules = calendarState.scheduleMap.toList()[page].second, // scheduleList가 갱신되면서 깜빡이는 것처럼 보이는 현상때문에 처음, 마지막 page에서 recomposition 되기 전에는 빈 리스트로 설정
                onEventClick = onEventClick,
                onBackButtonClicked = onBackButtonClicked
            )
        }
    }
}
//}@Composable
//fun SchedulePager(
//    modifier: Modifier = Modifier,
//    calendarViewModel: CalendarViewModel,
//    scheduleViewModel: ScheduleViewModel,
//    onEventClick: (BaseSchedule) -> Unit,
//    onBackButtonClicked: () -> Unit
//) {
//    val calendarState by calendarViewModel.state.collectAsState() // ViewModel의 State 구독
//    val coroutineScope = rememberCoroutineScope()
//
//    // scheduleList가 변경될 때 `selectedDate`를 유지하도록 설정
//    // ✅ `scheduleList` 최신 상태 유지
//    // ✅ 항상 최신 상태 유지
//    val scheduleList by remember(calendarState) {
//        derivedStateOf {
//            val sortedList = calendarState.scheduleMap.toList().sortedBy { it.first }
//            Log.e("SchedulePager", "🔄 scheduleList 변경됨: $sortedList")
//            sortedList
//        }
//    }
//
//    val selectedIndex by remember(scheduleList, calendarState.selectedDate) {
//        derivedStateOf {
//            val index = scheduleList.indexOfFirst { it.first == calendarState.selectedDate }
//                .takeIf { it >= 0 } ?: 0
//            Log.e("SchedulePager", "📌 selectedIndex 변경됨: $index, selectedDate: ${calendarState.selectedDate}")
//            index
//        }
//    }
//
//    // ✅ `PagerState`를 remember로 유지 (이제 `initialPage` 제거)
//    val pagerState = remember {
//        PagerState(pageCount = { scheduleList.size })
//    }
//
//    // ✅ `scheduleList`와 `selectedIndex`를 최신 상태로 유지 (PagerState 재생성 X)
//    val currentScheduleList by rememberUpdatedState(scheduleList)
//    val currentSelectedIndex by rememberUpdatedState(selectedIndex)
//
//    // ✅ `scheduleList`가 변경될 때 현재 페이지 유지 (PagerState 유지)
//    LaunchedEffect(currentScheduleList, currentSelectedIndex) {
//        Log.e("SchedulePager", "🚀 LaunchedEffect 실행됨: newIndex=$currentSelectedIndex, currentPage=${pagerState.currentPage}")
//
//        if (currentSelectedIndex != pagerState.currentPage) {
//            pagerState.scrollToPage(currentSelectedIndex)
//        }
//    }
//
//    // ✅ 사용자가 스크롤하면 `selectedDate` 및 `MonthChanged` 업데이트
//    LaunchedEffect(pagerState) {
//        snapshotFlow { pagerState.settledPage }
//            .distinctUntilChanged()
//            .collectLatest { page ->
//                val newDate = currentScheduleList.getOrNull(page)?.first ?: calendarState.selectedDate
//
//                Log.e("SchedulePager", "📌 PagerState 페이지 변경됨: page=$page, newDate=$newDate")
//
//                // ✅ 날짜 변경 감지
//                if (calendarState.selectedDate != newDate) {
//                    Log.e("CalendarIntent", "📆 DateSelected 변경됨: ${calendarState.selectedDate} → $newDate")
////                    calendarViewModel.processIntent(CalendarIntent.DateSelected(newDate ?: LocalDate.now()))
//                }
//
//                // ✅ 첫 페이지 또는 마지막 페이지일 때 `MonthChanged` 호출
//                if (page == currentScheduleList.size - 1 || page == 0) {
//                    val newMonth = YearMonth.from(newDate)
//                    Log.e("CalendarIntent", "📅 MonthChanged 호출됨: $newMonth")
////                    calendarViewModel.processIntent(CalendarIntent.MonthChanged(newMonth))
//                }
//            }
//    }
//
//
//    Box(modifier = modifier.fillMaxSize()) {
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier.fillMaxSize(),
//        ) { page ->
//            ScheduleView(
//                modifier = modifier,
//                selectedDay = scheduleList[page].first,
//                scheduleViewModel = scheduleViewModel,
//                schedules = if (page == scheduleList.size -1 || page == 0) emptyList() else scheduleList[page].second, // scheduleList가 갱신되면서 깜빡이는 것처럼 보이는 현상때문에 처음, 마지막 page에서 recomposition 되기 전에는 빈 리스트로 설정
//                onEventClick = onEventClick,
//                onBackButtonClicked = onBackButtonClicked
//            )
//        }
//    }
//}