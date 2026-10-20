# SetSignal

SetSignal is a standalone Wear OS workout timer built around one job: finish the set, start the
rest, and get back to the next set without fumbling with a phone. It is a local-first MVP with
three example routines, a real timestamp-driven rest timer, session summaries, and persistent
history.

The app is intentionally small. There is no phone companion, account, cloud sync, internet access,
health-platform integration, sensor input, AI, or fabricated recovery/calorie metric. All data stays
on the watch.

> The included routines are configuration examples, not medical advice or a personalised training
> plan. Adjust the effort to your level and consult a professional if needed.

## What it does

- **Three built-in routines** – full-body strength, interval training, and gentle calisthenics.
- **Live set workflow** – mark a set done, auto-advance to the next set/exercise, and finish the
  session on the final set.
- **Rest countdown from timestamps** – remaining time is derived from a monotonic clock deadline,
  not from decrementing UI state. Pausing freezes both workout time and rest time.
- **Rest controls** – add the configured extra time, skip the rest, or continue after the countdown
  reaches zero. A vibration fires when the rest completes while the app process is running.
- **Pause/resume and safe cancel** – pausing is available during exercise and rest; cancelling asks
  for confirmation and saves the work completed so far as a cancelled session.
- **Session summary** – distinguishes completed and cancelled sessions and shows active time,
  completed sets/exercises, rest used, and per-exercise results.
- **Local history** – completed and cancelled sessions survive app restarts and can be cleared with
  a second confirmation.
- **Persistent preferences** – default rest, extra-rest duration, and haptics are stored in
  DataStore.
- **Watch-first UI** – dark AMOLED palette, warm white/gray type, lime accent, large timer numerals,
  a custom tick-mark progress ring, short transitions, and round-screen-friendly layouts.

## Screens and flow

| Screen | Purpose |
| --- | --- |
| Home | Last session, completed-session count, history/settings shortcuts |
| Routines | Browse the three local routines |
| Routine detail | Review exercises, tune rest in 15-second steps, start the workout |
| Workout overlay | Exercise, rest, rest-complete, paused, saving, and save-error states |
| Summary | Results for a completed or cancelled session |
| History | Session list grouped by day, with completed/cancelled totals |
| Settings | Default rest, extra rest, haptics, clear history, app information |

A workout is an activity-level full-screen overlay rather than a navigation destination. Swipe-back
cannot dismiss or lose an active session; the system back gesture opens the cancel confirmation
instead. After the session is saved, the overlay closes and the summary opens.

## Architecture

```
app/src/main/java/com/example/setsignal/
├── data/
│   ├── datastore/        DataStore preferences repository
│   ├── local/            Room database, DAO, entities, relation
│   └── repository/       Room-backed session repository
├── domain/
│   ├── data/             Routine catalog (three local examples)
│   ├── model/            Exercise, routine, session, and settings models
│   ├── repository/       Repository contracts
│   └── workout/          Pure workout state machine and result mapping
├── feature/              Home, routines, workout, history, summary, settings ViewModels
├── platform/             Monotonic time source and vibration adapter
├── ui/                   Theme, reusable components, formatting, screens
├── AppContainer.kt       Manual dependency container
├── SetSignalApplication.kt
└── MainActivity.kt       Navigation host and workout overlay
```

Key decisions:

- **Pure engine first.** `WorkoutEngine` owns set/rest/pause transitions and accepts
  `elapsedRealtime()` values from the caller. It has no Android or Compose dependency, which keeps
  timing logic deterministic and unit-testable.
- **Only terminal sessions are persisted.** A session is written once, after it is completed or
  cancelled. In-progress state never enters the database, so history cannot contain partial or
  duplicate rows.
- **Unidirectional state.** ViewModels expose `StateFlow`; Compose screens collect with lifecycle
  awareness. Feature ViewModels use small manual factories and `AppContainer` instead of a DI
  framework.
- **Data stays local.** Room stores sessions/exercises; DataStore stores preferences. The manifest
  requests only `VIBRATE` and declares no network permission.
- **Navigation.** Wear Compose Navigation (`SwipeDismissableNavHost`) handles the library, history,
  settings, and summaries. The live workout deliberately sits outside that graph.

### Timing semantics

- Elapsed workout time uses `SystemClock.elapsedRealtime()` and excludes paused intervals; rest time
  counts as workout time.
- Rest remaining is `restEndsAtElapsedMillis - now`, so recomposition, lifecycle changes, and
  screen-off gaps do not accumulate drift.
- Completing a set starts a rest or advances immediately when rest is set to zero.
- Completing the final set finishes without a trailing rest.
- At `REST_COMPLETE`, the engine returns exactly one transition event so the ViewModel can trigger
  one haptic; the user then taps **SEGUIR** to continue.

