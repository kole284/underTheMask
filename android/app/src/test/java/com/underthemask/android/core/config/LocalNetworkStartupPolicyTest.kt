package com.underthemask.android.core.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalNetworkStartupPolicyTest {
    @Test
    fun `release build does not use local network permission gate`() {
        assertFalse(
            LocalNetworkStartupPolicy.requiresPermissionGate(
                isLocalNetworkBuild = false,
                sdkInt = 37,
            ),
        )
    }

    @Test
    fun `debug local build uses permission gate on Android 37 and newer`() {
        assertTrue(
            LocalNetworkStartupPolicy.requiresPermissionGate(
                isLocalNetworkBuild = true,
                sdkInt = 37,
            ),
        )
    }

    @Test
    fun `debug local build skips permission gate before Android 37`() {
        assertFalse(
            LocalNetworkStartupPolicy.requiresPermissionGate(
                isLocalNetworkBuild = true,
                sdkInt = 36,
            ),
        )
    }
}
