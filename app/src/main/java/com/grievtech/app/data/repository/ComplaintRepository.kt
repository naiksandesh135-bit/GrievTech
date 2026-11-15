package com.grievtech.app.data.repository

import android.util.Log
import com.grievtech.app.data.model.Complaint
import com.grievtech.app.data.model.TrackResult
import com.grievtech.app.util.MediaProcessResult
import com.grievtech.app.util.Resource
import com.grievtech.app.util.toUserFriendlyMessage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ComplaintRepository {
    fun getComplaints(): Flow<Resource<List<Complaint>>>
    fun createComplaint(complaint: Complaint, media: MediaProcessResult?): Flow<Resource<Complaint>>
    suspend fun checkDuplicates(complaint: Complaint): List<Complaint>
    suspend fun trackByTrackingId(trackingId: String): Resource<TrackResult>
}

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ComplaintRepository {

    override fun getComplaints(): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: supabaseClient.auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("User not authenticated")

            val complaints = supabaseClient.postgrest["complaints"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Complaint>()

            emit(Resource.Success(complaints))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("GrievTechAuth", "Failed to fetch complaints: ${e.message}", e)
            emit(Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e))
        }
    }

    override fun createComplaint(
        complaint: Complaint,
        media: MediaProcessResult?
    ): Flow<Resource<Complaint>> = flow {
        emit(Resource.Loading)
        var uploadedObjectPath: String? = null
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: supabaseClient.auth.currentSessionOrNull()?.user?.id
                ?: throw IllegalStateException("User not authenticated")

            var mediaUrl: String? = complaint.mediaUrl
            var mediaType = complaint.mediaType

            if (media is MediaProcessResult.Success) {
                val timestamp = System.currentTimeMillis()
                val randomSuffix = UUID.randomUUID().toString().take(6)
                val objectPath = "$userId/${timestamp}_$randomSuffix.${media.fileName.substringAfterLast('.', "tmp")}"
                uploadedObjectPath = objectPath

                Log.d("GrievTechAuth", "Uploading media to bucket 'complaint-media' at path: $objectPath")
                val bucket = supabaseClient.storage["complaint-media"]
                bucket.upload(objectPath, media.bytes) {
                    upsert = true
                }
                mediaUrl = bucket.publicUrl(objectPath)
                mediaType = media.mediaType
                Log.d("GrievTechAuth", "Media upload successful. Public URL: $mediaUrl")
            }

            val complaintToInsert = complaint.copy(
                userId = userId,
                mediaUrl = mediaUrl,
                mediaType = mediaType
            )

            Log.d("GrievTechAuth", "Inserting complaint row into Supabase 'complaints' table")
            val createdComplaint = supabaseClient.postgrest["complaints"]
                .insert(complaintToInsert) {
                    select()
                }
                .decodeSingle<Complaint>()

            Log.d("GrievTechAuth", "Complaint created successfully with tracking_id: ${createdComplaint.trackingId}")
            emit(Resource.Success(createdComplaint))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("GrievTechAuth", "Failed to create complaint: ${e.message}", e)

            if (uploadedObjectPath != null) {
                try {
                    Log.w("GrievTechAuth", "Deleting orphaned storage file at path: $uploadedObjectPath")
                    supabaseClient.storage["complaint-media"].delete(uploadedObjectPath)
                } catch (cleanupException: Exception) {
                    Log.e("GrievTechAuth", "Failed to delete orphaned storage file", cleanupException)
                }
            }

            emit(Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e))
        }
    }

    override suspend fun checkDuplicates(complaint: Complaint): List<Complaint> {
        // TODO Phase 6: Implement ML & Geo-proximity duplicate detection
        return emptyList()
    }

    override suspend fun trackByTrackingId(trackingId: String): Resource<TrackResult> {
        return try {
            val result = supabaseClient.postgrest.rpc("track_complaint", buildJsonObject {
                put("p_tracking_id", trackingId)
            }).decodeSingle<TrackResult>()
            Resource.Success(result)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Resource.Error(e.toUserFriendlyMessage("GrievTechAuth"), e)
        }
    }
}
