package com.heitoranglada.coffeeapp

import android.app.Application
import com.heitoranglada.coffeeapp.data.CoffeeDatabase
import com.heitoranglada.coffeeapp.data.CoffeeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CoffeeApplication : Application() {
    val repository: CoffeeRepository by lazy { CoffeeRepository(CoffeeDatabase.create(this)) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { repository.seedIfFirstRun(this@CoffeeApplication) }
    }
}
