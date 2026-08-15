package com.underthemask.android.core.config

import android.os.Build
import com.underthemask.android.BuildConfig

object LocalNetworkStartupPolicy {
    fun requiresPermissionGate(
        isLocalNetworkBuild: Boolean = BuildConfig.REQUIRES_LOCAL_NETWORK_PERMISSION,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): Boolean = isLocalNetworkBuild && sdkInt >= 37
}
