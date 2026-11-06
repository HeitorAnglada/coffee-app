package com.heitoranglada.coffeeapp.ui.journal

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heitoranglada.coffeeapp.CoffeeApplication
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.data.journal.BrewNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Um preparo daquele grão, com as anotações do diário (se houver). */
data class BeanBrewEntry(val brew: Brew, val note: BrewNote?)

/** Cartão do grão na lista: média das notas e o último "mudaria". */
data class BeanCard(val bean: Bean, val brewCount: Int, val averageRating: Int?, val nextTime: String?)

/** Preparos que pertencem ao grão: os ligados pelo diário e os antigos com o mesmo nome de café. */
fun brewsOfBean(bean: Bean, brews: List<Brew>, notes: List<BrewNote>): List<BeanBrewEntry> {
    val byBrew = notes.associateBy { it.brewId }
    return brews
        .filter { b -> byBrew[b.id]?.beanId == bean.id || (byBrew[b.id]?.beanId == null && b.coffee.trim().equals(bean.name.trim(), ignoreCase = true)) }
        .sortedByDescending { it.brewedAt }
        .map { BeanBrewEntry(it, byBrew[it.id]) }
}

class BeansViewModel(app: CoffeeApplication) : ViewModel() {
    val cards: StateFlow<List<BeanCard>?> = combine(
        app.journal.observeBeans(),
        app.journal.observeNotes(),
        app.repository.observeBrews(),
    ) { beans, notes, brews ->
        beans.map { bean ->
            val entries = brewsOfBean(bean, brews, notes)
            val ratings = entries.mapNotNull { it.brew.rating }
            BeanCard(
                bean = bean,
                brewCount = entries.size,
                averageRating = if (ratings.isEmpty()) null else Math.round(ratings.average()).toInt(),
                nextTime = entries.firstNotNullOfOrNull { it.note?.change?.takeIf(String::isNotBlank) },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

class BeanDetailViewModel(private val app: CoffeeApplication, beanId: Long) : ViewModel() {
    val bean: StateFlow<Bean?> = app.journal.observeBean(beanId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    val entries: StateFlow<List<BeanBrewEntry>> = combine(
        app.journal.observeBean(beanId),
        app.journal.observeNotes(),
        app.repository.observeBrews(),
    ) { b, notes, brews ->
        _loaded.value = true
        if (b == null) emptyList() else brewsOfBean(b, brews, notes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Pede ao preparo livre para começar com este grão. */
    fun brewWithThis() {
        bean.value?.let { app.journal.requestBrewWith(it.id) }
    }

    fun delete(onDone: () -> Unit) {
        val b = bean.value ?: return
        viewModelScope.launch {
            app.journal.deleteBean(b)
            onDone()
        }
    }
}

/** Formulário do grão. */
data class BeanForm(
    val name: String = "",
    val roaster: String = "",
    val origin: String = "",
    val variety: String = "",
    val process: String = "",
    val roast: String = "",
    val lot: String = "",
    val roastDate: String = "",
    val flavorNotes: String = "",
    val photos: List<String> = emptyList(),
)

data class BeanEditState(
    val loading: Boolean = true,
    val form: BeanForm = BeanForm(),
    val importing: Boolean = false,
    val errors: List<String> = emptyList(),
    val savedId: Long? = null,
)

class BeanEditViewModel(private val app: CoffeeApplication, private val beanId: Long) : ViewModel() {
    private val _state = MutableStateFlow(BeanEditState())
    val state: StateFlow<BeanEditState> = _state.asStateFlow()

    private var original: Bean? = null
    /** Fotos adicionadas nesta edição: se a pessoa sair sem salvar, são apagadas. */
    private val addedPhotos = mutableSetOf<String>()
    /** Fotos removidas nesta edição: só são apagadas de verdade ao salvar. */
    private val removedPhotos = mutableSetOf<String>()

    val photos get() = app.journal.photos

    init {
        viewModelScope.launch {
            val bean = if (beanId != 0L) app.journal.getBean(beanId) else null
            original = bean
            _state.update {
                it.copy(
                    loading = false,
                    form = bean?.let { b ->
                        BeanForm(b.name, b.roaster, b.origin, b.variety, b.process, b.roast, b.lot, b.roastDate, b.flavorNotes, b.photos)
                    } ?: BeanForm(),
                )
            }
        }
    }

    fun onChange(form: BeanForm) = _state.update { it.copy(form = form, errors = emptyList()) }

    fun addPhoto(uri: Uri, onDone: () -> Unit = {}) {
        _state.update { it.copy(importing = true) }
        viewModelScope.launch {
            val name = photos.import(uri)
            if (name != null) addedPhotos += name
            _state.update { s ->
                s.copy(importing = false, form = if (name != null) s.form.copy(photos = s.form.photos + name) else s.form)
            }
            onDone()
        }
    }

    fun removePhoto(name: String) {
        if (name in addedPhotos) {
            addedPhotos -= name
            photos.delete(name)
        } else {
            removedPhotos += name
        }
        _state.update { it.copy(form = it.form.copy(photos = it.form.photos - name)) }
    }

    fun save() {
        val f = _state.value.form
        if (f.name.isBlank()) {
            _state.update { it.copy(errors = listOf("Dê um nome ao café.")) }
            return
        }
        val base = original ?: Bean(name = "")
        val bean = base.copy(
            name = f.name.trim(),
            roaster = f.roaster.trim(),
            origin = f.origin.trim(),
            variety = f.variety.trim(),
            process = f.process.trim(),
            roast = f.roast.trim(),
            lot = f.lot.trim(),
            roastDate = f.roastDate.trim(),
            flavorNotes = f.flavorNotes.trim(),
            photos = f.photos,
        )
        viewModelScope.launch {
            val id = app.journal.saveBean(bean)
            removedPhotos.forEach(photos::delete)
            removedPhotos.clear()
            addedPhotos.clear()
            _state.update { it.copy(savedId = id) }
        }
    }

    override fun onCleared() {
        // Saiu sem salvar: as fotos novas não ficam ocupando espaço.
        addedPhotos.forEach(photos::delete)
        super.onCleared()
    }
}
