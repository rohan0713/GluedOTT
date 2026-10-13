# GluedOTT - Android OTT Application

An OTT streaming application built with **Kotlin**, **XML-based UI**, **MVVM**, **Clean Architecture**, and **Android Media3 (ExoPlayer)**.

---

## 1. Steps to Build and Run the Application

### Prerequisites
* **Android Studio**: Ladybug / Meerkat (or newer)
* **JDK Version**: Java 17 LTS or Java 21
* **Android SDK**: Compile SDK `37`, Target SDK `37`, Minimum SDK `24` (Android 7.0+)
* **Gradle**: Gradle 9.6.0 (bundled via Gradle Wrapper)

### Build Commands (Terminal / PowerShell)

```bash
# Clean the project
./gradlew clean

# Build the Debug APK
./gradlew assembleDebug

# Build the Obfuscated & Shrunk Release APK
./gradlew assembleRelease

# Run Unit Tests
./gradlew testDebugUnitTest
```

*(On Windows PowerShell or Command Prompt, use `.\gradlew.bat`)*

### Installing & Running on Device or Emulator

```bash
# Install on connected device/emulator
./gradlew installDebug
```

* **Debug APK Path**:
  `app/build/outputs/apk/debug/app-debug.apk`
* **Release APK Path (Obfuscated & Shrunk)**:
  `app/build/outputs/apk/release/app-release.apk`
* **ProGuard / R8 Mapping File**:
  `app/build/outputs/mapping/release/mapping.txt`

---

## 2. Architecture Overview

The application strictly adheres to **Clean Architecture** and **MVVM (Model-View-ViewModel)** with **Unidirectional Data Flow (UDF)**. This guarantees clear separation of concerns, high testability, and decoupled layers.

```
       ┌────────────────────────────────────────────────────────┐
       │                   Presentation Layer                   │
       │   • Activities & Fragments (XML + ViewBinding)         │
       │   • ViewModels (StateFlow / UDF)                       │
       │   • Adapters (ListAdapter + DiffUtil + Shared Pools)   │
       └───────────────────────────┬────────────────────────────┘
                                   │ User Actions / Observes UiState
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                      Domain Layer                      │
       │   • Use Cases (GetHomeFeed, GetDetail, TrackEvent)     │
       │   • Domain Entities (ContentItem, CategoryRail, etc.)  │
       │   • Repository Contracts (Interfaces)                  │
       └───────────────────────────┬────────────────────────────┘
                                   │ Depends on Repository Contract
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                       Data Layer                       │
       │   • Repository Implementations                         │
       │   • DTOs (Data Transfer Objects with Gson annotations) │
       │   • Mappers (ContentDtoMapper)                         │
       │   • Remote Data Source Abstraction                     │
       │       - MockJsonContentDataSource (Current)            │
       │       - RetrofitContentDataSource (Future)             │
       └────────────────────────────────────────────────────────┘
```

### Unidirectional Data Flow (UDF)
Every screen is governed by an immutable `UiState<T>` sealed interface:
* **`UiState.Loading`**: Emitted while data is being prepared or fetched.
* **`UiState.Success<T>`**: Emitted with immutable data upon successful retrieval.
* **`UiState.Empty`**: Emitted when the data source returns no items.
* **`UiState.Error`**: Emitted with error message and retry capability.

---

## 3. Project / Module Structure

The project is structured logically by layer and feature under `com.ott.gluedin`:

