package com.heitoranglada.coffeeapp.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heitoranglada.coffeeapp.data.CoffeeRepository
import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.ui.components.CoffeeForm
import com.heitoranglada.coffeeapp.ui.components.PourInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeListViewModel(repo: CoffeeRepository) : ViewModel() {
    val recipes: StateFlow<List<Recipe>?> = repo.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

class RecipeDetailViewModel(private val repo: CoffeeRepository, recipeId: Long) : ViewModel() {
    val recipe: StateFlow<Recipe?> = repo.observeRecipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(onDone: () -> Unit) {
        val r = recipe.value ?: return
        viewModelScope.launch {
            repo.deleteRecipe(r)
            onDone()
        }
    }
}

data class RecipeEditState(
    val loading: Boolean = true,
    val isNew: Boolean = true,
    val form: CoffeeForm = CoffeeForm(),
    val errors: List<String> = emptyList(),
    /** Preenchido quando a receita foi salva (id da receita). */
    val savedId: Long? = null,
)

/**
 * Edição/criação de receita.
 * @param recipeId receita a editar (0 = nova).
 * @param fromBrewId preparo do histórico usado como base para a nova receita (0 = nenhum).
 */
class RecipeEditViewModel(
    private val repo: CoffeeRepository,
    private val recipeId: Long,
    fromBrewId: Long,
) : ViewModel() {
    private val _state = MutableStateFlow(RecipeEditState())
    val state: StateFlow<RecipeEditState> = _state.asStateFlow()

    val coffees = repo.observeCoffees().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val grinders = repo.observeGrinders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var original: Recipe? = null

    init {
        viewModelScope.launch {
            val form = when {
                recipeId != 0L -> repo.getRecipe(recipeId)?.also { original = it }?.let { r ->
                    CoffeeForm(
                        name = r.name,
                        method = r.method,
                        coffee = r.coffee,
                        grinder = r.grinder,
                        grindSetting = r.grindSetting,
                        doseText = r.doseGrams?.let(::formatNumber).orEmpty(),
                        pours = r.pours.map(PourInput::from).ifEmpty { listOf(PourInput("0:00", "")) },
                        waterTempText = r.waterTempC?.toString().orEmpty(),
                        timeText = r.targetTimeSeconds?.let(::formatDuration).orEmpty(),
                        notes = r.notes,
                    )
                }
                fromBrewId != 0L -> repo.getBrew(fromBrewId)?.let { b ->
                    CoffeeForm(
                        name = b.recipeName?.let { "$it (ajustada)" } ?: "${b.method.label} – ${b.coffee.ifBlank { "minha receita" }}",
                        method = b.method,
                        coffee = b.coffee,
                        grinder = b.grinder,
                        grindSetting = b.grindSetting,
                        doseText = b.doseGrams?.let(::formatNumber).orEmpty(),
                        pours = b.pours.map(PourInput::from).ifEmpty { listOf(PourInput("0:00", "")) },
                        waterTempText = b.waterTempC?.toString().orEmpty(),
                        timeText = b.totalTimeSeconds.takeIf { it > 0 }?.let(::formatDuration).orEmpty(),
                        notes = b.notes,
                    )
                }
                else -> null
            } ?: CoffeeForm()
            _state.update { it.copy(loading = false, isNew = original == null, form = form) }
        }
    }

    fun onFormChange(form: CoffeeForm) = _state.update { it.copy(form = form, errors = emptyList()) }

    fun save() {
        val form = _state.value.form
        val errors = form.validate(requireName = true)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        val base = original
        val recipe = Recipe(
            id = base?.id ?: 0,
            name = form.name.trim(),
            method = form.method,
            coffee = form.coffee.trim(),
            grinder = form.grinder.trim(),
            grindSetting = form.grindSetting.trim(),
            doseGrams = form.dose,
            pours = form.parsedPours,
            waterTempC = form.waterTemp,
            targetTimeSeconds = form.timeSeconds,
            notes = form.notes.trim(),
            createdAt = base?.createdAt ?: System.currentTimeMillis(),
        )
        viewModelScope.launch {
            val id = repo.saveRecipe(recipe)
            _state.update { it.copy(savedId = id) }
        }
    }
}
