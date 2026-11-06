package com.heitoranglada.coffeeapp

import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.CoffeeRepository
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.StepKind
import com.heitoranglada.coffeeapp.ui.components.withMethod
import com.heitoranglada.coffeeapp.ui.recipes.RecipeEditViewModel
import com.heitoranglada.coffeeapp.ui.CoffeeAppRoot
import com.heitoranglada.coffeeapp.ui.brew.BrewPhase
import com.heitoranglada.coffeeapp.ui.brew.BrewScreen
import com.heitoranglada.coffeeapp.ui.brew.BrewViewModel
import com.heitoranglada.coffeeapp.ui.history.BrewDetailScreen
import com.heitoranglada.coffeeapp.ui.history.HistoryScreen
import com.heitoranglada.coffeeapp.ui.recipes.RecipeDetailScreen
import com.heitoranglada.coffeeapp.ui.recipes.RecipeEditScreen
import com.heitoranglada.coffeeapp.ui.theme.CoffeeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

/**
 * Gera imagens das telas (em build/outputs/roborazzi) para conferir o visual sem um celular.
 * Não compara nada: só registra.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xxhdpi")
class ScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    private val app: CoffeeApplication get() = RuntimeEnvironment.getApplication() as CoffeeApplication
    private val repo: CoffeeRepository get() = app.repository

    private fun idleMain(time: Duration = Duration.ZERO) {
        if (time.isZero) shadowOf(Looper.getMainLooper()).idle() else shadowOf(Looper.getMainLooper()).idleFor(time)
    }

    private fun waitUntil(condition: () -> Boolean) {
        repeat(200) {
            if (condition()) return
            Thread.sleep(25)
            idleMain()
        }
    }

    private fun seeded(): List<Long> = runBlocking {
        withTimeout(15_000) {
            var list = repo.observeRecipes().first()
            while (list.isEmpty()) {
                delay(50)
                list = repo.observeRecipes().first()
            }
            list.map { it.id }
        }
    }

    private fun recipeId(method: BrewMethod): Long = runBlocking {
        seeded()
        repo.observeRecipes().first().first { it.method == method }.id
    }

    private fun show(name: String, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent { CoffeeTheme { content() } }
        repeat(40) {
            Thread.sleep(15)
            idleMain()
            rule.mainClock.advanceTimeBy(100)
        }
        rule.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    private fun readyVm(recipeId: Long): BrewViewModel {
        val vm = BrewViewModel(repo, recipeId)
        waitUntil { !vm.state.value.loading }
        return vm
    }

    @Test
    fun a01_receitas() {
        seeded()
        show("01-receitas") { CoffeeAppRoot() }
    }

    @Test
    fun a02_detalhe() {
        val id = recipeId(BrewMethod.V60)
        show("02-detalhe-receita") { RecipeDetailScreen(id, {}, {}, {}) }
    }

    @Test
    fun a03_nova_receita() {
        seeded()
        show("03-nova-receita") { RecipeEditScreen(0, 0, {}, {}) }
    }

    @Test
    fun a04_guiado_inicio() {
        val id = recipeId(BrewMethod.V60)
        val vm = readyVm(id)
        show("04-guiado-inicio") { BrewScreen(id, {}, {}, vm = vm) }
    }

    @Test
    fun a05_guiado_rodando() {
        val id = recipeId(BrewMethod.V60)
        val vm = readyVm(id)
        vm.start()
        idleMain(Duration.ofSeconds(50))
        show("05-guiado-rodando") { BrewScreen(id, {}, {}, vm = vm) }
    }

    @Test
    fun a06_livre_inicio() {
        seeded()
        val vm = readyVm(0)
        show("06-livre-inicio") { BrewScreen(0, null, {}, vm = vm) }
    }

    private fun freeBrewWithPours(): BrewViewModel {
        val vm = readyVm(0)
        vm.start()
        idleMain(Duration.ofSeconds(42))
        vm.markPour()
        idleMain(Duration.ofSeconds(33))
        vm.markPour()
        idleMain(Duration.ofSeconds(3))
        return vm
    }

    @Test
    fun a07_livre_rodando() {
        seeded()
        val vm = freeBrewWithPours()
        show("07-livre-rodando") { BrewScreen(0, null, {}, vm = vm) }
    }

    @Test
    fun a08_salvar() {
        seeded()
        val vm = freeBrewWithPours()
        vm.finish()
        waitUntil { vm.state.value.phase == BrewPhase.REVIEW }
        val form = vm.state.value.form
        vm.onFormChange(
            form.copy(
                pours = form.pours.mapIndexed { i, p -> if (i < 2) p.copy(gramsText = listOf("45", "105")[i]) else p },
                coffee = "Santa Inês",
                doseText = "15",
                grinder = "Comandante C40",
                grindSetting = "24 cliques",
                rating = 4,
            ),
        )
        show("08-salvar-preparo") { BrewScreen(0, null, {}, vm = vm) }
    }

    private fun savedBrews(): List<Long> = runBlocking {
        seeded()
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        listOf(
            Brew(recipeName = null, method = BrewMethod.V60, coffee = "Santa Inês", doseGrams = 15.0, pours = listOf(Pour(0, 45.0), Pour(42, 105.0), Pour(75, 100.0)), totalTimeSeconds = 178, rating = 4, brewedAt = now - 2 * 3_600_000L),
            Brew(recipeName = "V60 clássica", method = BrewMethod.V60, coffee = "Santa Inês", doseGrams = 15.0, pours = listOf(Pour(0, 45.0), Pour(45, 105.0), Pour(75, 100.0)), totalTimeSeconds = 185, rating = 3, brewedAt = now - day),
            Brew(recipeName = "Prensa francesa", method = BrewMethod.FRENCH_PRESS, coffee = "Cerrado", doseGrams = 30.0, pours = listOf(Pour(0, 500.0)), totalTimeSeconds = 250, rating = 5, brewedAt = now - 3 * day),
            Brew(recipeName = "Melitta do dia a dia", method = BrewMethod.MELITTA, coffee = "Mogiana", doseGrams = 20.0, pours = listOf(Pour(0, 60.0), Pour(40, 120.0), Pour(90, 120.0)), totalTimeSeconds = 232, rating = 4, brewedAt = now - 4 * day),
        ).map { repo.saveBrew(it) }
    }

    @Test
    fun a09_historico() {
        savedBrews()
        show("09-historico") { HistoryScreen({}) }
    }

    @Test
    fun a10_preparo_salvo() {
        val id = savedBrews().first()
        show("10-preparo-salvo") { BrewDetailScreen(id, {}, {}, {}) }
    }

    @Test
    fun a11_aeropress_detalhe() {
        val id = recipeNamed("AeroPress campeã")
        show("11-aeropress-detalhe") { RecipeDetailScreen(id, {}, {}, {}) }
    }

    @Test
    fun a12_aeropress_prensando() {
        val id = recipeNamed("AeroPress campeã")
        val vm = readyVm(id)
        vm.start()
        idleMain(Duration.ofSeconds(72))
        show("12-aeropress-prensando") { BrewScreen(id, {}, {}, vm = vm) }
    }

    @Test
    fun a13_aeropress_livre() {
        seeded()
        val vm = readyVm(0)
        vm.selectMethod(BrewMethod.AEROPRESS)
        vm.start()
        idleMain(Duration.ofSeconds(14))
        vm.mark(StepKind.STIR)
        idleMain(Duration.ofSeconds(38))
        vm.mark(StepKind.FLIP)
        idleMain(Duration.ofSeconds(12))
        show("13-aeropress-livre") { BrewScreen(0, null, {}, vm = vm) }
    }

    @Test
    fun a14_espresso_detalhe() {
        val id = recipeId(BrewMethod.ESPRESSO)
        show("14-espresso-detalhe") { RecipeDetailScreen(id, {}, {}, {}) }
    }

    @Test
    fun a15_espresso_extraindo() {
        val id = recipeId(BrewMethod.ESPRESSO)
        val vm = readyVm(id)
        vm.start()
        idleMain(Duration.ofMillis(27_400))
        show("15-espresso-extraindo") { BrewScreen(id, {}, {}, vm = vm) }
    }

    @Test
    fun a16_espresso_depois() {
        val id = recipeId(BrewMethod.ESPRESSO)
        runBlocking {
            val now = System.currentTimeMillis()
            repo.saveBrew(Brew(recipeName = "Espresso da casa", method = BrewMethod.ESPRESSO, doseGrams = 18.0, yieldGrams = 34.0, shotSeconds = 19.2, totalTimeSeconds = 19, brewedAt = now - 600_000))
            repo.saveBrew(Brew(recipeName = "Espresso da casa", method = BrewMethod.ESPRESSO, doseGrams = 18.0, yieldGrams = 36.0, shotSeconds = 24.1, totalTimeSeconds = 24, brewedAt = now - 300_000))
        }
        val vm = readyVm(id)
        vm.start()
        idleMain(Duration.ofMillis(27_400))
        vm.finish()
        waitUntil { vm.state.value.phase == BrewPhase.REVIEW }
        vm.onFormChange(vm.state.value.form.copy(yieldText = "38", coffee = "Mogiana", grinder = "Eureka Mignon", grindSetting = "2,4", rating = 4))
        show("16-espresso-depois") { BrewScreen(id, {}, {}, vm = vm) }
    }

    @Test
    fun a17_nova_receita_espresso() {
        seeded()
        val vm = RecipeEditViewModel(repo, 0, 0)
        waitUntil { !vm.state.value.loading }
        vm.onFormChange(vm.state.value.form.withMethod(BrewMethod.ESPRESSO).copy(name = "Espresso da casa", coffee = "Mogiana, torra média"))
        show("17-nova-receita-espresso") { RecipeEditScreen(0, 0, {}, {}, vm = vm) }
    }

    private fun recipeNamed(name: String): Long = runBlocking {
        seeded()
        repo.observeRecipes().first().first { it.name == name }.id
    }
}
