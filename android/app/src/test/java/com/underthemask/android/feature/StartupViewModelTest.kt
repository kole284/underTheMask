package com.underthemask.android.feature

import com.underthemask.android.core.network.AppError
import com.underthemask.android.core.network.AppException
import com.underthemask.android.core.network.ErrorKind
import com.underthemask.android.core.ui.AppEffect
import com.underthemask.android.feature.startup.StartupViewModel
import com.underthemask.android.core.model.LobbyStatus
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
    fun `startup opens home without saved session`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLobbyRepository().apply {
                session = null
            }
            val viewModel = StartupViewModel(repository)

            assertEquals(AppEffect.OpenHome, viewModel.state.value.destination)
            assertEquals(null, viewModel.state.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `startup reconnect opens saved waiting lobby`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLobbyRepository().apply {
                lobby = sampleLobby().copy(status = LobbyStatus.WAITING)
            }
            val viewModel = StartupViewModel(repository)

            assertEquals(AppEffect.OpenLobby("ABC234"), viewModel.state.value.destination)
            assertEquals(null, viewModel.state.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }

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
