# GluedOTT - Android OTT Application

An OTT streaming application built with **Kotlin**, **XML-based UI**, **MVVM**, **Clean Architecture**, **Dagger Hilt (KSP)**, and **Android Media3 (ExoPlayer)**.

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

The application strictly adheres to **Clean Architecture** and **MVVM (Model-View-ViewModel)** with **Unidirectional Data Flow (UDF)** and **Dagger Hilt** for Dependency Injection. This guarantees clear separation of concerns, high testability, and decoupled layers.

```
       ┌────────────────────────────────────────────────────────┐
       │                   Presentation Layer                   │
       │   • Activities & Fragments (@AndroidEntryPoint)        │
       │   • ViewModels (@HiltViewModel, StateFlow, UDF)        │
       │   • Adapters (ListAdapter + DiffUtil + Shared Pools)   │
       └───────────────────────────┬────────────────────────────┘
                                   │ User Actions / Observes UiState
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                      Domain Layer                      │
       │   • Use Cases (@Inject constructor)                    │
       │       (GetHomeFeed, GetDetail, TrackEvent)             │
       │   • Domain Entities (ContentItem, CategoryRail, etc.)  │
       │   • Repository Contracts (Interfaces)                  │
       └───────────────────────────┬────────────────────────────┘
                                   │ Depends on Repository Contract
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │                       Data Layer                       │
       │   • Repository Implementations (@Inject, @Singleton)   │
       │   • DTOs (Data Transfer Objects with Gson annotations) │
       │   • Mappers (ContentDtoMapper @Inject)                 │
       │   • Remote Data Source Abstraction                     │
       │       - MockJsonContentDataSource (Current)            │
       │       - RetrofitContentDataSource (Future)             │
       └───────────────────────────▲────────────────────────────┘
                                   │
       ┌───────────────────────────┴────────────────────────────┐
       │                 Dependency Injection (Hilt)            │
       │   • AppModule: Application-level Singletons (Gson)     │
       │   • RepositoryModule: Interface to Impl bindings       │
       │   • DispatchersModule: Qualified CoroutineDispatchers  │
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
├── GluedOttApp.kt                              // @HiltAndroidApp Application class
├── di/                                         // Dagger Hilt Dependency Injection Modules
│   ├── AppModule.kt                            // Gson & global singletons
│   ├── RepositoryModule.kt                     // Interface-to-Implementation bindings (@Binds)
│   └── DispatchersModule.kt                    // Qualified Dispatchers (@IoDispatcher, etc.)
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
│       ├── GetHomeFeedUseCase.kt               // Fetch home feed use case (@Inject)
│       ├── GetContentDetailUseCase.kt          // Fetch content item by ID (@Inject)
│       └── TrackPlayerEventUseCase.kt          // Process player analytics events (@Inject)
├── data/                                       // Data Access & Mapping Layer
│   ├── datasource/
│   │   ├── ContentRemoteDataSource.kt          // Remote data source abstraction
│   │   └── MockJsonContentDataSource.kt        // Raw JSON asset mock implementation (@Inject)
│   ├── dto/
│   │   ├── HomeFeedResponseDto.kt              // DTO for home API response
│   │   ├── CategoryRailDto.kt                  // DTO for rail payload
│   │   └── ContentItemDto.kt                   // DTO for media item
│   ├── mapper/
│   │   └── ContentDtoMapper.kt                 // Maps DTOs to Domain Entities (@Inject)
│   └── repository/
│       ├── ContentRepositoryImpl.kt            // Concrete ContentRepository (@Inject, @Singleton)
│       └── AnalyticsRepositoryImpl.kt          // Concrete AnalyticsRepository (@Inject, @Singleton)
├── player/                                     // Media3 / ExoPlayer Engine
│   ├── PlayerManager.kt                        // Lifecycle-aware player manager
│   └── PlayerEventBridge.kt                    // Translates Player.Listener to Domain Events
└── presentation/                               // UI & ViewModels (Feature-packaged)
    ├── common/
    │   ├── UiState.kt                          // Unified sealed interface for UI state
    │   └── TopCropImageView.kt                 // Custom ImageView anchoring scale to top
    ├── splash/
    │   └── SplashActivity.kt                   // Branded animated splash screen (@AndroidEntryPoint)
    ├── main/
    │   └── MainActivity.kt                     // Container Activity managing navigation (@AndroidEntryPoint)
    ├── home/
    │   ├── ui/
    │   │   └── HomeFragment.kt                 // Home Screen (@AndroidEntryPoint, Hero + Rails)
    │   ├── viewmodel/
    │   │   └── HomeViewModel.kt                // @HiltViewModel with StateFlow & scroll state
    │   └── adapter/
    │       ├── CategoryRailAdapter.kt          // Vertical adapter for rails (Shared ViewPool)
    │       └── ContentCardAdapter.kt           // Horizontal adapter for content cards
    ├── detail/
    │   ├── ui/
    │   │   └── DetailFragment.kt               // Detail Screen (@AndroidEntryPoint, Backdrop, Play CTA)
    │   └── viewmodel/
    │       └── DetailViewModel.kt              // @HiltViewModel for Detail Screen
    └── player/
        ├── PlayerActivity.kt                   // Fullscreen player (@AndroidEntryPoint)
        └── PlayerViewModel.kt                  // @HiltViewModel coordinating telemetry & state
```

