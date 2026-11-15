package com.grievtech.app.data.repository

import com.grievtech.app.data.model.Feedback
import com.grievtech.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

interface FeedbackRepository {
    suspend fun submitFeedback(feedback: Feedback): Resource<Unit>
}

@Singleton
class FeedbackRepositoryImpl @Inject constructor() : FeedbackRepository {
    override suspend fun submitFeedback(feedback: Feedback): Resource<Unit> {
        TODO("Implemented in Phase 4")
    }
}
