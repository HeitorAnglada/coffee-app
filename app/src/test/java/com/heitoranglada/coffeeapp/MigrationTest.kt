package com.heitoranglada.coffeeapp

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.heitoranglada.coffeeapp.data.CoffeeDatabase
import com.heitoranglada.coffeeapp.data.MIGRATION_1_2
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.Pour
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Quem atualiza o app não pode perder receitas nem preparos. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {
    @Test
    fun receitasEPreparosSobrevivemAAtualizacao() {
        val context = RuntimeEnvironment.getApplication()
        val file = context.getDatabasePath("migracao.db")
        file.parentFile?.mkdirs()
        file.delete()
        // Banco como a versão 1 do app criava.
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `recipes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `method` TEXT NOT NULL, `coffee` TEXT NOT NULL, `grinder` TEXT NOT NULL, `grindSetting` TEXT NOT NULL, `doseGrams` REAL, `pours` TEXT NOT NULL, `waterTempC` INTEGER, `targetTimeSeconds` INTEGER, `notes` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `brews` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recipeId` INTEGER, `recipeName` TEXT, `method` TEXT NOT NULL, `coffee` TEXT NOT NULL, `grinder` TEXT NOT NULL, `grindSetting` TEXT NOT NULL, `doseGrams` REAL, `pours` TEXT NOT NULL, `waterTempC` INTEGER, `totalTimeSeconds` INTEGER NOT NULL, `rating` INTEGER, `notes` TEXT NOT NULL, `brewedAt` INTEGER NOT NULL)")
            db.execSQL("INSERT INTO recipes VALUES (1, 'V60 clássica', 'V60', 'Santa Inês', 'C40', '24', 15.0, '0@45.0;45@105.0', 93, 180, '', 1, 1)")
            db.execSQL("INSERT INTO brews VALUES (1, 1, 'V60 clássica', 'V60', 'Santa Inês', 'C40', '24', 15.0, '0@45.0;45@105.0', 93, 178, 4, '', 1)")
            db.version = 1
        }

        val room = Room.databaseBuilder(context, CoffeeDatabase::class.java, "migracao.db")
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        runBlocking {
            val recipe = room.recipeDao().get(1)!!
            assertEquals("V60 clássica", recipe.name)
            assertEquals(BrewMethod.V60, recipe.method)
            assertEquals(listOf(Pour(0, 45.0), Pour(45, 105.0)), recipe.pours)
            assertFalse(recipe.inverted)
            assertNull(recipe.yieldGrams)
            val brew = room.brewDao().get(1)!!
            assertEquals(178, brew.totalTimeSeconds)
            assertNull(brew.shotSeconds)
        }
        room.close()
    }
}
