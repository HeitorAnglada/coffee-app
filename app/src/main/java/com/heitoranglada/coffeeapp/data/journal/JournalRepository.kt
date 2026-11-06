package com.heitoranglada.coffeeapp.data.journal

import com.heitoranglada.coffeeapp.data.Brew
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/** O que o app lembra na hora de preparar um café: a última anotação daquele grão. */
data class BeanReminder(val coffee: String, val bean: Bean?, val note: BrewNote)

/** Diário de grãos e preparos (tudo no aparelho). */
class JournalRepository(db: JournalDatabase, val photos: PhotoStore) {
    private val dao = db.journalDao()

    fun observeBeans(): Flow<List<Bean>> = dao.observeBeans()
    fun observeBean(id: Long): Flow<Bean?> = dao.observeBean(id)
    suspend fun getBean(id: Long): Bean? = dao.getBean(id)
    suspend fun findBeanByName(name: String): Bean? = name.trim().takeIf { it.isNotEmpty() }?.let { dao.findBeanByName(it) }
    fun observeBeanNames(): Flow<List<String>> = dao.observeBeans().map { list -> list.map { it.name } }

    /** Insere (id == 0) ou atualiza o grão. Retorna o id. */
    suspend fun saveBean(bean: Bean): Long =
        if (bean.id == 0L) {
            dao.insertBean(bean)
        } else {
            dao.updateBean(bean.copy(updatedAt = System.currentTimeMillis()))
            bean.id
        }

    suspend fun deleteBean(bean: Bean) {
        dao.unlinkNotes(bean.id)
        dao.deleteBean(bean.id)
        bean.photos.forEach(photos::delete)
    }

    fun observeNotes(): Flow<List<BrewNote>> = dao.observeNotes()
    fun observeNote(brewId: Long): Flow<BrewNote?> = dao.observeNote(brewId)

    /** Guarda as anotações do preparo (ou apaga, se a pessoa não escreveu nada e não escolheu grão). */
    suspend fun saveNote(brew: Brew, brewId: Long, beanId: Long?, liked: String, change: String) {
        if (beanId == null && liked.isBlank() && change.isBlank()) return
        dao.upsertNote(
            BrewNote(
                brewId = brewId,
                beanId = beanId,
                coffee = brew.coffee.trim(),
                method = brew.method,
                recipeName = brew.recipeName,
                grindSetting = brew.grindSetting,
                rating = brew.rating,
                liked = liked.trim(),
                change = change.trim(),
                createdAt = brew.brewedAt,
            ),
        )
        // O grão usado sobe para o topo da lista.
        if (beanId != null) dao.getBean(beanId)?.let { dao.updateBean(it.copy(updatedAt = System.currentTimeMillis())) }
    }

    suspend fun deleteNote(brewId: Long) = dao.deleteNote(brewId)

    /** Lembrete para o próximo preparo de um grão já cadastrado. */
    suspend fun reminderFor(bean: Bean): BeanReminder? =
        dao.latestNoteFor(bean.id, bean.name)?.let { BeanReminder(bean.name, bean, it) }

    /** Lembrete pelo nome do café (campo "café" da receita ou do preparo). */
    suspend fun reminderForCoffee(coffee: String): BeanReminder? {
        val name = coffee.trim()
        if (name.isEmpty()) return null
        val bean = dao.findBeanByName(name)
        return if (bean != null) reminderFor(bean) else dao.latestNoteForCoffee(name)?.let { BeanReminder(name, null, it) }
    }

    /** Grão escolhido em "Preparar este café", à espera do preparo livre. */
    private val _pendingBean = MutableStateFlow<Long?>(null)
    val pendingBean: StateFlow<Long?> = _pendingBean.asStateFlow()
    fun requestBrewWith(beanId: Long) { _pendingBean.value = beanId }
    fun consumePendingBean() { _pendingBean.value = null }

    companion object {
        private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).apply { isLenient = false }

        /** Dias desde a torra, se a data foi digitada como dd/mm/aaaa (ou dd/mm/aa). */
        fun daysSinceRoast(roastDate: String, now: Long = System.currentTimeMillis()): Int? {
            val text = roastDate.trim().let { t ->
                val parts = t.split("/")
                if (parts.size == 3 && parts[2].length == 2) "${parts[0]}/${parts[1]}/20${parts[2]}" else t
            }
            if (text.isEmpty()) return null
            val date = runCatching { synchronized(dateFmt) { dateFmt.parse(text) } }.getOrNull() ?: return null
            val days = TimeUnit.MILLISECONDS.toDays(now - date.time).toInt()
            return days.takeIf { it >= 0 }
        }

        fun roastAgeLabel(roastDate: String, now: Long = System.currentTimeMillis()): String? =
            daysSinceRoast(roastDate, now)?.let {
                when (it) {
                    0 -> "torrado hoje"
                    1 -> "1 dia"
                    else -> "$it dias"
                }
            }
    }
}
