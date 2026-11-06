package com.heitoranglada.coffeeapp.data

import android.content.Context
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import kotlinx.coroutines.flow.Flow

/** Ponto único de acesso aos dados locais (tudo fica no aparelho). */
class CoffeeRepository(private val db: CoffeeDatabase) {
    private val recipes = db.recipeDao()
    private val brews = db.brewDao()

    fun observeRecipes(): Flow<List<Recipe>> = recipes.observeAll()
    fun observeRecipe(id: Long): Flow<Recipe?> = recipes.observe(id)
    suspend fun getRecipe(id: Long): Recipe? = recipes.get(id)

    /** Insere (id == 0) ou atualiza a receita. Retorna o id. */
    suspend fun saveRecipe(recipe: Recipe): Long =
        if (recipe.id == 0L) {
            recipes.insert(recipe)
        } else {
            recipes.update(recipe.copy(updatedAt = System.currentTimeMillis()))
            recipe.id
        }

    suspend fun deleteRecipe(recipe: Recipe) = recipes.delete(recipe)

    fun observeBrews(): Flow<List<Brew>> = brews.observeAll()
    fun observeBrew(id: Long): Flow<Brew?> = brews.observe(id)
    suspend fun getBrew(id: Long): Brew? = brews.get(id)
    suspend fun getLatestBrew(): Brew? = brews.getLatest()
    suspend fun saveBrew(brew: Brew): Long = brews.insert(brew)
    suspend fun deleteBrew(brew: Brew) = brews.delete(brew)

    fun observeCoffees(): Flow<List<String>> = brews.observeCoffees()
    fun observeGrinders(): Flow<List<String>> = brews.observeGrinders()

    /** Na primeira abertura, cria receitas-modelo para quem está começando. */
    suspend fun seedIfFirstRun(context: Context) {
        val prefs = context.getSharedPreferences("coffee_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        if (recipes.count() == 0) {
            starterRecipes().forEach { recipes.insert(it) }
        }
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    companion object {
        private const val KEY_SEEDED = "seeded_v1"

        fun starterRecipes(): List<Recipe> = listOf(
            Recipe(
                name = "V60 clássica",
                method = BrewMethod.V60,
                grindSetting = "Média-fina",
                doseGrams = 15.0,
                pours = listOf(Pour(0, 45.0), Pour(45, 105.0), Pour(75, 100.0)),
                waterTempC = 93,
                targetTimeSeconds = 180,
                notes = "Bloom de 45 s. Despeje em espiral, do centro para fora, sem atingir o papel.",
            ),
            Recipe(
                name = "Melitta do dia a dia",
                method = BrewMethod.MELITTA,
                grindSetting = "Média",
                doseGrams = 20.0,
                pours = listOf(Pour(0, 40.0), Pour(40, 130.0), Pour(90, 130.0)),
                waterTempC = 94,
                targetTimeSeconds = 240,
                notes = "Escalde o filtro antes. Despeje devagar pelo centro.",
            ),
            Recipe(
                name = "Prensa francesa básica",
                method = BrewMethod.FRENCH_PRESS,
                grindSetting = "Grossa",
                doseGrams = 30.0,
                pours = listOf(Pour(0, 500.0)),
                waterTempC = 94,
                targetTimeSeconds = 240,
                notes = "Aos 4:00, quebre a crosta, retire a espuma e pressione o êmbolo devagar.",
            ),
        )
    }
}
