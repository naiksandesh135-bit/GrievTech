package com.grievtech.app.data.repository

import android.util.Log
import com.grievtech.app.util.Resource
import com.grievtech.app.util.toUserFriendlyMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    fun signUp(email: String, password: String, fullName: String, phone: String): Flow<Resource<Unit>>
    fun signIn(email: String, password: String): Flow<Resource<Unit>>
    fun signOut(): Flow<Resource<Unit>>
    fun sendPasswordReset(email: String): Flow<Resource<Unit>>
    val sessionStatus: Flow<Boolean>
    fun currentUserId(): String?
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            Log.d("GrievTechAuth", "Attempting signUp for email domain: ${email.substringAfter("@", "unknown")}")
            val metadata = buildJsonObject {
                put("full_name", fullName)
                put("phone", phone)
            }
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = metadata
            }
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: supabaseClient.auth.currentSessionOrNull()?.user?.id
            Log.d("GrievTechAuth", "SignUp request completed successfully. User UUID: $userId")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            Log.e("GrievTechAuth", "SignUp error [${e.javaClass.simpleName}]: message=${e.message}", e)
            emit(Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e))
        }
    }

    override fun signIn(email: String, password: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            Log.d("GrievTechAuth", "Attempting signIn for email domain: ${email.substringAfter("@", "unknown")}")
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("GrievTechAuth", "SignIn request completed successfully")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e))
        }
    }

    override fun signOut(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            Log.d("GrievTechAuth", "Attempting signOut")
            supabaseClient.auth.signOut()
            Log.d("GrievTechAuth", "SignOut completed successfully")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e))
        }
    }

    override fun sendPasswordReset(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            Log.d("GrievTechAuth", "Sending password reset link")
            supabaseClient.auth.resetPasswordForEmail(email)
            Log.d("GrievTechAuth", "Password reset request sent")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e))
        }
    }

    override val sessionStatus: Flow<Boolean>
        get() = supabaseClient.auth.sessionStatus.map { status ->
            val isAuth = status is SessionStatus.Authenticated
            Log.d("GrievTechAuth", "SessionStatus changed: $status (Authenticated=$isAuth)")
            isAuth
        }

    override fun currentUserId(): String? {
        return supabaseClient.auth.currentSessionOrNull()?.user?.id
    }
}
