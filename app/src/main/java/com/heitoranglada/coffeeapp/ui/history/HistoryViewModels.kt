package com.heitoranglada.coffeeapp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.CoffeeRepository
import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.data.journal.BrewNote
import com.heitoranglada.coffeeapp.data.journal.JournalRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

class HistoryViewModel(repo: CoffeeRepository) : ViewModel() {
    val brews: StateFlow<List<Brew>?> = repo.observeBrews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@OptIn(ExperimentalCoroutinesApi::class)
class BrewDetailViewModel(
    private val repo: CoffeeRepository,
    private val brewId: Long,
    private val journal: JournalRepository? = null,
) : ViewModel() {
    val brew: StateFlow<Brew?> = repo.observeBrew(brewId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Receita usada no preparo, se ainda existir. */
    val recipe: StateFlow<Recipe?> = repo.observeBrew(brewId)
        .flatMapLatest { b -> b?.recipeId?.let { repo.observeRecipe(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Anotações do diário sobre este preparo. */
    val note: StateFlow<BrewNote?> = (journal?.observeNote(brewId) ?: flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Grão ligado ao preparo (pelo diário ou pelo nome do café). */
    val bean: StateFlow<Bean?> = combine(repo.observeBrew(brewId), note) { b, n -> b to n }
        .flatMapLatest { (b, n) ->
            val j = journal
            when {
                j == null || b == null -> flowOf(null)
                n?.beanId != null -> j.observeBean(n.beanId)
                else -> flow { emit(j.findBeanByName(b.coffee)) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(onDone: () -> Unit) {
        val b = brew.value ?: return
        viewModelScope.launch {
            journal?.deleteNote(brewId)
            repo.deleteBrew(b)
            onDone()
        }
    }
}
