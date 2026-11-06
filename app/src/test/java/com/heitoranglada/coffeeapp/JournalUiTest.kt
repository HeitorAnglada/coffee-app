package com.heitoranglada.coffeeapp

import androidx.room.Room
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.data.journal.JournalDatabase
import com.heitoranglada.coffeeapp.data.journal.JournalRepository
import com.heitoranglada.coffeeapp.data.journal.PhotoStore
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.ui.journal.brewsOfBean
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Diário de grãos: anotações dos preparos e o lembrete do próximo preparo. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JournalUiTest {
    private lateinit var db: JournalDatabase
    private lateinit var journal: JournalRepository

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, JournalDatabase::class.java).allowMainThreadQueries().build()
        journal = JournalRepository(db, PhotoStore(context))
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun lembreteDoGraoMostraOQueMudar() = runBlocking {
        val beanId = journal.saveBean(Bean(name = "Santa Inês", origin = "Sul de Minas"))
        val bean = journal.getBean(beanId)!!
        assertNull(journal.reminderFor(bean))

        val old = Brew(method = BrewMethod.V60, coffee = "Santa Inês", rating = 3, brewedAt = 1_000)
        journal.saveNote(old, 1, beanId, liked = "corpo", change = "moer mais grosso")
        val recent = Brew(method = BrewMethod.FRENCH_PRESS, coffee = "Santa Inês", rating = 4, grindSetting = "24 cliques", brewedAt = 2_000)
        journal.saveNote(recent, 2, beanId, liked = "doçura", change = "moer 2 cliques mais fino")

        val reminder = journal.reminderFor(bean)
        assertNotNull(reminder)
        assertEquals("moer 2 cliques mais fino", reminder!!.note.change)
        assertEquals(BrewMethod.FRENCH_PRESS, reminder.note.method)
        // Pelo nome do café (ex.: receita com "café" preenchido), sem diferenciar maiúsculas.
        assertEquals("moer 2 cliques mais fino", journal.reminderForCoffee("santa inês")?.note?.change)
    }

    @Test
    fun cafeSemGraoCadastradoTambemLembra() = runBlocking {
        val brew = Brew(method = BrewMethod.MELITTA, coffee = "Mogiana", brewedAt = 5_000)
        journal.saveNote(brew, 7, null, liked = "", change = "menos água")
        assertEquals("menos água", journal.reminderForCoffee("Mogiana")?.note?.change)
        assertNull(journal.reminderForCoffee("Cerrado"))
    }

    @Test
    fun preparoSemNadaEscritoNaoCriaAnotacao() = runBlocking {
        journal.saveNote(Brew(method = BrewMethod.V60, coffee = "X"), 3, null, liked = " ", change = "")
        assertEquals(0, journal.observeNotes().first().size)
    }

    @Test
    fun excluirGraoMantemAsAnotacoes() = runBlocking {
        val beanId = journal.saveBean(Bean(name = "Pedra Azul"))
        journal.saveNote(Brew(method = BrewMethod.V60, coffee = "Pedra Azul"), 4, beanId, liked = "floral", change = "")
        journal.deleteBean(journal.getBean(beanId)!!)
        val notes = journal.observeNotes().first()
        assertEquals(1, notes.size)
        assertNull(notes.first().beanId)
    }

    @Test
    fun preparosDoGraoIncluemOsAntigosPeloNome() = runBlocking {
        val beanId = journal.saveBean(Bean(name = "Santa Inês"))
        val bean = journal.getBean(beanId)!!
        val brews = listOf(
            Brew(id = 1, method = BrewMethod.V60, coffee = "Santa Inês", brewedAt = 1),
            Brew(id = 2, method = BrewMethod.V60, coffee = "Outro", brewedAt = 2),
            Brew(id = 3, method = BrewMethod.V60, coffee = "santa inês ", brewedAt = 3),
        )
        journal.saveNote(brews[1], 2, beanId, liked = "", change = "ligado pelo diário")
        val entries = brewsOfBean(bean, brews, journal.observeNotes().first())
        assertEquals(listOf(3L, 2L, 1L), entries.map { it.brew.id })
    }
}
