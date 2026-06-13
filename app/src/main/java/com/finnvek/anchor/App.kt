package com.finnvek.anchor

import android.app.Application
import com.finnvek.anchor.data.local.seed.TagSeeder
import com.finnvek.anchor.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var tagSeeder: TagSeeder

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            tagSeeder.seedIfNeeded()
        }
    }
}
