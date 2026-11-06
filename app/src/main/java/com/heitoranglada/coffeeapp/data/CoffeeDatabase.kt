package com.heitoranglada.coffeeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Recipe::class, Brew::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class CoffeeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun brewDao(): BrewDao

    companion object {
        fun create(context: Context): CoffeeDatabase =
            Room.databaseBuilder(context, CoffeeDatabase::class.java, "coffee.db").build()
    }
}
