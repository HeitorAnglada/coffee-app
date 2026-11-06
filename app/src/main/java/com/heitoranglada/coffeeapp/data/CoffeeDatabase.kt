package com.heitoranglada.coffeeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Recipe::class, Brew::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class CoffeeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun brewDao(): BrewDao

    companion object {
        fun create(context: Context): CoffeeDatabase =
            Room.databaseBuilder(context, CoffeeDatabase::class.java, "coffee.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

/** v2: AeroPress e espresso (posição, rendimento, janela de tempo). Nada do que já existe muda. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE recipes ADD COLUMN inverted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE recipes ADD COLUMN yieldGrams REAL")
        db.execSQL("ALTER TABLE recipes ADD COLUMN windowMinSeconds INTEGER")
        db.execSQL("ALTER TABLE recipes ADD COLUMN windowMaxSeconds INTEGER")
        db.execSQL("ALTER TABLE recipes ADD COLUMN preInfusionSeconds INTEGER")
        db.execSQL("ALTER TABLE brews ADD COLUMN yieldGrams REAL")
        db.execSQL("ALTER TABLE brews ADD COLUMN shotSeconds REAL")
    }
}
