package com.orgzly.android

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@UsesApplicationCoroutineScope
val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

@UsesApplicationCoroutineScope
object ApplicationScopeJava {

    @JvmStatic
    fun cancel() {
        applicationScope.cancel()
    }

}

@RequiresOptIn(
    message = "Use of long lived scope. A shorter lived scope such as viewModelScope or " +
            "lifecycleScope, or a WorkManager task is almost always more appropriate.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
annotation class UsesApplicationCoroutineScope