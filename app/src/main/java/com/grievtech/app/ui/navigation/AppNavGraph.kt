package com.grievtech.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.grievtech.app.data.model.UserRole
import com.grievtech.app.ui.auth.AuthViewModel
import com.grievtech.app.ui.screens.AdminAnalyticsScreen
import com.grievtech.app.ui.screens.AdminHomeScreen
import com.grievtech.app.ui.screens.CitizenHomeScreen
import com.grievtech.app.ui.screens.ComplaintDetailScreen
import com.grievtech.app.ui.screens.LandingScreen
import com.grievtech.app.ui.screens.LoginScreen
import com.grievtech.app.ui.screens.OnboardingScreen
import com.grievtech.app.ui.screens.RegisterScreen
import com.grievtech.app.ui.screens.SplashScreen
import com.grievtech.app.ui.screens.SubmitComplaintScreen
import com.grievtech.app.ui.screens.TrackComplaintScreen
import com.grievtech.app.ui.screens.TrackEntryScreen
import com.grievtech.app.ui.session.AuthState
import com.grievtech.app.ui.session.SessionViewModel
import com.grievtech.app.ui.submit.SubmitViewModel
import com.grievtech.app.util.LanguageManager

@Composable
fun AppNavGraph(
    navController: NavHostController,
    languageManager: LanguageManager,
    modifier: Modifier = Modifier,
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val authState by sessionViewModel.authState.collectAsState()
    val hasSeenOnboarding by languageManager.hasSeenOnboarding.collectAsState(initial = true)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                sessionViewModel = sessionViewModel,
                onNavigateNext = { state ->
                    if (!hasSeenOnboarding) {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        when (state) {
                            is AuthState.Citizen -> {
                                navController.navigate(Screen.CitizenHome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            is AuthState.Admin -> {
                                navController.navigate(Screen.AdminHome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            is AuthState.Unauthenticated -> {
                                navController.navigate(Screen.Landing.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            is AuthState.Loading -> {}
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                languageManager = languageManager,
                onOnboardingFinished = {
                    val destination = when (authState) {
                        is AuthState.Citizen -> Screen.CitizenHome.route
                        is AuthState.Admin -> Screen.AdminHome.route
                        else -> Screen.Landing.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Landing.route) {
            LandingScreen(
                languageManager = languageManager,
                onNavigateToLogin = { role ->
                    navController.navigate("${Screen.Login.route}?role=$role")
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToTrack = {
                    navController.navigate(Screen.TrackEntry.route)
                }
            )
        }

        composable(
            route = "${Screen.Login.route}?role={role}",
            arguments = listOf(navArgument("role") { defaultValue = "CITIZEN" })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "CITIZEN"
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                initialRole = role,
                viewModel = authViewModel,
                onLoginSuccess = { userRole ->
                    sessionViewModel.fetchProfile()
                    val destination = if (userRole == UserRole.ADMIN) Screen.AdminHome.route else Screen.CitizenHome.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    sessionViewModel.fetchProfile()
                    navController.navigate(Screen.CitizenHome.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.TrackEntry.route) {
            TrackEntryScreen(
                languageManager = languageManager,
                onTrackSubmitted = { trackingId ->
                    navController.navigate(Screen.TrackComplaint.createRoute(trackingId))
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.CitizenHome.route) {
            when (authState) {
                is AuthState.Admin -> {
                    LaunchedEffect(authState) {
                        navController.navigate(Screen.AdminHome.route) {
                            popUpTo(Screen.CitizenHome.route) { inclusive = true }
                        }
                    }
                }
                is AuthState.Unauthenticated -> {
                    LaunchedEffect(authState) {
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                is AuthState.Citizen -> {
                    val profile = (authState as AuthState.Citizen).profile
                    CitizenHomeScreen(
                        userProfile = profile,
                        onLogoutClick = {
                            sessionViewModel.signOut()
                            navController.navigate(Screen.Landing.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onSubmitComplaintClick = {
                            navController.navigate(Screen.SubmitComplaint.route)
                        },
                        onTrackComplaintsClick = {
                            navController.navigate(Screen.TrackEntry.route)
                        }
                    )
                }
                is AuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        composable(Screen.SubmitComplaint.route) {
            val submitViewModel: SubmitViewModel = hiltViewModel()
            SubmitComplaintScreen(
                viewModel = submitViewModel,
                languageManager = languageManager,
                onNavigateToTrack = { trackingId ->
                    navController.navigate(Screen.TrackComplaint.createRoute(trackingId)) {
                        popUpTo(Screen.CitizenHome.route)
                    }
                },
                onBackClick = { navController.navigateUp() },
                onNavigateHome = {
                    navController.navigate(Screen.CitizenHome.route) {
                        popUpTo(Screen.CitizenHome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.TrackComplaint.route,
            arguments = listOf(navArgument("trackingId") { type = NavType.StringType; defaultValue = "GT-000000" })
        ) { backStackEntry ->
            val trackingId = backStackEntry.arguments?.getString("trackingId") ?: "GT-000000"
            TrackComplaintScreen(
                onComplaintClick = { complaintId ->
                    navController.navigate(Screen.ComplaintDetail.createRoute(complaintId))
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.ComplaintDetail.route,
            arguments = listOf(navArgument("complaintId") { type = NavType.StringType })
        ) { backStackEntry ->
            val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
            ComplaintDetailScreen(
                complaintId = complaintId,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(Screen.AdminHome.route) {
            when (authState) {
                is AuthState.Citizen -> {
                    LaunchedEffect(authState) {
                        navController.navigate(Screen.CitizenHome.route) {
                            popUpTo(Screen.AdminHome.route) { inclusive = true }
                        }
                    }
                }
                is AuthState.Unauthenticated -> {
                    LaunchedEffect(authState) {
                        navController.navigate(Screen.Landing.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                is AuthState.Admin -> {
                    val profile = (authState as AuthState.Admin).profile
                    AdminHomeScreen(
                        userProfile = profile,
                        onLogoutClick = {
                            sessionViewModel.signOut()
                            navController.navigate(Screen.Landing.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToAnalytics = {
                            navController.navigate(Screen.AdminAnalytics.route)
                        },
                        onBackClick = { navController.navigateUp() }
                    )
                }
                is AuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        composable(Screen.AdminAnalytics.route) {
            AdminAnalyticsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}
