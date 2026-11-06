package com.heitoranglada.coffeeapp.ui.brew

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.CalculatorStore
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.data.journal.BeanReminder
import com.heitoranglada.coffeeapp.data.journal.JournalRepository
import com.heitoranglada.coffeeapp.domain.CalcState
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
    /** Grãos do diário, para escolher o café. */
    val beans: List<Bean> = emptyList(),
    val selectedBeanId: Long? = null,
    /** Última anotação daquele café, mostrada antes de começar. */
    val reminder: BeanReminder? = null,
    val reminderDismissed: Boolean = false,
    /** Diário da xícara. */
    val liked: String = "",
    val change: String = "",
    /** Conta da calculadora escolhida para este preparo livre. */
    val plan: CalcState? = null,
) {
    val selectedBean: Bean? get() = beans.firstOrNull { it.id == selectedBeanId }

    val isGuided: Boolean get() = recipe != null
}

/**
 * Controla o timer de extração e o registro do preparo.
 * - Preparo guiado (recipeId != 0): mostra os despejos da receita no tempo certo.
 * - Preparo livre (recipeId == 0): a pessoa marca cada despejo e informa as gramas no final.
 */
class BrewViewModel(
    private val repo: CoffeeRepository,
    private val recipeId: Long,
    private val journal: JournalRepository? = null,
    private val calc: CalculatorStore? = null,
) : ViewModel() {

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
            // Café da receita (guiado) ou do último preparo (livre): já vem escolhido, com o lembrete.
            val j = journal ?: return@launch
            val coffee = recipe?.coffee ?: repo.getLatestBrew()?.coffee.orEmpty()
            val bean = j.findBeanByName(coffee)
            if (_state.value.selectedBeanId == null) {
                _state.update { it.copy(selectedBeanId = bean?.id) }
                loadReminder()
            }
        }
        journal?.let { j ->
            viewModelScope.launch { j.observeBeans().collect { list -> _state.update { it.copy(beans = list) } } }
            if (recipeId == 0L) {
                // "Preparar este café" no diário: o preparo livre já começa com o grão escolhido.
                viewModelScope.launch {
                    j.pendingBean.collect { id ->
                        if (id != null) {
                            j.consumePendingBean()
                            if (_state.value.phase == BrewPhase.READY) selectBean(id)
                        }
                    }
                }
            }
        }
        if (recipeId == 0L) {
            calc?.let { c -> viewModelScope.launch { c.plan.collect { p -> _state.update { it.copy(plan = p) } } } }
        }
    }

    /** Busca a última anotação do café escolhido (ou do café da receita). */
    private suspend fun loadReminder() {
        val j = journal ?: return
        val s = _state.value
        val bean = s.selectedBeanId?.let { j.getBean(it) }
        val reminder = when {
            bean != null -> j.reminderFor(bean)
            s.recipe != null -> j.reminderForCoffee(s.recipe.coffee)
            else -> null
        }
        _state.update { it.copy(reminder = reminder, reminderDismissed = false) }
    }

    /** Escolhe o café do preparo (null = nenhum). */
    fun selectBean(beanId: Long?) {
        _state.update { it.copy(selectedBeanId = beanId) }
        viewModelScope.launch { loadReminder() }
    }

    fun dismissReminder() = _state.update { it.copy(reminderDismissed = true) }

    /** Ao salvar: escolher um grão preenche o campo "café". */
    fun selectReviewBean(bean: Bean?) = _state.update { s ->
        s.copy(
            selectedBeanId = bean?.id,
            form = if (bean != null) s.form.copy(coffee = bean.name) else s.form,
            errors = emptyList(),
        )
    }

    fun setLiked(text: String) = _state.update { it.copy(liked = text) }
    fun setChange(text: String) = _state.update { it.copy(change = text) }

    fun clearPlan() {
        calc?.clearPlan()
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
                    coffee = s.selectedBean?.name ?: recipe.coffee,
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
                    coffee = s.selectedBean?.name ?: last?.coffee.orEmpty(),
                    grinder = last?.grinder.orEmpty(),
                    grindSetting = last?.grindSetting.orEmpty(),
                    // A dose da calculadora, se a pessoa veio de lá; senão, a do último preparo.
                    doseText = (s.plan?.coffeeGrams ?: last?.doseGrams)?.let(::formatNumber).orEmpty(),
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
                liked = "",
                change = "",
            )
        }
        // O lembrete pode ter mudado com o preparo que acabou de ser salvo.
        viewModelScope.launch { loadReminder() }
    }

    fun onFormChange(form: CoffeeForm) = _state.update { s ->
        // Digitar o nome de outro café desfaz a escolha do grão (ou escolhe o grão com aquele nome).
        val beanId = if (form.coffee == s.form.coffee) {
            s.selectedBeanId
        } else {
            s.beans.firstOrNull { it.name.equals(form.coffee.trim(), ignoreCase = true) }?.id
        }
        s.copy(form = form, errors = emptyList(), selectedBeanId = beanId)
    }

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
            journal?.saveNote(brew, brewId, s.selectedBeanId, s.liked, s.change)
            if (!s.isGuided) calc?.clearPlan()
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
