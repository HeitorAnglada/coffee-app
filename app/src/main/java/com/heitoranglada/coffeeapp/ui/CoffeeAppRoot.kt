package com.heitoranglada.coffeeapp.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.heitoranglada.coffeeapp.R
import com.heitoranglada.coffeeapp.ui.brew.BrewScreen
import com.heitoranglada.coffeeapp.ui.components.FloatingNavBar
import com.heitoranglada.coffeeapp.ui.components.NavTab
import com.heitoranglada.coffeeapp.ui.history.BrewDetailScreen
import com.heitoranglada.coffeeapp.ui.history.HistoryScreen
import com.heitoranglada.coffeeapp.ui.recipes.RecipeDetailScreen
import com.heitoranglada.coffeeapp.ui.recipes.RecipeEditScreen
import com.heitoranglada.coffeeapp.ui.recipes.RecipeListScreen
import com.heitoranglada.coffeeapp.ui.theme.Bloom

private object Routes {
    const val RECIPES = "recipes"
    const val FREE_BREW = "free-brew"
    const val HISTORY = "history"
    const val RECIPE_DETAIL = "recipe/{id}"
    const val RECIPE_EDIT = "recipe-edit?id={id}&fromBrew={fromBrew}"
    const val GUIDED_BREW = "brew/{recipeId}"
    const val BREW_DETAIL = "history/{id}"

    fun recipeDetail(id: Long) = "recipe/$id"
    fun recipeEdit(id: Long = 0, fromBrew: Long = 0) = "recipe-edit?id=$id&fromBrew=$fromBrew"
    fun guidedBrew(recipeId: Long) = "brew/$recipeId"
    fun brewDetail(id: Long) = "history/$id"
}

private val tabs = listOf(
    NavTab(Routes.RECIPES, "Receitas", R.drawable.ic_bb_recipes),
    NavTab(Routes.FREE_BREW, "Livre", R.drawable.ic_bb_timer),
    NavTab(Routes.HISTORY, "Histórico", R.drawable.ic_bb_history),
)
private val tabRoutes = tabs.map { it.route }.toSet()

private fun NavBackStackEntry.isTab() = destination.route in tabRoutes

/*
 * Transições: entre abas, a tela troca com um leve zoom; ao abrir um detalhe, ele sobe por cima
 * e a tela de trás afunda um pouco. Voltar faz o caminho inverso.
 */
private val springOffset = spring<IntOffset>(dampingRatio = 0.85f, stiffness = 380f)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enter(): EnterTransition =
    if (initialState.isTab() && targetState.isTab()) {
        fadeIn(tween(260, delayMillis = 60)) + scaleIn(tween(320, easing = FastOutSlowInEasing), initialScale = 0.96f)
    } else {
        slideInVertically(springOffset) { it / 5 } + fadeIn(tween(260))
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exit(): ExitTransition =
    if (initialState.isTab() && targetState.isTab()) {
        fadeOut(tween(160))
    } else {
        fadeOut(tween(220)) + scaleOut(tween(300), targetScale = 0.94f)
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnter(): EnterTransition =
    fadeIn(tween(260)) + scaleIn(tween(320, easing = FastOutSlowInEasing), initialScale = 0.94f)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
    slideOutVertically(tween(260)) { it / 5 } + fadeOut(tween(200))

@Composable
fun CoffeeAppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    var brewing by remember { mutableStateOf(false) }
    val showBar = currentRoute in tabRoutes && !brewing

    Box(Modifier.fillMaxSize().background(Bloom.Cream)) {
        NavHost(
            navController = nav,
            startDestination = Routes.RECIPES,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { enter() },
            exitTransition = { exit() },
            popEnterTransition = { popEnter() },
            popExitTransition = { popExit() },
        ) {
            composable(Routes.RECIPES) {
                RecipeListScreen(
                    onOpenRecipe = { nav.navigate(Routes.recipeDetail(it)) },
                    onBrewRecipe = { nav.navigate(Routes.guidedBrew(it)) },
                    onNewRecipe = { nav.navigate(Routes.recipeEdit()) },
                )
            }
            composable(Routes.FREE_BREW) {
                BrewScreen(
                    recipeId = 0,
                    onBack = null,
                    onSaved = { brewId -> nav.openSavedBrew(brewId) },
                    onImmersive = { brewing = it },
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onOpenBrew = { nav.navigate(Routes.brewDetail(it)) })
            }
            composable(
                Routes.RECIPE_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                RecipeDetailScreen(
                    recipeId = entry.arguments?.getLong("id") ?: 0,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate(Routes.recipeEdit(id = it)) },
                    onBrew = { nav.navigate(Routes.guidedBrew(it)) },
                )
            }
            composable(
                Routes.RECIPE_EDIT,
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("fromBrew") { type = NavType.LongType; defaultValue = 0L },
                ),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0
                RecipeEditScreen(
                    recipeId = id,
                    fromBrewId = entry.arguments?.getLong("fromBrew") ?: 0,
                    onBack = { nav.popBackStack() },
                    onSaved = { savedId ->
                        nav.popBackStack()
                        // Receita nova: abre o detalhe dela. Edição: já volta para o detalhe.
                        if (id == 0L) nav.navigate(Routes.recipeDetail(savedId))
                    },
                )
            }
            composable(
                Routes.GUIDED_BREW,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType }),
            ) { entry ->
                BrewScreen(
                    recipeId = entry.arguments?.getLong("recipeId") ?: 0,
                    onBack = { nav.popBackStack() },
                    onSaved = { brewId -> nav.openSavedBrew(brewId) },
                )
            }
            composable(
                Routes.BREW_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                BrewDetailScreen(
                    brewId = entry.arguments?.getLong("id") ?: 0,
                    onBack = { nav.popBackStack() },
                    onSaveAsRecipe = { nav.navigate(Routes.recipeEdit(fromBrew = it)) },
                    onBrewAgain = { nav.navigate(Routes.guidedBrew(it)) },
                )
            }
        }

        AnimatedVisibility(
            visible = showBar,
            enter = slideInVertically(spring(dampingRatio = 0.6f, stiffness = 300f)) { it * 2 } + fadeIn(),
            exit = slideOutVertically(tween(220)) { it * 2 } + fadeOut(tween(180)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp),
        ) {
            FloatingNavBar(
                tabs = tabs,
                current = currentRoute,
                onSelect = { nav.switchTab(it) },
                onAdd = { nav.navigate(Routes.recipeEdit()) },
            )
        }
    }
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Depois de salvar um preparo: vai para a aba Histórico e abre o preparo salvo. */
private fun NavHostController.openSavedBrew(brewId: Long) {
    navigate(Routes.HISTORY) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
    }
    navigate(Routes.brewDetail(brewId))
}