---

## 4. Libraries Used

| Library | Version | Purpose |
| :--- | :--- | :--- |
| **Dagger Hilt** | `2.60.1` | Compile-time Dependency Injection framework |
| **Google KSP** | `2.3.6` | Kotlin Symbol Processing for fast, modern annotation processing |
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
   * Repositories depend on abstractions (`ContentRemoteDataSource`), bound cleanly via Hilt.

2. **RecyclerView Performance Optimizations:**
   * **`RecyclerView.RecycledViewPool` Sharing:** The parent `CategoryRailAdapter` shares a single `RecycledViewPool` among all horizontal rail adapters, minimizing view inflation churn during fast vertical scrolling.
   * **`ListAdapter` + `DiffUtil`:** Computes item differences on background threads to ensure 60fps scrolling.
   * **Rail Scroll State Preservation:** Horizontal scroll positions and vertical offset are preserved in `HomeViewModel` and restored seamlessly upon returning from detail views.

3. **Image Scaling & Visual Polish:**
   * Custom `TopCropImageView` anchors poster images to `dy = 0` (top crop), ensuring movie titles and faces are never clipped by center-cropping.

4. **Lifecycle Safety & Memory Leak Prevention:**
   * `StateFlow` collection uses `repeatOnLifecycle(Lifecycle.State.STARTED)`, suspending collectors when the app moves to the background.
   * ViewBinding references in Fragments are set to `null` in `onDestroyView()` to prevent memory leaks.
   * `ExoPlayer` is managed via `PlayerManager` using `context.applicationContext` to avoid Activity leaks, releasing on `onStop` (API $\ge$ 24) or `onPause` (API < 24).

5. **Playback State Retention:**
   * Playback position and `playWhenReady` flags are preserved across screen rotations and process death via `onSaveInstanceState` and restored seamlessly.

6. **Orientation Management:**
   * Home and Detail screens are locked to portrait orientation; `PlayerActivity` runs in sensor landscape. When exiting the player, the orientation explicitly reverts to portrait to avoid landscape leakage.

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

---

## 7. Replacing Mock Data Source with Real API (Retrofit)

With Hilt in place, replacing mock data with a production backend requires **zero changes** to ViewModels, Use Cases, Entities, or UI:

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
@Singleton
class RetrofitContentDataSource @Inject constructor(
    private val apiService: OttApiService,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContentRemoteDataSource {

    override suspend fun getHomeFeed(): HomeFeedResponseDto = withContext(ioDispatcher) {
        apiService.getHomeFeed()
    }

    override suspend fun getContentDetail(contentId: String): ContentItemDto = withContext(ioDispatcher) {
        apiService.getContentDetail(contentId)
    }
}
```

### Step 3: Update Hilt `RepositoryModule`
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContentRemoteDataSource(
        impl: RetrofitContentDataSource // Simply change binding from MockJson to Retrofit
    ): ContentRemoteDataSource
    
    // ...
}
```

---

## 8. Code Obfuscation & Shrinking (R8 / ProGuard)

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

### ProGuard Rules Highlights (`app/proguard-rules.pro`)
* **Dagger & Hilt**: Preserves Hilt component wrappers, DI modules, and generated factories.
* **Gson & DTO Models**: Preserves `@SerializedName` fields and models in `com.ott.gluedin.data.dto.**` and `com.ott.gluedin.domain.entity.**` to prevent reflection failures.
* **Media3 / ExoPlayer**: Preserves dynamic codec discovery and UI components.
* **Custom Views**: Keeps custom views like `TopCropImageView`.
* **Coroutines & ViewModels**: Retains ViewModel constructors and coroutine internals.
* **Stack Tracing**: Retains `SourceFile` and `LineNumberTable` for retracing release crashes with `mapping.txt`.

### Size Reduction Results
* **Debug APK**: `~14.4 MB`
* **Release APK (Optimized, Shrunk & Obfuscated)**: **`4.85 MB`** (~66% reduction)
* Mapping file output: `app/build/outputs/mapping/release/mapping.txt`.
