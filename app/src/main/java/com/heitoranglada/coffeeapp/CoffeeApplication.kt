package com.heitoranglada.coffeeapp

import android.app.Application
import com.heitoranglada.coffeeapp.data.CalculatorStore
import com.heitoranglada.coffeeapp.data.CoffeeDatabase
import com.heitoranglada.coffeeapp.data.CoffeeRepository
import com.heitoranglada.coffeeapp.data.journal.JournalDatabase
import com.heitoranglada.coffeeapp.data.journal.JournalRepository
import com.heitoranglada.coffeeapp.data.journal.PhotoStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CoffeeApplication : Application() {
    val repository: CoffeeRepository by lazy {
        CoffeeRepository(CoffeeDatabase.create(this)).also { repo ->
            // Os grãos do diário também aparecem como sugestão no campo "café".
            repo.extraCoffees = journal.observeBeanNames()
        }
    }

    val journal: JournalRepository by lazy { JournalRepository(JournalDatabase.create(this), PhotoStore(this)) }

    val calculator: CalculatorStore by lazy { CalculatorStore(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { repository.seedIfFirstRun(this@CoffeeApplication) }
    }
}
