package com.grievtech.app.di

import com.grievtech.app.data.repository.AuthRepository
import com.grievtech.app.data.repository.AuthRepositoryImpl
import com.grievtech.app.data.repository.ComplaintRepository
import com.grievtech.app.data.repository.ComplaintRepositoryImpl
import com.grievtech.app.data.repository.FeedbackRepository
import com.grievtech.app.data.repository.FeedbackRepositoryImpl
import com.grievtech.app.data.repository.ProfileRepository
import com.grievtech.app.data.repository.ProfileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindComplaintRepository(
        impl: ComplaintRepositoryImpl
    ): ComplaintRepository

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        impl: FeedbackRepositoryImpl
    ): FeedbackRepository
}
