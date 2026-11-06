package com.heitoranglada.coffeeapp.domain

/** O que acontece numa etapa do preparo. Só despejar e diluir põem água. */
enum class StepKind(val label: String, val addsWater: Boolean) {
    POUR("Despejo", true),
    STIR("Mexer", false),
    FLIP("Virar", false),
    PRESS("Prensar", false),
    DILUTE("Diluir", true),
}

/**
 * Uma etapa do preparo. Na maioria dos métodos é um despejo de água; na AeroPress também há
 * mexer, virar, prensar e diluir.
 *
 * @param startSeconds segundo do preparo em que a etapa começa.
 * @param grams água desta etapa (não acumulada); 0 nas etapas que não põem água.
 */
data class Pour(val startSeconds: Int, val grams: Double, val kind: StepKind = StepKind.POUR)

/** Soma da água de todas as etapas. */
fun List<Pour>.totalGrams(): Double = filter { it.kind.addsWater }.sumOf { it.grams }

/** Peso acumulado na balança ao final da etapa [index]. */
fun List<Pour>.cumulativeGramsAt(index: Int): Double = take(index + 1).totalGrams()

/** Só os despejos e diluições (as etapas com água). */
val List<Pour>.waterSteps: List<Pour> get() = filter { it.kind.addsWater }

/**
 * Serializa as etapas em texto compacto ("0@45.0;20@0.0@STIR") para guardar no banco
 * sem precisar de tabela separada. Despejos comuns não levam o tipo, então o formato
 * antigo continua valendo.
 */
object PourCodec {
    fun encode(pours: List<Pour>): String =
        pours.joinToString(";") {
            if (it.kind == StepKind.POUR) "${it.startSeconds}@${it.grams}" else "${it.startSeconds}@${it.grams}@${it.kind.name}"
        }

    fun decode(text: String?): List<Pour> {
        if (text.isNullOrBlank()) return emptyList()
        return text.split(";").mapNotNull { part ->
            val pieces = part.split("@")
            if (pieces.size !in 2..3) return@mapNotNull null
            val start = pieces[0].trim().toIntOrNull() ?: return@mapNotNull null
            val grams = pieces[1].trim().toDoubleOrNull() ?: return@mapNotNull null
            val kind = if (pieces.size == 3) {
                StepKind.entries.firstOrNull { it.name == pieces[2].trim() } ?: return@mapNotNull null
            } else {
                StepKind.POUR
            }
            Pour(start, grams, kind)
        }
    }
}