## Tech stack

Versions are declared in `gradle/libs.versions.toml`:

- Kotlin 2.2.10, AGP 9.4.1, Gradle 9.6.0
- Jetpack Compose for Wear OS 1.5.6 (Material 3 Wear, Foundation, Navigation)
- Compose BOM 2025.06.01, Activity Compose 1.10.1
- Lifecycle 2.9.4, Navigation Compose (Wear) 1.5.6
- Room 2.8.5 with KSP 2.2.10-2.0.2
- DataStore Preferences 1.2.1
- JUnit 4.13.2, Robolectric 4.16.1, coroutines-test 1.10.2

Compile/target SDK 37, min SDK 30, standalone watch app, application ID `com.example.setsignal`.

## Build and run

Requirements:

- Android Studio with Android SDK 37
- JDK compatible with the project's Gradle/AGP toolchain
- A Wear OS emulator or watch running API 30+

From the repository root:

```bat
gradlew.bat :app:assembleDebug
```

Install on a connected watch/emulator:

```bat
gradlew.bat :app:installDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Tests and checks

```bat
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:lintDebug
gradlew.bat :app:assembleDebug
```

Current local results:

- **28 JVM unit tests pass** (workout engine, Room repository with Robolectric, DataStore
  persistence/clamping, formatting/date helpers).
- **`:app:lintDebug` completes with 0 errors** (15 informational warnings, mostly available
  dependency upgrades and two manifest/backup attributes).
- **`:app:assembleDebug` succeeds** and produces `app-debug.apk`.

The test suite covers:

- Rest deadlines, completion transitions, skip, zero rest, pause/resume, extra rest, cancel, and
  result validation.
- Completed/cancelled session round-trip, exercise ordering, newest-first history, completed count,
  and clear-history behavior.
- Preference defaults, validation clamping, and persistence into a reopened DataStore file.
- Elapsed/clock/date formatting and history-day labels.

### Emulator verification

The debug build was installed and exercised on an Android Wear OS emulator (AVD `SetSignalWear`,
Wear OS 7, round 454×454 screen). The following were confirmed by inspecting the running app:

- Cold start into Home: the wordmark, tagline, and the primary **ELEGIR RUTINA** action render on
  the first screen without scroll, clipping, or overflow.
- Routines list: the three routine cards render with full titles and metadata.
- Routine detail: description, metrics, and the inline **EMPEZAR** action render on the first screen.
- Live workout: completing a set advances the set counter and the progress ring; the rest screen
  counts down from the chosen duration, and pause/resume freezes and resumes the countdown.
- **+15 s** adds the configured extra rest, and **SALTAR** skips the rest.
- Finishing the final set saves the session and opens the summary, which reports the routine, date,
  active time, and the completed-set total.
- Cancelling asks for confirmation; confirming saves the session as cancelled, and the cancelled
  session appears in the local history with its own status.
- History persists the session across an app restart.

Not verified on the emulator: the final inline **VOLVER** button in the summary screen was added in
the last commit and compiles and passes the test suite, but the change came after the last visual
pass, so it was not re-confirmed on screen. Vibration was not verified in the emulator because the
emulator has no meaningful haptics.

## Known limitations

- **Active-session recovery is not implemented.** If Android kills the process mid-workout, the
  in-progress set/rest state is lost. Only finished and cancelled sessions are durable.
- **No background alarm service.** Rest completion haptics fire while the app process and its
  coroutine ticker are alive. Screen-off behavior depends on the device/OS keeping the process
  running; haptics are not guaranteed after process death or aggressive background restrictions.
- **No background rest notifications.** The countdown is visible in the app; there is no system
  notification or ongoing complication.
- **Routines are fixed examples.** There is no routine editor, scheduling, progression, or exercise
  library in this MVP.
- **The UI is Spanish-only.** Strings are currently embedded in composables; there is no localized
  resource set yet.
- **Known visual defect in the Home "Última sesión" card.** When `SessionCard` is used in detailed
  mode, the completed-set count and the progress ring do not fit the row: the count wraps one
  character per line and the routine name is truncated. Routine rows, the workout overlay, and the
  summary screen are not affected. It needs a layout fix in
  `ui/components/SessionCards.kt` (give the metric its own line, or drop the ring in detailed
  mode).
- The app intentionally does not read heart rate, steps, or other sensor data and does not estimate
  calories or recovery.

## Project configuration notes

- KSP and AGP 9 require `android.disallowKotlinSourceSets=false` in `gradle.properties` until KSP
  adopts AGP's built-in Kotlin source-set DSL.
- Room schemas are exported to `app/schemas/` through the KSP `room.schemaLocation` argument.
- Backups are disabled (`android:allowBackup="false"`); history is local to the installed watch app.