```
com.ott.gluedin/
├── domain/                                     // Pure Kotlin Business Logic (Zero Android dependencies)
│   ├── entity/
│   │   ├── ContentItem.kt                      // Core media item entity
│   │   ├── CategoryRail.kt                     // Category rail entity
│   │   ├── HomeFeed.kt                         // Aggregated home feed entity
│   │   └── PlayerAnalyticsEvent.kt             // Domain telemetry events
│   ├── repository/
│   │   ├── ContentRepository.kt                // Content data contract
│   │   └── AnalyticsRepository.kt              // Telemetry tracking contract
│   └── usecase/
│       ├── GetHomeFeedUseCase.kt               // Fetch home feed use case
│       ├── GetContentDetailUseCase.kt          // Fetch content item by ID
│       └── TrackPlayerEventUseCase.kt          // Process player analytics events
├── data/                                       // Data Access & Mapping Layer
│   ├── datasource/
│   │   ├── ContentRemoteDataSource.kt          // Remote data source abstraction
│   │   └── MockJsonContentDataSource.kt        // Raw JSON asset mock implementation
│   ├── dto/
│   │   ├── HomeFeedResponseDto.kt              // DTO for home API response
│   │   ├── CategoryRailDto.kt                  // DTO for rail payload
│   │   └── ContentItemDto.kt                   // DTO for media item
│   ├── mapper/
│   │   └── ContentDtoMapper.kt                 // Maps DTOs to Domain Entities
│   └── repository/
│       ├── ContentRepositoryImpl.kt            // Concrete ContentRepository
│       └── AnalyticsRepositoryImpl.kt          // Concrete AnalyticsRepository (Logcat & stream)
├── player/                                     // Media3 / ExoPlayer Engine
│   ├── PlayerManager.kt                        // Lifecycle-aware player manager
│   └── PlayerEventBridge.kt                    // Translates Player.Listener to Domain Events
└── presentation/                               // UI & ViewModels (Feature-packaged)
    ├── common/
    │   └── UiState.kt                          // Unified sealed interface for UI state
    ├── splash/
    │   └── SplashActivity.kt                   // Branded animated splash screen
    ├── main/
    │   └── MainActivity.kt                     // Container Activity managing navigation
    ├── home/
    │   ├── ui/
    │   │   └── HomeFragment.kt                 // Home Screen (Hero Banner + Category Rails)
    │   ├── viewmodel/
    │   │   └── HomeViewModel.kt                // Home Screen StateFlow ViewModel
    │   └── adapter/
    │       ├── CategoryRailAdapter.kt          // Vertical adapter for rails (Shared ViewPool)
    │       └── ContentCardAdapter.kt           // Horizontal adapter for content cards
    ├── detail/
    │   ├── ui/
    │   │   └── DetailFragment.kt               // Detail Screen (Backdrop, Metadata, Play CTA)
    │   └── viewmodel/
    │       └── DetailViewModel.kt              // Detail Screen ViewModel
    └── player/
        ├── PlayerActivity.kt                   // Fullscreen immersive video player
        └── PlayerViewModel.kt                  // Coordinates telemetry and UI state
```

---

## 4. Libraries Used

| Library | Version | Purpose |
| :--- | :--- | :--- |
| **AndroidX Core KTX** | `1.19.1` | Kotlin extensions for Android framework APIs |
| **AndroidX AppCompat** | `1.8.0` | Backward-compatible Android components |
| **Material Components** | `1.14.0` | Material 3 components and dark theme styling |
| **ConstraintLayout** | `2.2.2` | Flat, performant view hierarchies |
| **RecyclerView** | `1.4.0` | High-performance content rails with DiffUtil |
| **Lifecycle (ViewModel & KTX)** | `2.8.7` | MVVM lifecycle management and Coroutine scopes |
| **Fragment KTX** | `1.8.6` | Fragment transactions and `viewModels()` delegation |
| **Media3 ExoPlayer** | `1.5.1` | Core media playback engine |
| **Media3 ExoPlayer HLS** | `1.5.1` | Native HLS (`.m3u8`) adaptive stream support |
| **Media3 UI** | `1.5.1` | Standard PlayerView and playback controls |
| **Coil** | `2.7.0` | Lightweight Coroutine-native image loading |
| **Gson** | `2.11.0` | JSON serialization/deserialization for mock DTOs |
| **Kotlinx Coroutines** | `1.10.1` | Asynchronous programming and structured concurrency |

---

## 5. Key Technical & Architectural Decisions

1. **Clean Separation of Concerns & Dependency Inversion:**
   * The UI layer never interacts with the data layer or raw DTOs directly; it communicates exclusively via Domain Use Cases.
   * Repositories depend on abstractions (`ContentRemoteDataSource`), not concrete network implementations.

2. **RecyclerView Performance Optimizations:**
   * **`RecyclerView.RecycledViewPool` Sharing:** The parent `CategoryRailAdapter` shares a single `RecycledViewPool` among all horizontal rail adapters, minimizing view inflation churn during fast vertical scrolling.
   * **`ListAdapter` + `DiffUtil`:** Computes item differences on background threads to ensure 60fps scrolling.
   * **Prefetching:** Configured `initialPrefetchItemCount = 4` on horizontal rail layout managers.

3. **Lifecycle Safety & Memory Leak Prevention:**
   * `StateFlow` collection uses `repeatOnLifecycle(Lifecycle.State.STARTED)`, suspending collectors when the app moves to the background.
   * ViewBinding references in Fragments are set to `null` in `onDestroyView()` to prevent memory leaks.
   * `ExoPlayer` is managed via `PlayerManager` using `context.applicationContext` to avoid Activity leaks, releasing on `onStop` (API $\ge$ 24) or `onPause` (API < 24).

4. **Playback State Retention:**
   * Playback position and `playWhenReady` flags are preserved across screen rotations and process death via `onSaveInstanceState` and restored seamlessly.

5. **Type-Safe Fragment Navigation:**
   * `DetailFragment.newInstance(contentId)` uses the static factory method pattern to encapsulate argument bundling and guarantee argument preservation across OS fragment recreation.

---

## 6. Player Event Handling Approach

Player events are **not** logged directly inside UI classes. Instead, they flow through a decoupled, unidirectional telemetry pipeline:

