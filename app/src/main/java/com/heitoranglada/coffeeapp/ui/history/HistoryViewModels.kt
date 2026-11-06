package com.heitoranglada.coffeeapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.CoffeeRepository
import com.heitoranglada.coffeeapp.data.Recipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

class HistoryViewModel(repo: CoffeeRepository) : ViewModel() {
    val brews: StateFlow<List<Brew>?> = repo.observeBrews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@OptIn(ExperimentalCoroutinesApi::class)
class BrewDetailViewModel(private val repo: CoffeeRepository, brewId: Long) : ViewModel() {
    val brew: StateFlow<Brew?> = repo.observeBrew(brewId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Receita usada no preparo, se ainda existir. */
    val recipe: StateFlow<Recipe?> = repo.observeBrew(brewId)
        .flatMapLatest { b -> b?.recipeId?.let { repo.observeRecipe(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(onDone: () -> Unit) {
        val b = brew.value ?: return
        viewModelScope.launch {
            repo.deleteBrew(b)
            onDone()
        }
    }
}
