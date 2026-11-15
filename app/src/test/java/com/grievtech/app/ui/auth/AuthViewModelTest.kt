package com.grievtech.app.ui.auth

import com.grievtech.app.data.model.Profile
import com.grievtech.app.data.model.UserRole
import com.grievtech.app.data.repository.AuthRepository
import com.grievtech.app.data.repository.ProfileRepository
import com.grievtech.app.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        fakeProfileRepository = FakeProfileRepository()
        viewModel = AuthViewModel(fakeAuthRepository, fakeProfileRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_invalidEmail_setsErrorMessage() {
        viewModel.onLoginEmailChanged("invalid-email")
        viewModel.onLoginPasswordChanged("password123")

        viewModel.login()

        assertEquals("Please enter a valid email address.", viewModel.loginState.value.errorMessage)
    }

    @Test
    fun login_validCitizenCredentials_triggersSuccess() = runTest {
        viewModel.setTargetRole(UserRole.CITIZEN)
        viewModel.onLoginEmailChanged("citizen@grievtech.org")
        viewModel.onLoginPasswordChanged("Password123!")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(null, viewModel.loginState.value.errorMessage)
    }

    @Test
    fun login_citizenAttemptingAdminLogin_deniesAccess() = runTest {
        viewModel.setTargetRole(UserRole.ADMIN)
        viewModel.onLoginEmailChanged("citizen@grievtech.org")
        viewModel.onLoginPasswordChanged("Password123!")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            "Access denied. Your account does not have administrator privileges.",
            viewModel.loginState.value.errorMessage
        )
    }

    @Test
    fun login_validAdminCredentials_triggersAdminSuccess() = runTest {
        fakeProfileRepository.currentRole = UserRole.ADMIN
        viewModel.setTargetRole(UserRole.ADMIN)
        viewModel.onLoginEmailChanged("admin@grievtech.org")
        viewModel.onLoginPasswordChanged("Password123!")

        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(null, viewModel.loginState.value.errorMessage)
    }

    @Test
    fun register_invalidConfirmPassword_setsError() {
        viewModel.onRegisterFullNameChanged("Ravi Kumar")
        viewModel.onRegisterEmailChanged("ravi@grievtech.org")
        viewModel.onRegisterPhoneChanged("9876543210")
        viewModel.onRegisterPasswordChanged("Pass123!")
        viewModel.onRegisterConfirmPasswordChanged("Different123!")

        viewModel.register()

        assertEquals("Passwords do not match.", viewModel.registerState.value.confirmPasswordError)
    }

    @Test
    fun register_validData_callsSignUp() = runTest {
        viewModel.onRegisterFullNameChanged("Ravi Kumar")
        viewModel.onRegisterEmailChanged("ravi@grievtech.org")
        viewModel.onRegisterPhoneChanged("+919876543210")
        viewModel.onRegisterPasswordChanged("Pass123!")
        viewModel.onRegisterConfirmPasswordChanged("Pass123!")

        viewModel.register()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(null, viewModel.registerState.value.errorMessage)
    }
}

class FakeAuthRepository : AuthRepository {
    override fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
    }

    override fun signIn(email: String, password: String): Flow<Resource<Unit>> = flow {
        if (password == "Password123!") {
            emit(Resource.Success(Unit))
        } else {
            emit(Resource.Error("Invalid email or password."))
        }
    }

    override fun signOut(): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
    }

    override fun sendPasswordReset(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
    }

    override val sessionStatus: Flow<Boolean> = flow { emit(true) }

    override fun currentUserId(): String? = "user_123"
}

class FakeProfileRepository : ProfileRepository {
    var currentRole: UserRole = UserRole.CITIZEN

    override suspend fun getMyProfile(): Resource<Profile> {
        return Resource.Success(Profile(id = "user_123", fullName = "Test User", role = currentRole))
    }

    override fun updateLanguage(code: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Success(Unit))
    }
}
