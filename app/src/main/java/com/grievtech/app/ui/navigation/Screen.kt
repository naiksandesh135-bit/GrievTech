package com.grievtech.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Register : Screen("register")
    object TrackEntry : Screen("track_entry")
    object CitizenHome : Screen("citizen_home")
    object SubmitComplaint : Screen("submit_complaint")
    object TrackComplaint : Screen("track_complaint/{trackingId}") {
        fun createRoute(trackingId: String) = "track_complaint/$trackingId"
    }
    object ComplaintDetail : Screen("complaint_detail/{complaintId}") {
        fun createRoute(complaintId: String) = "complaint_detail/$complaintId"
    }
    object AdminHome : Screen("admin_home")
    object AdminAnalytics : Screen("admin_analytics")
}
