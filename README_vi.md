# Android Base Kotlin

[English](README.md) | Tiếng Việt

Một bộ khởi tạo (starter) Android hiện đại, ưu tiên Kotlin, theo kiến trúc đa module, đi kèm nhiều
widget/UI và tiện ích dùng chung cùng một ứng dụng demo. Dự án nhắm Android 14, sử dụng toolchain
SDK 36 và Java/Kotlin 17, tích hợp sẵn Navigation, Retrofit/OkHttp, Coil/Glide, Firebase (
Crashlytics/Analytics/Remote Config), Google Mobile Ads, Location, v.v.

## Tổng quan nhanh

- Công cụ: AGP 8.10.1, Kotlin 2.1.21, JDK 17
- Android: compileSdk 36, targetSdk 36, minSdk 26
- Modules:
    - app: Ứng dụng demo ghép nối toàn bộ tính năng
    - skeleton: Hiển thị trạng thái khung xương/shimmer cho view và danh sách
    - sliderview: Các thành phần slider tuỳ biến
    - stickerview: Sticker/overlay widgets
    - simplecropview: UI cắt ảnh đơn giản
    - cameraview: Tiện ích/thành phần camera
    - weekviewevent: Lịch tuần (1–7 ngày), chuyển sang Kotlin; có ví dụ minh hoạ
    - jinwidget: Widget/tiện ích dùng chung giữa các module

## Mục lục

- Yêu cầu
- Cấu trúc dự án
- Bắt đầu
- Build & chạy
- Cấu hình (Firebase, Ads)
- Thư viện đi kèm
- Tài liệu module & ví dụ
- Khắc phục sự cố
- Giấy phép
- Tác giả

## Yêu cầu

- Android Studio (khuyến nghị bản ổn định mới nhất)
- JDK 17 (Gradle dùng Java 17)
- Bộ Android SDK tới API 36

## Cấu trúc dự án

- settings.gradle.kts khai báo các module: app, skeleton, sliderview, stickerview, simplecropview,
  cameraview, weekviewevent, jinwidget
- Phiên bản phụ thuộc quản lý tập trung trong gradle/libs.versions.toml

## Bắt đầu

1) Clone repo và mở bằng Android Studio.
2) Chờ Gradle sync; dự án dùng Version Catalogs và Kotlin DSL.
3) Chạy cấu hình app trên thiết bị/giả lập (Android 8.0+, API 26+).

Tái sử dụng các module trong dự án của bạn bằng cách thêm phụ thuộc Gradle (ví dụ trong
app/build.gradle.kts):

```kotlin
implementation(project(":jinwidget"))
implementation(project(":skeleton"))
implementation(project(":sliderview"))
implementation(project(":stickerview"))
implementation(project(":simplecropview"))
implementation(project(":cameraview"))
implementation(project(":weekviewevent"))
```

## Build & chạy

- compileSdk = 36, targetSdk = 36, minSdk = 26
- Toolchain Java/Kotlin: 17
- ViewBinding bật sẵn; BuildConfig sinh các trường chứa Ad ID

Lệnh Gradle (Windows):

```bash
./gradlew clean
./gradlew :app:assembleDebug
```

File APK tạo ra sẽ có tên kèm timestamp như Base_Project_Kotlin_MM.dd.yyyy_hh.mm.

## Cấu hình

Dự án đã tích hợp Firebase và Google Mobile Ads. Hãy thay test keys bằng khoá thật trước khi phát
hành.

### Firebase (Analytics, Crashlytics, Remote Config)

- Đặt google-services.json dưới thư mục app/.
- Dùng Firebase BoM; đã áp dụng Crashlytics Gradle plugin.
- Trên Firebase Console: bật Crashlytics, Analytics và Remote Config cho app của bạn.

### Google Mobile Ads (AdMob)

- Dùng BuildConfig và manifestPlaceholders để inject AdMob IDs.
- app/build.gradle.kts định nghĩa các placeholder như AD_MOD_APP_ID và unit ID (test/prod). Hãy thay
  bằng ID thật khi phát hành.

## Thư viện đi kèm (tiêu biểu)

- AndroidX: appcompat, core-ktx, material, constraintlayout, recyclerview, activity, lifecycle (
  viewmodel/livedata/runtime), navigation (fragment/ui/dynamic-features)
- Networking: retrofit 2 + gson converter, okhttp logging-interceptor
- Hình ảnh/UI: coil, glide, lottie, Facebook shimmer, Flexbox
- Google Play: review, review-ktx, play-services-location, play-services-ads
- Lưu trữ/Tiện ích: Tencent MMKV, Apache commons-lang3
- Firebase: analytics-ktx, crashlytics-ktx, config-ktx (qua Firebase BoM)

Phiên bản được quản lý trong gradle/libs.versions.toml để dễ nâng cấp.

## Tài liệu module & ví dụ

- skeleton: Xem skeleton/README.md để bắt đầu nhanh, cấu hình và ví dụ shimmer
- weekviewevent: Xem weekviewevent/README.md để biết API và ảnh minh hoạ
    - Ảnh mẫu: weekviewevent/images/dayview.png, 3dayview.png, weekview.png

## Khắc phục sự cố

- Đảm bảo Android Studio dùng JDK 17 (Gradle JVM).
- Nếu Firebase đồng bộ lỗi, tải lại google-services.json từ Firebase Console và đặt vào app/.
- Với quảng cáo, dùng test ID trong quá trình phát triển; ID thật cần tuân thủ chính sách và có thể
  cần xét duyệt.
- Clean và build lại nếu thay đổi Version Catalog (libs.versions.toml).

## Giấy phép

Hãy chỉ định giấy phép cho dự án (ví dụ MIT, Apache-2.0). Nếu chưa rõ, thêm file LICENSE ở thư mục
gốc và tham chiếu tại đây.

## Tác giả

- Base: doanvu2000
- Support: [hoicham](https://github.com/PNThanggg), [vandatgsts](https://github.com/vandatgsts)

