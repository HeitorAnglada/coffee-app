package com.heitoranglada.coffeeapp.data.journal

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Banco do diário (grãos e anotações dos preparos). Fica num arquivo separado do banco de
 * receitas e histórico, com versões próprias, para as duas partes evoluírem sem se atrapalhar.
 */
@Database(entities = [Bean::class, BrewNote::class], version = 1, exportSchema = true)
@TypeConverters(JournalConverters::class)
abstract class JournalDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao

    companion object {
        fun create(context: Context): JournalDatabase =
            Room.databaseBuilder(context, JournalDatabase::class.java, "journal.db").build()
    }
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM beans ORDER BY updatedAt DESC")
    fun observeBeans(): Flow<List<Bean>>

    @Query("SELECT * FROM beans WHERE id = :id")
    fun observeBean(id: Long): Flow<Bean?>

    @Query("SELECT * FROM beans WHERE id = :id")
    suspend fun getBean(id: Long): Bean?

    @Query("SELECT * FROM beans WHERE name = :name COLLATE NOCASE ORDER BY updatedAt DESC LIMIT 1")
    suspend fun findBeanByName(name: String): Bean?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBean(bean: Bean): Long

    @Update
    suspend fun updateBean(bean: Bean)

    @Query("DELETE FROM beans WHERE id = :id")
    suspend fun deleteBean(id: Long)

    /** Ao apagar um grão, as anotações continuam no diário, só perdem o vínculo. */
    @Query("UPDATE brew_notes SET beanId = NULL WHERE beanId = :beanId")
    suspend fun unlinkNotes(beanId: Long)

    @Query("SELECT * FROM brew_notes ORDER BY createdAt DESC")
    fun observeNotes(): Flow<List<BrewNote>>

    @Query("SELECT * FROM brew_notes WHERE brewId = :brewId")
    fun observeNote(brewId: Long): Flow<BrewNote?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: BrewNote)

    @Query("DELETE FROM brew_notes WHERE brewId = :brewId")
    suspend fun deleteNote(brewId: Long)

    /** Última anotação com texto daquele grão (pelo vínculo ou, para preparos antigos, pelo nome do café). */
    @Query(
        "SELECT * FROM brew_notes WHERE (beanId = :beanId OR coffee = :name COLLATE NOCASE) " +
            "AND (liked != '' OR change != '') ORDER BY createdAt DESC LIMIT 1",
    )
    suspend fun latestNoteFor(beanId: Long, name: String): BrewNote?

    /** Última anotação com texto de um café que ainda não foi cadastrado como grão. */
    @Query(
        "SELECT * FROM brew_notes WHERE coffee = :name COLLATE NOCASE " +
            "AND (liked != '' OR change != '') ORDER BY createdAt DESC LIMIT 1",
    )
    suspend fun latestNoteForCoffee(name: String): BrewNote?
}
