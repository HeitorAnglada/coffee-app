package com.heitoranglada.coffeeapp.domain

/** Onde o preparo guiado está, dado o tempo decorrido. */
data class GuidedProgress(
    /** Índice do despejo atual (-1 se o primeiro ainda não começou). */
    val currentIndex: Int,
    /** Índice do próximo despejo, ou null se não houver. */
    val nextIndex: Int?,
    /** Segundos até o próximo despejo, ou null. */
    val secondsToNext: Int?,
)

fun guidedProgress(pours: List<Pour>, elapsedSeconds: Int): GuidedProgress {
    val ordered = pours.sortedBy { it.startSeconds }
    val current = ordered.indexOfLast { it.startSeconds <= elapsedSeconds }
    val next = (current + 1).takeIf { it < ordered.size }
    return GuidedProgress(
        currentIndex = current,
        nextIndex = next,
        secondsToNext = next?.let { ordered[it].startSeconds - elapsedSeconds },
    )
}
