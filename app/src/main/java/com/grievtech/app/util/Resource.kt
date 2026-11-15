package com.grievtech.app.util

import android.util.Log

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
}

fun Throwable.toUserFriendlyMessage(tag: String = "GrievTechAuth"): String {
    Log.e(tag, "Authentication error [${this.javaClass.simpleName}]: ${this.message}", this)

    val msg = this.message?.lowercase() ?: ""
    val className = this.javaClass.name.lowercase()

    return when {
        msg.contains("invalid url") || msg.contains("malformedurl") || className.contains("malformedurl") ->
            "Supabase authentication configuration problem. Please verify host configuration."

        msg.contains("invalid login credentials") || msg.contains("invalid_credentials") || msg.contains("invalid_grant") || msg.contains("invalid email or password") ->
            "Invalid email or password. Please check your credentials and try again."

        msg.contains("already registered") || msg.contains("user_already_exists") || msg.contains("already exists") || msg.contains("user with this email already exists") ->
            "An account with this email address is already registered. Please sign in."

        msg.contains("email_not_confirmed") || msg.contains("email not confirmed") || msg.contains("unconfirmed") || msg.contains("verify your email") ->
            "Please verify your email address before signing in. Check your inbox for the confirmation link."

        msg.contains("password should be at least") || msg.contains("weak_password") ->
            "Password should be at least 6 characters long."

        msg.contains("over_email_send_rate_limit") || msg.contains("email_rate_limit") || msg.contains("email rate limit") ->
            "Email send rate limit exceeded on Supabase. Please wait a few minutes before trying again or try signing in."

        msg.contains("over_request_rate_limit") || msg.contains("too many requests") || msg.contains("rate_limit") ->
            "Request rate limit exceeded. Please wait a moment and try again."

        msg.contains("unable to resolve host") || msg.contains("connectexception") || msg.contains("network_error") ||
        msg.contains("no internet") || msg.contains("unknownhostexception") || msg.contains("sockettimeout") ||
        msg.contains("failed to connect") || className.contains("unknownhostexception") || className.contains("connectexception") ->
            "Network connection problem. Please check your internet connection and try again."

        msg.contains("serializationexception") || className.contains("serializationexception") || msg.contains("expected start of the object") ->
            "Supabase authentication configuration problem. Unexpected server response."

        else -> this.localizedMessage?.takeIf { it.isNotBlank() }
            ?: "An unexpected error occurred. Please try again."
    }
}
