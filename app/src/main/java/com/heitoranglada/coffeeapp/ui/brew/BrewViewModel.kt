package com.heitoranglada.coffeeapp.ui.brew

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.CoffeeRepository
import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.StepKind
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.ui.components.CoffeeForm
import com.heitoranglada.coffeeapp.ui.components.PourInput
import com.heitoranglada.coffeeapp.ui.components.windowMax
import com.heitoranglada.coffeeapp.ui.components.windowMin
import java.util.Calendar
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
    /** Etapas que a pessoa marcou no preparo livre (só o tempo e o tipo; as gramas vêm no fim). */
    val marks: List<Pour> = emptyList(),
    /** Espresso: tempo do shot com décimos. */
    val shotSeconds: Double = 0.0,
    /** Espresso: shots de hoje com o mesmo método, do mais antigo ao mais novo (calibragem). */
    val todayShots: List<Brew> = emptyList(),
    val totalSeconds: Int = 0,
    val form: CoffeeForm = CoffeeForm(),
    val saveAsRecipe: Boolean = false,
    val errors: List<String> = emptyList(),
    /** Id do preparo salvo (evento para navegar). */
    val savedBrewId: Long? = null,
) {
    val isGuided: Boolean get() = recipe != null
    val currentMethod: BrewMethod get() = recipe?.method ?: method
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
                // No preparo livre, o primeiro despejo começa junto com o timer (no espresso não há despejos).
                marks = if (it.isGuided || it.currentMethod.isEspresso) emptyList() else listOf(Pour(0, 0.0)),
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
    fun markPour() = mark(StepKind.POUR)

    /** Marca uma etapa do preparo livre (despejo, mexer, virar, prensar, diluir). */
    fun mark(kind: StepKind) {
        val now = elapsedSeconds
        _state.update { s ->
            val last = s.marks.lastOrNull()
            if (last != null && last.startSeconds == now && last.kind == kind) s else s.copy(marks = s.marks + Pour(now, 0.0, kind))
        }
    }

    fun undoLastPour() = _state.update { s ->
        if (s.marks.size <= 1) s else s.copy(marks = s.marks.dropLast(1))
    }

    /** Para o timer e abre o formulário para completar e salvar o preparo. */
    fun finish() {
        tickJob?.cancel()
        _elapsedMs.value = SystemClock.elapsedRealtime() - startedAt
        val total = elapsedSeconds
        val shot = _elapsedMs.value / 1000.0
        viewModelScope.launch {
            val s = _state.value
            val recipe = s.recipe
            val method = s.currentMethod
            val last = repo.getLatestBrew()
            val lastSame = repo.getLatestBrew(method)
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
                    inverted = recipe.inverted,
                    yieldText = recipe.yieldGrams?.let(::formatNumber).orEmpty(),
                    windowMin = recipe.windowMin,
                    windowMax = recipe.windowMax,
                    preInfusionText = recipe.preInfusionSeconds?.toString().orEmpty(),
                )
            } else if (method.isEspresso) {
                // Espresso livre: repete o último shot (dose, moagem, rendimento) para só ajustar.
                val base = lastSame ?: last
                val dose = lastSame?.doseGrams ?: 18.0
                CoffeeForm(
                    name = "Espresso – meu shot",
                    method = method,
                    coffee = base?.coffee.orEmpty(),
                    grinder = base?.grinder.orEmpty(),
                    grindSetting = lastSame?.grindSetting.orEmpty(),
                    doseText = formatNumber(dose),
                    yieldText = formatNumber(lastSame?.yieldGrams ?: (dose * 2)),
                    pours = emptyList(),
                    waterTempText = lastSame?.waterTempC?.toString().orEmpty(),
                    timeText = formatDuration(total),
                )
            } else {
                // Preenche com o último preparo para poupar digitação (café, moedor, moagem, dose).
                CoffeeForm(
                    name = "${s.method.label} – preparo livre",
                    method = s.method,
                    coffee = last?.coffee.orEmpty(),
                    grinder = last?.grinder.orEmpty(),
                    grindSetting = last?.grindSetting.orEmpty(),
                    doseText = (lastSame ?: last?.takeIf { !it.method.isEspresso })?.doseGrams?.let(::formatNumber).orEmpty(),
                    pours = s.marks.map { PourInput(formatDuration(it.startSeconds), "", it.kind) }.ifEmpty { listOf(PourInput("0:00", "")) },
                    waterTempText = last?.waterTempC?.toString().orEmpty(),
                    timeText = formatDuration(total),
                )
            }
            val todayShots = if (method.isEspresso) repo.brewsSince(startOfToday(), method) else emptyList()
            _state.update { it.copy(phase = BrewPhase.REVIEW, totalSeconds = total, shotSeconds = shot, form = form, todayShots = todayShots) }
        }
    }

    /** Volta ao início, descartando o preparo atual. */
    fun reset() {
        tickJob?.cancel()
        _elapsedMs.value = 0
        _state.update {
            it.copy(
                phase = BrewPhase.READY,
                marks = emptyList(),
                totalSeconds = 0,
                shotSeconds = 0.0,
                todayShots = emptyList(),
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
            yieldGrams = if (form.method.isEspresso) form.yieldGrams else null,
            shotSeconds = if (form.method.isEspresso) s.shotSeconds.takeIf { it > 0 } else null,
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
                        targetTimeSeconds = if (form.method.isEspresso) null else brew.totalTimeSeconds.takeIf { it > 0 },
                        notes = brew.notes,
                        inverted = form.inverted && form.method == BrewMethod.AEROPRESS,
                        yieldGrams = brew.yieldGrams,
                        windowMinSeconds = if (form.method.isEspresso) form.windowMin else null,
                        windowMaxSeconds = if (form.method.isEspresso) form.windowMax else null,
                        preInfusionSeconds = if (form.method.isEspresso) form.preInfusion else null,
                    ),
                )
            }
            _state.update { it.copy(savedBrewId = brewId) }
        }
    }

    /** Chamado pela tela depois de navegar para o preparo salvo. */
    fun onSavedHandled() = reset()
}

/** Meia-noite de hoje, no fuso do aparelho. */
fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
