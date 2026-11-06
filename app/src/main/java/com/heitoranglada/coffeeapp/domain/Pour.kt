package com.heitoranglada.coffeeapp.domain

/**
 * Um despejo de água.
 *
 * @param startSeconds segundo do preparo em que o despejo começa.
 * @param grams quantidade de água deste despejo (não acumulada).
 */
data class Pour(val startSeconds: Int, val grams: Double)

/** Soma das gramas de todos os despejos. */
fun List<Pour>.totalGrams(): Double = sumOf { it.grams }

/** Peso acumulado na balança ao final do despejo [index]. */
fun List<Pour>.cumulativeGramsAt(index: Int): Double = take(index + 1).sumOf { it.grams }

/**
 * Serializa a lista de despejos em texto compacto ("0@45.0;45@105.0") para guardar no banco
 * sem precisar de tabela separada.
 */
object PourCodec {
    fun encode(pours: List<Pour>): String =
        pours.joinToString(";") { "${it.startSeconds}@${it.grams}" }

    fun decode(text: String?): List<Pour> {
        if (text.isNullOrBlank()) return emptyList()
        return text.split(";").mapNotNull { part ->
            val pieces = part.split("@")
            if (pieces.size != 2) return@mapNotNull null
            val start = pieces[0].trim().toIntOrNull() ?: return@mapNotNull null
            val grams = pieces[1].trim().toDoubleOrNull() ?: return@mapNotNull null
            Pour(start, grams)
        }
    }
}
