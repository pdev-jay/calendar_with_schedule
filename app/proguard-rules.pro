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
-keepattributes *Annotation*

-keep class com.google.gson.reflect.TypeToken { *; }

# RecurringData, ScheduleData 관련 클래스 보존
-keep class com.pdevjay.calendar_with_schedule.screens.schedule.data.RecurringData { *; }
-keep class com.pdevjay.calendar_with_schedule.screens.schedule.data.ScheduleData { *; }
-keep class com.pdevjay.calendar_with_schedule.screens.schedule.data.BaseSchedule { *; }
-keep class com.pdevjay.calendar_with_schedule.screens.calendar.data.HolidayData { *; }
-keep class com.pdevjay.calendar_with_schedule.data.entity.HolidayDataEntity { *; }
-keep class com.pdevjay.calendar_with_schedule.screens.settings.data.VersionResponse { *; }
-keep interface com.pdevjay.calendar_with_schedule.data.remote.DataApiService { *; }
-keep class com.pdevjay.calendar_with_schedule.data.remote.RetrofitClient { *; }
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
-keep class com.pdevjay.calendar_with_schedule.utils.works.HolidaySyncWorker { *; }

# 모든 Worker의 기본 구조 보존 (안전망용)
-keep class androidx.work.Worker { *; }
-keep class androidx.work.CoroutineWorker { *; }

# Hilt의 AssistedInject 관련 보존
-keep class dagger.assisted.AssistedInject { *; }
-keep class **_AssistedFactory { *; }

-keep class com.pdevjay.calendar_with_schedule.utils.ScheduleMapAdapter { *; }


######## Retrofit ########
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response
######## Retrofit ########

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
