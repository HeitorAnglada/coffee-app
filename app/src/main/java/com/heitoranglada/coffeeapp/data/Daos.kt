package com.heitoranglada.coffeeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.heitoranglada.coffeeapp.domain.BrewMethod
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observe(id: Long): Flow<Recipe?>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun get(id: Long): Recipe?

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)
}

@Dao
interface BrewDao {
    @Query("SELECT * FROM brews ORDER BY brewedAt DESC")
    fun observeAll(): Flow<List<Brew>>

    @Query("SELECT * FROM brews WHERE id = :id")
    fun observe(id: Long): Flow<Brew?>

    @Query("SELECT * FROM brews WHERE id = :id")
    suspend fun get(id: Long): Brew?

    @Query("SELECT * FROM brews ORDER BY brewedAt DESC LIMIT 1")
    suspend fun getLatest(): Brew?

    @Query("SELECT * FROM brews WHERE method = :method ORDER BY brewedAt DESC LIMIT 1")
    suspend fun getLatest(method: BrewMethod): Brew?

    @Query("SELECT * FROM brews WHERE brewedAt >= :since AND method = :method ORDER BY brewedAt ASC")
    suspend fun since(since: Long, method: BrewMethod): List<Brew>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(brew: Brew): Long

    @Delete
    suspend fun delete(brew: Brew)

    /** Valores já usados, para sugerir ao digitar café/moedor. */
    @Query("SELECT DISTINCT coffee FROM brews WHERE coffee != '' UNION SELECT DISTINCT coffee FROM recipes WHERE coffee != ''")
    fun observeCoffees(): Flow<List<String>>

    @Query("SELECT DISTINCT grinder FROM brews WHERE grinder != '' UNION SELECT DISTINCT grinder FROM recipes WHERE grinder != ''")
    fun observeGrinders(): Flow<List<String>>
}