```
[Media3 ExoPlayer Engine]
        │
        ▼ (ExoPlayer.Listener callbacks)
[PlayerEventBridge]
        │
        ▼ (Maps to Domain PlayerAnalyticsEvent)
[PlayerViewModel]
        │
        ▼
[TrackPlayerEventUseCase]
        │
        ▼
[AnalyticsRepositoryImpl]
        │
        ├─► Android Logcat: Log.i("OTT_ANALYTICS", "...")
        └─► SharedFlow Stream (for UI badge display / testing)
```

### Handled Telemetry Events:
* **`PLAYER_READY`**: Triggered when ExoPlayer enters `STATE_READY` with total media duration.
* **`PLAYER_STARTED`**: Triggered when playback starts or resumes with current position.
* **`PLAYER_PAUSED`**: Triggered when playback is paused with current position.
* **`PLAYER_SEEKED`**: Triggered when a user seeks, capturing `fromPositionMs` and `toPositionMs`.
* **`BUFFERING_STARTED`**: Triggered when playback enters `STATE_BUFFERING`.
* **`BUFFERING_ENDED`**: Triggered when buffering completes and playback is ready.
* **`PLAYER_COMPLETED`**: Triggered when video reaches `STATE_ENDED`.
* **`PLAYER_ERROR`**: Triggered on `PlaybackException`, capturing error code name and message.

All events are logged under the Logcat tag **`OTT_ANALYTICS`**:
```
I/OTT_ANALYTICS: Telemetry Event Logged: PLAYER_READY [Duration: 734000ms]
I/OTT_ANALYTICS: Telemetry Event Logged: PLAYER_STARTED [Position: 0ms]
I/OTT_ANALYTICS: Telemetry Event Logged: PLAYER_SEEKED [From: 12400ms -> To: 45000ms]
I/OTT_ANALYTICS: Telemetry Event Logged: PLAYER_PAUSED [Position: 45200ms]
I/OTT_ANALYTICS: Telemetry Event Logged: PLAYER_COMPLETED [Total Duration: 734000ms]
```
A live telemetry indicator is also displayed on the player interface for real-time visual verification.

---

## 7. How the Mock Data Source Would Be Replaced With a Real API

The application is completely **API-Ready**. To switch from mock JSON to a production backend (e.g., Retrofit / Ktor):

### Step 1: Define the Retrofit API Interface
```kotlin
interface OttApiService {
    @GET("v1/home/feed")
    suspend fun getHomeFeed(): HomeFeedResponseDto

    @GET("v1/content/{id}")
    suspend fun getContentDetail(@Path("id") contentId: String): ContentItemDto
}
```

### Step 2: Implement `ContentRemoteDataSource`
```kotlin
class RetrofitContentDataSource(
    private val apiService: OttApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContentRemoteDataSource {

    override suspend fun getHomeFeed(): HomeFeedResponseDto = withContext(ioDispatcher) {
        apiService.getHomeFeed()
    }

    override suspend fun getContentDetail(contentId: String): ContentItemDto = withContext(ioDispatcher) {
        apiService.getContentDetail(contentId)
    }
}
```

### Step 3: Swap the Dependency
```kotlin
// In your DI Module or Factory:
val remoteDataSource: ContentRemoteDataSource = RetrofitContentDataSource(apiService)
val repository: ContentRepository = ContentRepositoryImpl(remoteDataSource)
```

### Impact on the rest of the application:
* **UI Layer**: `0` lines changed.
* **ViewModels**: `0` lines changed.
* **Use Cases**: `0` lines changed.
* **Domain Entities**: `0` lines changed.
* **Repository Implementation**: `0` lines changed.

---

## 8. Known Limitations

* **Offline Playback & DRM**: Offline video download/caching and Widevine DRM encryption are omitted as part of the current assignment scope.
* **Track Selection**: Subtitle and audio track selection uses ExoPlayer's default controls rather than a custom modal bottom sheet.
* **Authentication**: User profiles and authentication tokens are not integrated in the mock state.

---

## 9. Code Obfuscation & Shrinking (R8 / ProGuard)

Release builds use **R8** for code shrinking, resource shrinking, optimization, and obfuscation.

### Configuration (`app/build.gradle.kts`)
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

### ProGuard Rules (`app/proguard-rules.pro`)
* **Gson & DTO Models**: Preserves `@SerializedName` fields and models in `com.ott.gluedin.data.dto.**` and `com.ott.gluedin.domain.entity.**` to prevent reflection failures.
* **Media3 / ExoPlayer**: Preserves dynamic codec discovery and UI components.
* **Custom Views**: Keeps custom views like `TopCropImageView`.
* **Coroutines & ViewModels**: Retains ViewModel reflection constructors and coroutine internals.
* **Stack Tracing**: Retains `SourceFile` and `LineNumberTable` for retracing release crashes with `mapping.txt`.

### Build Verification
* Run `./gradlew assembleRelease` to produce the shrunk and obfuscated APK.
* Mapping file output: `app/build/outputs/mapping/release/mapping.txt`.

