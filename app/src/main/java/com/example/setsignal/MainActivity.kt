package com.example.setsignal

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.setsignal.feature.history.HistoryViewModel
import com.example.setsignal.feature.home.HomeViewModel
import com.example.setsignal.feature.routines.RoutineDetailViewModel
import com.example.setsignal.feature.settings.SettingsViewModel
import com.example.setsignal.feature.summary.SummaryViewModel
import com.example.setsignal.feature.workout.WorkoutOverlay
import com.example.setsignal.feature.workout.WorkoutViewModel
import com.example.setsignal.ui.screens.HistoryScreen
import com.example.setsignal.ui.screens.HomeScreen
import com.example.setsignal.ui.screens.RoutineDetailScreen
import com.example.setsignal.ui.screens.RoutinesScreen
import com.example.setsignal.ui.screens.SettingsScreen
import com.example.setsignal.ui.screens.SummaryScreen
import com.example.setsignal.ui.theme.SetSignalTheme

class MainActivity : ComponentActivity() {
    private val container: AppContainer
        get() = (application as SetSignalApplication).container

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SetSignalTheme {
                val workoutViewModel: WorkoutViewModel = viewModel(
                    factory = remember(container) {
                        SimpleViewModelFactory {
                            WorkoutViewModel(
                                routineCatalog = container.routineCatalog,
                                sessionRepository = container.sessionRepository,
                                settingsRepository = container.settingsRepository,
                                haptics = container.haptics,
                                timeSource = container.timeSource,
                            )
                        }
                    },
                )
                SetSignalApp(
                    container = container,
                    workoutViewModel = workoutViewModel,
                )
            }
        }
    }
}

private object Routes {
    const val HOME = "home"
    const val ROUTINES = "routines"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val ROUTINE_DETAIL = "routine/{routineId}"
    const val SUMMARY = "summary/{sessionId}"

    fun routine(id: String): String = "routine/${Uri.encode(id)}"
    fun summary(id: String): String = "summary/${Uri.encode(id)}"
}

@Composable
private fun SetSignalApp(
    container: AppContainer,
    workoutViewModel: WorkoutViewModel,
) {
    val navController = rememberSwipeDismissableNavController()
    val workoutState by workoutViewModel.uiState.collectAsStateWithLifecycle()

    AppScaffold(
        timeText = {
            if (!workoutState.active) {
                TimeText()
            }
        },
    ) {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.HOME) {
                val viewModel: HomeViewModel = viewModel(
                    key = "home",
                    factory = remember(container) {
                        SimpleViewModelFactory { HomeViewModel(container.sessionRepository) }
                    },
                )
                HomeScreen(
                    viewModel = viewModel,
                    onChooseRoutine = { navController.navigate(Routes.ROUTINES) },
                    onHistory = { navController.navigate(Routes.HISTORY) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenSession = { navController.navigate(Routes.summary(it)) },
                )
            }

            composable(Routes.ROUTINES) {
                RoutinesScreen(
                    routines = container.routineCatalog.routines,
                    onBack = { navController.popBackStack() },
                    onOpenRoutine = { navController.navigate(Routes.routine(it)) },
                )
            }

            composable(
                route = Routes.ROUTINE_DETAIL,
                arguments = listOf(navArgument("routineId") { type = NavType.StringType }),
            ) { entry ->
                val routineId = entry.arguments?.getString("routineId").orEmpty()
                val viewModel: RoutineDetailViewModel = viewModel(
                    key = "routine-$routineId",
                    factory = remember(container, routineId) {
                        SimpleViewModelFactory {
                            RoutineDetailViewModel(
                                routineId = routineId,
                                routineCatalog = container.routineCatalog,
                                settingsRepository = container.settingsRepository,
                            )
                        }
                    },
                )
                RoutineDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onStart = { restSeconds ->
                        workoutViewModel.start(routineId, restSeconds)
                    },
                )
            }

            composable(Routes.HISTORY) {
                val viewModel: HistoryViewModel = viewModel(
                    key = "history",
                    factory = remember(container) {
                        SimpleViewModelFactory { HistoryViewModel(container.sessionRepository) }
                    },
                )
                HistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenSession = { navController.navigate(Routes.summary(it)) },
                )
            }

            composable(Routes.SETTINGS) {
                val viewModel: SettingsViewModel = viewModel(
                    key = "settings",
                    factory = remember(container) {
                        SimpleViewModelFactory {
                            SettingsViewModel(
                                settingsRepository = container.settingsRepository,
                                sessionRepository = container.sessionRepository,
                            )
                        }
                    },
                )
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.SUMMARY,
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
            ) { entry ->
                val sessionId = entry.arguments?.getString("sessionId").orEmpty()
                val viewModel: SummaryViewModel = viewModel(
                    key = "summary-$sessionId",
                    factory = remember(container, sessionId) {
                        SimpleViewModelFactory {
                            SummaryViewModel(
                                sessionId = sessionId,
                                sessionRepository = container.sessionRepository,
                            )
                        }
                    },
                )
                SummaryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        AnimatedVisibility(
            visible = workoutState.active,
            enter = fadeIn(animationSpec = tween(160)),
            exit = fadeOut(animationSpec = tween(160)),
            modifier = Modifier.fillMaxSize(),
        ) {
            WorkoutOverlay(
                state = workoutState,
                onCompleteSet = workoutViewModel::completeSet,
                onTogglePause = workoutViewModel::togglePause,
                onAddRest = workoutViewModel::addRestTime,
                onSkipRest = workoutViewModel::skipRest,
                onCancel = workoutViewModel::cancelSession,
                onRetrySave = workoutViewModel::retrySave,
                onDiscard = workoutViewModel::discardUnsavedSession,
            )
        }
    }

    val savedSessionId = workoutState.savedSessionId
    LaunchedEffect(savedSessionId) {
        if (savedSessionId != null) {
            navController.navigate(Routes.summary(savedSessionId)) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
            workoutViewModel.consumeFinishedSession()
        }
    }
}

private class SimpleViewModelFactory<T : ViewModel>(
    private val create: () -> T,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
}
