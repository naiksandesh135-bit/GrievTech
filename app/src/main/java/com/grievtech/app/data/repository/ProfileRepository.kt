package com.grievtech.app.data.repository

import android.util.Log
import com.grievtech.app.data.model.Profile
import com.grievtech.app.util.Resource
import com.grievtech.app.util.toUserFriendlyMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    suspend fun getMyProfile(): Resource<Profile>
    fun updateLanguage(code: String): Flow<Resource<Unit>>
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ProfileRepository {

    override suspend fun getMyProfile(): Resource<Profile> {
        return try {
            val user = supabaseClient.auth.currentUserOrNull()
                ?: supabaseClient.auth.currentSessionOrNull()?.user
                ?: return Resource.Error("User not authenticated")

            val userId = user.id
            Log.d("GrievTechAuth", "Fetching profile for user UUID: $userId")

            val profile = supabaseClient.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Profile>()

            Log.d("GrievTechAuth", "Profile fetched successfully: id=${profile.id}, role=${profile.role}")
            Resource.Success(profile)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("GrievTechAuth", "Failed to fetch profile: ${e.javaClass.simpleName} - ${e.message}", e)
            Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e)
        }
    }

    override fun updateLanguage(code: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: supabaseClient.auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("User not authenticated")

            Log.d("GrievTechAuth", "Updating language preference to '$code'")

            supabaseClient.postgrest["profiles"]
                .update({
                    set("preferred_language", code)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            Log.d("GrievTechAuth", "Language updated successfully")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("GrievTechAuth", "Failed to update language: ${e.message}", e)
            emit(Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e))
        }
    }
}
