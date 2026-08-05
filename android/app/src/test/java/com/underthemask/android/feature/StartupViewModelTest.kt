package com.underthemask.android.feature

import com.underthemask.android.core.network.AppError
import com.underthemask.android.core.network.AppException
import com.underthemask.android.core.network.ErrorKind
import com.underthemask.android.core.ui.AppEffect
import com.underthemask.android.feature.startup.StartupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {
    @Test
    fun `temporary network failure keeps saved session`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLobbyRepository().apply {
                reconnectError = AppException(
                    AppError(ErrorKind.NETWORK, "NETWORK_ERROR", "Server nije dostupan."),
                )
            }
            val viewModel = StartupViewModel(repository)

            assertEquals(AppEffect.OpenHome, viewModel.state.value.destination)
            assertNotNull(viewModel.state.value.errorMessage)
            assertNotNull(repository.session)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
