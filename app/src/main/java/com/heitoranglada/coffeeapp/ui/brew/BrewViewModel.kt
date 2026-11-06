package com.heitoranglada.coffeeapp.ui.brew

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.CoffeeRepository
import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.ui.components.CoffeeForm
import com.heitoranglada.coffeeapp.ui.components.PourInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BrewPhase { READY, RUNNING, REVIEW }

data class BrewUiState(
    val loading: Boolean = true,
    val phase: BrewPhase = BrewPhase.READY,
    /** Receita do preparo guiado; null = preparo livre. */
    val recipe: Recipe? = null,
    /** Método escolhido antes de iniciar um preparo livre. */
    val method: BrewMethod = BrewMethod.V60,
    /** Segundos em que a pessoa tocou "Novo despejo" (preparo livre). */
    val pourMarks: List<Int> = emptyList(),
    val totalSeconds: Int = 0,
    val form: CoffeeForm = CoffeeForm(),
    val saveAsRecipe: Boolean = false,
    val errors: List<String> = emptyList(),
    /** Id do preparo salvo (evento para navegar). */
    val savedBrewId: Long? = null,
) {
    val isGuided: Boolean get() = recipe != null
}

/**
 * Controla o timer de extração e o registro do preparo.
 * - Preparo guiado (recipeId != 0): mostra os despejos da receita no tempo certo.
 * - Preparo livre (recipeId == 0): a pessoa marca cada despejo e informa as gramas no final.
 */
class BrewViewModel(private val repo: CoffeeRepository, private val recipeId: Long) : ViewModel() {

    private val _state = MutableStateFlow(BrewUiState())
    val state: StateFlow<BrewUiState> = _state.asStateFlow()

    /** Tempo decorrido em milissegundos, atualizado ~10x por segundo enquanto o timer corre. */
    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    val coffees = repo.observeCoffees().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val grinders = repo.observeGrinders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var startedAt = 0L
    private var tickJob: Job? = null

    init {
        viewModelScope.launch {
            val recipe = if (recipeId != 0L) repo.getRecipe(recipeId) else null
            val lastMethod = repo.getLatestBrew()?.method
            _state.update {
                it.copy(loading = false, recipe = recipe, method = recipe?.method ?: lastMethod ?: BrewMethod.V60)
            }
        }
    }

    private val elapsedSeconds: Int get() = (_elapsedMs.value / 1000).toInt()

    fun selectMethod(method: BrewMethod) = _state.update { it.copy(method = method) }

    fun start() {
        startedAt = SystemClock.elapsedRealtime()
        _elapsedMs.value = 0
        _state.update {
            it.copy(
                phase = BrewPhase.RUNNING,
                // No preparo livre, o primeiro despejo começa junto com o timer.
                pourMarks = if (it.isGuided) emptyList() else listOf(0),
                savedBrewId = null,
                errors = emptyList(),
            )
        }
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                _elapsedMs.value = SystemClock.elapsedRealtime() - startedAt
                delay(100)
            }
        }
    }

    /** Marca o início de um novo despejo (ignora toques duplos no mesmo segundo). */
    fun markPour() {
        val now = elapsedSeconds
        _state.update { s ->
            if (s.pourMarks.lastOrNull() == now) s else s.copy(pourMarks = s.pourMarks + now)
        }
    }

    fun undoLastPour() = _state.update { s ->
        if (s.pourMarks.size <= 1) s else s.copy(pourMarks = s.pourMarks.dropLast(1))
    }

    /** Para o timer e abre o formulário para completar e salvar o preparo. */
    fun finish() {
        tickJob?.cancel()
        _elapsedMs.value = SystemClock.elapsedRealtime() - startedAt
        val total = elapsedSeconds
        viewModelScope.launch {
            val s = _state.value
            val recipe = s.recipe
            val form = if (recipe != null) {
                CoffeeForm(
                    name = recipe.name,
                    method = recipe.method,
                    coffee = recipe.coffee,
                    grinder = recipe.grinder,
                    grindSetting = recipe.grindSetting,
                    doseText = recipe.doseGrams?.let(::formatNumber).orEmpty(),
                    pours = recipe.pours.map(PourInput::from).ifEmpty { listOf(PourInput("0:00", "")) },
                    waterTempText = recipe.waterTempC?.toString().orEmpty(),
                    timeText = formatDuration(total),
                )
            } else {
                // Preenche com o último preparo para poupar digitação (café, moedor, moagem, dose).
                val last = repo.getLatestBrew()
                CoffeeForm(
                    name = "${s.method.label} – preparo livre",
                    method = s.method,
                    coffee = last?.coffee.orEmpty(),
                    grinder = last?.grinder.orEmpty(),
                    grindSetting = last?.grindSetting.orEmpty(),
                    doseText = last?.doseGrams?.let(::formatNumber).orEmpty(),
                    pours = s.pourMarks.map { PourInput(formatDuration(it), "") }.ifEmpty { listOf(PourInput("0:00", "")) },
                    waterTempText = last?.waterTempC?.toString().orEmpty(),
                    timeText = formatDuration(total),
                )
            }
            _state.update { it.copy(phase = BrewPhase.REVIEW, totalSeconds = total, form = form) }
        }
    }

    /** Volta ao início, descartando o preparo atual. */
    fun reset() {
        tickJob?.cancel()
        _elapsedMs.value = 0
        _state.update {
            it.copy(
                phase = BrewPhase.READY,
                pourMarks = emptyList(),
                totalSeconds = 0,
                form = CoffeeForm(),
                saveAsRecipe = false,
                errors = emptyList(),
                savedBrewId = null,
            )
        }
    }

    fun onFormChange(form: CoffeeForm) = _state.update { it.copy(form = form, errors = emptyList()) }

    fun setSaveAsRecipe(value: Boolean) = _state.update { it.copy(saveAsRecipe = value, errors = emptyList()) }

    fun save() {
        val s = _state.value
        val form = s.form
        val errors = form.validate(requireName = s.saveAsRecipe)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        val pours = form.parsedPours
        val brew = Brew(
            recipeId = s.recipe?.id,
            recipeName = s.recipe?.name,
            method = form.method,
            coffee = form.coffee.trim(),
            grinder = form.grinder.trim(),
            grindSetting = form.grindSetting.trim(),
            doseGrams = form.dose,
            pours = pours,
            waterTempC = form.waterTemp,
            totalTimeSeconds = form.timeSeconds ?: s.totalSeconds,
            rating = form.rating,
            notes = form.notes.trim(),
        )
        viewModelScope.launch {
            val brewId = repo.saveBrew(brew)
            if (s.saveAsRecipe) {
                repo.saveRecipe(
                    Recipe(
                        name = form.name.trim(),
                        method = brew.method,
                        coffee = brew.coffee,
                        grinder = brew.grinder,
                        grindSetting = brew.grindSetting,
                        doseGrams = brew.doseGrams,
                        pours = pours,
                        waterTempC = brew.waterTempC,
                        targetTimeSeconds = brew.totalTimeSeconds.takeIf { it > 0 },
                        notes = brew.notes,
                    ),
                )
            }
            _state.update { it.copy(savedBrewId = brewId) }
        }
    }

    /** Chamado pela tela depois de navegar para o preparo salvo. */
    fun onSavedHandled() = reset()
}
