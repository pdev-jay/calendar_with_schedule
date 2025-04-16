# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Gson generic type 보존
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }

# RecurringData, ScheduleData 관련 클래스 보존
-keep class com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData { *; }
-keep class com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData { *; }
-keep class com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule { *; }

# JSON 유틸 및 어댑터 보존
-keep class com.pdevjay.calendar_with_schedule.utils.JsonUtils { *; }
-keep class com.pdevjay.calendar_with_schedule.utils.ScheduleMapAdapter { *; }

# Kotlin metadata (선택적이지만 도움이 됨)
-keep class kotlin.Metadata { *; }

# enum 관련
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep enum com.pdevjay.calendar_with_schedule.screens.schedule.enums.AlarmOption { *; }
-keep enum com.pdevjay.calendar_with_schedule.screens.schedule.enums.RepeatType { *; }

# Gson이 enum과 필드를 리플렉션으로 접근할 수 있게
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep enum com.pdevjay.calendar_with_schedule.notification.AlarmAction { *; }
-keep enum com.pdevjay.calendar_with_schedule.notification.ScheduleType { *; }

# AlarmRegisterWorker와 AlarmRefreshWorker는 이름 그대로 유지하고 제거 금지
-keep class com.pdevjay.calendar_with_schedule.notification.AlarmRegisterWorker { *; }
-keep class com.pdevjay.calendar_with_schedule.notification.AlarmRefreshWorker { *; }

# 모든 Worker의 기본 구조 보존 (안전망용)
-keep class androidx.work.Worker { *; }
-keep class androidx.work.CoroutineWorker { *; }

# Hilt의 AssistedInject 관련 보존
-keep class dagger.assisted.AssistedInject { *; }
-keep class **_AssistedFactory { *; }

-keep class com.pdevjay.calendar_with_schedule.utils.ScheduleMapAdapter { *; }
