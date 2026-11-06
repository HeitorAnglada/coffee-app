package com.heitoranglada.coffeeapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.ui.components.MethodTiles
import com.heitoranglada.coffeeapp.ui.theme.CoffeeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Interações de tela que já quebraram o app. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UiTest {
    @get:Rule
    val rule = createComposeRule()

    /** Trocar de método na nova receita fechava o app (a borda animada ficava negativa). */
    @Test
    fun trocarMetodoNaNovaReceita() {
        var chosen = BrewMethod.V60
        rule.setContent {
            CoffeeTheme {
                var selected by remember { mutableStateOf(BrewMethod.V60) }
                MethodTiles(selected) { selected = it; chosen = it }
            }
        }
        listOf("Melitta", "Prensa", "V60", "Prensa").forEach { label ->
            rule.onNodeWithText(label).performClick()
            rule.mainClock.advanceTimeBy(2_000)
            rule.waitForIdle()
        }
        assertEquals(BrewMethod.FRENCH_PRESS, chosen)
    }
}
