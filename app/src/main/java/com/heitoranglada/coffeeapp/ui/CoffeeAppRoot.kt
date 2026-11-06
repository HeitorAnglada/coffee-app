package com.heitoranglada.coffeeapp.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.heitoranglada.coffeeapp.R
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.heitoranglada.coffeeapp.ui.brew.BrewScreen
import com.heitoranglada.coffeeapp.ui.history.BrewDetailScreen
import com.heitoranglada.coffeeapp.ui.history.HistoryScreen
import com.heitoranglada.coffeeapp.ui.recipes.RecipeDetailScreen
import com.heitoranglada.coffeeapp.ui.recipes.RecipeEditScreen
import com.heitoranglada.coffeeapp.ui.recipes.RecipeListScreen

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

private data class Tab(val route: String, val label: String, val icon: Int)

private val tabs = listOf(
    Tab(Routes.RECIPES, "Receitas", R.drawable.ic_bb_recipes),
    Tab(Routes.FREE_BREW, "Preparo livre", R.drawable.ic_bb_timer),
    Tab(Routes.HISTORY, "Histórico", R.drawable.ic_bb_history),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoffeeAppRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { nav.switchTab(tab.route) },
                            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = Routes.RECIPES,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
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
