package com.heitoranglada.coffeeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import com.heitoranglada.coffeeapp.domain.totalGrams

/** Receita cadastrada (com antecedência ou criada a partir de um preparo). */
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val method: BrewMethod,
    val coffee: String = "",
    val grinder: String = "",
    val grindSetting: String = "",
    val doseGrams: Double? = null,
    val pours: List<Pour> = emptyList(),
    val waterTempC: Int? = null,
    val targetTimeSeconds: Int? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Um preparo realizado, salvo no histórico. */
@Entity(tableName = "brews")
data class Brew(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Receita usada no preparo guiado, ou null para preparo livre. */
    val recipeId: Long? = null,
    val recipeName: String? = null,
    val method: BrewMethod,
    val coffee: String = "",
    val grinder: String = "",
    val grindSetting: String = "",
    val doseGrams: Double? = null,
    val pours: List<Pour> = emptyList(),
    val waterTempC: Int? = null,
    val totalTimeSeconds: Int = 0,
    /** Nota de 1 a 5, ou null. */
    val rating: Int? = null,
    val notes: String = "",
    val brewedAt: Long = System.currentTimeMillis(),
)

/** Água total da receita (soma dos despejos). Fica fora da classe para o Room não tentar persistir. */
val Recipe.waterGrams: Double get() = pours.totalGrams()

/** Água total do preparo (soma dos despejos). */
val Brew.waterGrams: Double get() = pours.totalGrams()
