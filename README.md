# 📆 Schedy - Android Compose 일정 캘린더

Schedy는 Jetpack Compose 기반으로 제작된 직관적이고 유연한 **일정 캘린더 앱**입니다.  
단일 일정과 반복 일정을 함께 관리하며, 깔끔한 UI와 알림 기능, 커스터마이징 가능한 색상 테마를 제공합니다.

## ✨ 주요 기능

- 📅 **월간 캘린더 UI**
  - 매월 첫날 라벨 표시
  - 연속된 일정은 한 줄로 연결 표시
  - 일정이 많은 날짜는 `+N more`로 축약
- 🔁 **반복 일정 기능**
  - 매일, 매주, 매월 등 다양한 반복 옵션
  - "이번 일정만", "이후 모두", "내용만 변경" 등의 수정 모드
- 🔔 **알림 설정**
  - 알림 옵션에 따라 지정 시간 전에 푸시 알림
  - 기기 재부팅 시 알림 자동 복구
- 🎨 **일정 색상 선택**
  - 일정당 커스텀 색상 지정 가능
- 📩 **문의 및 피드백**
  - 이메일 전송 기능 내장 (앱/OS 정보 자동 포함)

## 📱 스크린샷

<!-- 이미지 추가 예정 -->
<!-- ![캘린더 화면](screenshots/calendar.png) -->

## 🔧 사용 기술

| 분류 | 기술 |
|------|------|
| UI | Jetpack Compose, Material 3 |
| 아키텍처 | MVVM, Hilt DI |
| 데이터베이스 | Room |
| 비동기 처리 | Kotlin Coroutines, StateFlow |
| 알림 | WorkManager, AlarmManager |
| 기타 | Gson, SharedPreferences, Custom LocalDate/LocalTime Adapter |

## 🔐 권한

| 권한 | 설명 |
|------|------|
| `POST_NOTIFICATIONS` | 알림 표시 (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | 기기 재부팅 후 알람 복구 |

## 📬 문의

버그 제보나 기능 제안은 [pdev.jay@gmail.com](mailto:pdev.jay@gmail.com) 으로 보내주세요.

---

> Made by Jay using Jetpack Compose
