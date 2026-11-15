package com.grievtech.app.data.remote

import com.grievtech.app.data.model.Complaint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun fetchComplaints(): Flow<List<Complaint>> = flow {
        val complaints = supabaseClient.postgrest["complaints"]
            .select()
            .decodeList<Complaint>()
        emit(complaints)
    }

    suspend fun submitComplaint(complaint: Complaint): Complaint {
        return supabaseClient.postgrest["complaints"]
            .insert(complaint) {
                select()
            }
            .decodeSingle<Complaint>()
    }
}
