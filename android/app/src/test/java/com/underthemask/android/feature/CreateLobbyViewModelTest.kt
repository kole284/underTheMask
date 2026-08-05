package com.underthemask.android.feature

import com.underthemask.android.core.ui.AppEffect
import com.underthemask.android.feature.createLobby.CreateLobbyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateLobbyViewModelTest {
    @Test
    fun `successful create transitions from form to lobby effect`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        try {
            val repository = FakeLobbyRepository()
            val viewModel = CreateLobbyViewModel(repository)
            val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }

            viewModel.setHostName("Mina")
            viewModel.submit()

            assertEquals(AppEffect.OpenLobby("ABC234"), effect.await())
            assertFalse(viewModel.state.value.isSubmitting)
            assertEquals(null, viewModel.state.value.errorMessage)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
