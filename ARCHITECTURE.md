# WidgetPtP — Structure & Architecture

`com.ivi.widgetptp` — an Android automotive home-screen widget host. Renders three
configurable widget "slots" (date/time, driver health, weather, navigation, tire
pressure demo, logo) over a looping background video, sourced either from fake
in-memory data or from the platform's `fauto.car.systemstate` System State Store.

## Tech stack

- Kotlin, Jetpack Compose (no XML layouts beyond the manifest/theme resources)
- Hilt for DI (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- Coroutines/Flow throughout — no LiveData, no RxJava
- Single Activity (`MainActivity`), no navigation graph — the whole app is one Compose screen

## Layered structure

```
app/src/main/java/com/ivi/widgetptp/
├── MainActivity.kt                  # single Activity, hosts Compose content
├── WidgetPtPApplication.kt          # @HiltAndroidApp entry point
├── data/
│   ├── fake/                        # in-memory MutableStateFlow-backed test doubles
│   ├── fauto/
│   │   └── FAutoCarConnectionManager.kt   # shared FAutoCar service connection
│   └── repository/                  # domain-interface implementations (Default*/SystemState*)
├── domain/
│   ├── model/WidgetModels.kt        # plain data classes + WidgetType enum
│   ├── policy/                      # pure classification/mapping logic
│   └── repository/Repositories.kt   # domain-facing interfaces (Flow-based)
├── di/
│   └── RepositoryModule.kt          # @Binds — wires interface → implementation
└── presentation/
    ├── host/                        # ViewModel, UiState, screen composition, UI mappers
    ├── theme/                       # Compose theme
    └── widgets/                     # per-slot Composables
```

Standard clean-architecture layering: `domain` has no Android/Compose/Hilt
dependency, `data` implements `domain` interfaces against either fakes or the
real platform, `presentation` consumes `domain` interfaces only (never a
concrete repository directly).

## Data layer: Default (fake) vs SystemState (real), one interface each

`domain/repository/Repositories.kt` declares one interface per data domain:

| Interface | Fake impl | Real impl |
|---|---|---|
| `WidgetConfigurationRepository` | `DefaultWidgetConfigurationRepository` (→ `FakeWidgetConfigurationDataSource`) | — (no real source yet) |
| `DateTimeRepository` | — | `SystemDateTimeRepository` (system clock, ticks every second) |
| `DriverHealthRepository` | `DefaultDriverHealthRepository` (→ fake) | `SystemStateDriverHealthRepository` |
| `WeatherRepository` | `DefaultWeatherRepository` (→ fake) | `SystemStateWeatherRepository` |
| `NavigationRepository` | `DefaultNavigationRepository` (→ fake) | `SystemStateNavigationRepository` |
| `VehicleDrivingStateRepository` | `DefaultVehicleDrivingStateRepository` (→ fake) | — |
| `UnitSettingsRepository` | `DefaultUnitSettingsRepository` (→ fake) | — |

Which implementation is active is decided in exactly one place —
`di/RepositoryModule.kt`'s `@Binds` methods. Swapping a domain from fake to
real data is a one-line change there; nothing else in the app needs to know.
Currently bound to real System State: `DateTimeRepository`,
`DriverHealthRepository`, `WeatherRepository`, `NavigationRepository`.
Still fake: `WidgetConfigurationRepository` (widget slot selection),
`VehicleDrivingStateRepository`, `UnitSettingsRepository`.

### `FAutoCarConnectionManager` — the shared platform connection

All `SystemState*Repository` classes depend on one injected singleton,
`data/fauto/FAutoCarConnectionManager`, instead of each opening their own
`FAutoCar` service connection. It exposes:

```kotlin
val connectedCar: Flow<FAutoCar?>
```

built with `callbackFlow` + `shareIn(WhileSubscribed, replay = 1)` — one real
service connection is shared across every repository that's currently being
observed; it tears down when nothing is subscribed and reconnects on demand.

### System State repository pattern

Every `SystemState*Repository` follows the same shape (see
`SystemStateNavigationRepository.kt` or `SystemStateWeatherRepository.kt` for
the fullest examples):

1. `connectionManager.connectedCar.flatMapLatest { fAutoCar -> ... }` — re-subscribes
   whenever the underlying connection changes.
2. `fAutoCar.getIVICarManager(FAutoCarSystemStateManager.SERVICE_NAME) as? FAutoCarSystemStateManager`
   — **this is the correct accessor**; an earlier draft used a nonexistent
   `getFAutoCarManager(...)` and silently produced no data.
3. `callbackFlow { ... }` registers one `PropertyListener<T>` per `PropertyID`,
   does an initial `getProperty(...)` snapshot, and unregisters everything in
   `awaitClose`.
4. Local mutable state + `synchronized(stateLock)` combine the individual
   property callbacks into one immutable domain model, emitted via `trySend`.

Real `PropertyID`s currently consumed: `NAVIGATION_ACTIVE`,
`NAVIGATION_DESTINATION_NAME`, `NAVIGATION_ETA_SECONDS`,
`NAVIGATION_REMAINING_DISTANCE` (navigation), plus the weather and driver
health equivalents. `remainingDurationMinutes` and `routeProgress` are **not**
platform properties — they're derived client-side from `NAVIGATION_ETA_SECONDS`
and `NAVIGATION_REMAINING_DISTANCE` respectively (progress needs a cached
"distance at route start" baseline, guarded against being locked to a
transient `0` read before the platform's first real value arrives).

## Domain layer

- **`WidgetModels.kt`** — `WidgetType` enum (`DATE_TIME`, `DRIVER_HEALTH`,
  `WEATHER`, `TIRE_PRESSURE`, `LOGO_DISPLAY`, `NAVIGATION`) plus one data class
  per domain (`DateTimeSnapshot`, `DriverHealthData`, `WeatherData`,
  `NavigationData`). All fields nullable/optional where "no data yet" is a
  real state, not an error.
- **`domain/policy/`** — pure, side-effect-free classification logic with no
  repository/Compose dependency: `HealthClassificationPolicy` (heart rate /
  respiration thresholds), `WeatherCodePolicy` (weather-API code → internal
  `WeatherCondition`), `DaylightPolicy` (hour-of-day → day/night for icon
  selection).

## Presentation layer

### `WidgetHostViewModel` — the 3-slot selector

```kotlin
val uiState = widgetConfigurationRepository.selectedWidgets   // List<WidgetType>
    .flatMapLatest(::observeSelection)                        // → 3 parallel slot flows
    .stateIn(viewModelScope, WhileSubscribed(5_000L), WidgetHostUiState())
```

`selectedWidgets` (currently always
`[DRIVER_HEALTH, DATE_TIME, NAVIGATION]` from the fake config source) is
mapped to exactly three ordered slots. Each slot's `WidgetType` is routed
through `observeSlot(type)`, a `when` that picks the right repository flow
and UI mapper. `WEATHER` additionally rotates between current and destination
weather every 5s once both are available (`rotatingWeather`).

### `WidgetSlotUiState` — one variant per rendered state

Sealed interface in `WidgetHostUiState.kt`: `Empty`, `DateTime`,
`DriverHealth`, `Weather`, `NavigationRoute`, `TirePressureDemo`,
`DriveToDisplay`, `LogoDisplay`. `TirePressureDemo` and `LogoDisplay` are
stateless `data object`s — they render a fixed image regardless of any
underlying data (there is no tire-pressure *data* in this app anymore; the
slot just needs to know whether the vehicle `isDriving` to pick
`TirePressureDemo` vs `DriveToDisplay`).

### UI mappers (`WidgetUiMappers.kt`)

One `@Inject`-able mapper class per domain (`DateTimeUiMapper`,
`DriverHealthUiMapper`, `NavigationUiMapper`, `WeatherUiMapper`) — pure
functions from a domain model to a `WidgetSlotUiState` variant. This is where
unit formatting happens (km, minutes, `°C`/`°F`, 12/24h clock, day-of-week).

### Screen composition (`WidgetHostScreen.kt`)

```
Box(fillMaxSize, background = Color.Black)        // fallback color behind video
├── BackgroundVideo(fillMaxSize)                   # looping muted VideoView, lifecycle-aware
└── Row(fillMaxSize)                                # foreground content, transparent
    ├── Spacer(weight 1f)                           # left gutter
    └── Box(weight 1f, centered)
        └── Row(1701.dp × 160.dp)                   # the 3 slots
            ├── WidgetSlot(uiState.first,  567×160.dp)
            ├── WidgetSlot(uiState.second, 567×160.dp)
            └── WidgetSlot(uiState.third,  567×160.dp)
```

`BackgroundVideo` wraps a plain `android.widget.VideoView` via `AndroidView`
(Compose has no native video primitive), looped/muted on prepare, started/
paused through a `LifecycleEventObserver` rather than Activity callbacks —
requires `res/raw/phud_dashboard_3840x208.mp4` to exist or the app won't
build. **Both `BackgroundVideo` and the content `Row` must be direct children
of the same `Box`** — as siblings with no shared parent, or with the content
`Row` still carrying its own opaque background, the video silently disappears
behind whatever's drawn after it.

`WidgetComposables.kt` holds one `@Composable fun XxxWidget()` per slot type,
dispatched from a `WidgetSlot(state: WidgetSlotUiState)` entry point
(`when (state) { ... }`).

## Dependency injection (`di/RepositoryModule.kt`)

One `@Module @InstallIn(SingletonComponent::class) abstract class` with a
`@Binds @Singleton` method per domain interface. This file alone determines
fake vs. real for every data domain — grep here first when data "isn't
showing up" in the widget; the underlying repository may simply not be wired
to the System State implementation yet.

## Testing (`app/src/test/`)

Mirrors the main source layout. `WidgetHostViewModelTest.kt` constructs the
ViewModel directly against `Default*Repository`/`Fake*DataSource` instances
(bypassing Hilt/DI entirely) and drives it with `StandardTestDispatcher` +
`runCurrent()`. Domain policy classes have their own focused unit tests
(`HealthClassificationPolicyTest`, `WeatherPoliciesTest`).

## Known gaps

- `WidgetConfigurationRepository` has no real (System State-backed) slot
  selection yet — always the fake 3-widget default.
- `WeatherRepository.destinationWeather` is hardcoded `flowOf(null)` — no
  destination-weather property exists yet, so the weather slot never
  actually rotates in practice.
- Tire pressure has no data path at all by design — the slot only reflects
  `VehicleDrivingStateRepository.isDriving`, not a PSI reading.
