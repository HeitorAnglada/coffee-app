package com.heitoranglada.coffeeapp.data.journal

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.heitoranglada.coffeeapp.domain.BrewMethod

/**
 * Um café (grão) que a pessoa comprou, com as fotos do pacote.
 * As fotos são nomes de arquivo guardados na pasta privada do app (ver [PhotoStore]).
 */
@Entity(tableName = "beans")
data class Bean(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roaster: String = "",
    val origin: String = "",
    val variety: String = "",
    val process: String = "",
    val roast: String = "",
    val lot: String = "",
    /** Data da torra como a pessoa digitou (dd/mm/aaaa). */
    val roastDate: String = "",
    val flavorNotes: String = "",
    val photos: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Anotações do diário sobre um preparo: o que gostou e o que mudaria na próxima xícara.
 * Guarda uma cópia do café, método, moagem e nota do preparo para montar o lembrete sem
 * depender do banco de receitas e histórico.
 */
@Entity(tableName = "brew_notes", indices = [Index("beanId"), Index("coffee")])
data class BrewNote(
    /** Id do preparo no histórico. */
    @PrimaryKey val brewId: Long,
    val beanId: Long? = null,
    val coffee: String = "",
    val method: BrewMethod = BrewMethod.V60,
    val recipeName: String? = null,
    val grindSetting: String = "",
    val rating: Int? = null,
    val liked: String = "",
    val change: String = "",
    val createdAt: Long = System.currentTimeMillis(),
) {
    val hasText: Boolean get() = liked.isNotBlank() || change.isNotBlank()
}

class JournalConverters {
    @TypeConverter
    fun photosToString(photos: List<String>): String = photos.joinToString("\n")

    @TypeConverter
    fun stringToPhotos(value: String): List<String> = value.split("\n").filter { it.isNotBlank() }

    @TypeConverter
    fun methodToString(method: BrewMethod): String = method.name

    @TypeConverter
    fun stringToMethod(value: String): BrewMethod =
        BrewMethod.entries.firstOrNull { it.name == value } ?: BrewMethod.V60
}
