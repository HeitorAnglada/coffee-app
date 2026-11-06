package com.heitoranglada.coffeeapp.domain

/** Métodos de preparo suportados no MVP. */
enum class BrewMethod(val label: String, val tip: String) {
    V60(
        label = "V60",
        tip = "Moagem média-fina. Comece com um bloom (2–3x o peso do café) e despeje em espiral.",
    ),
    MELITTA(
        label = "Melitta",
        tip = "Moagem média. Despeje devagar pelo centro, mantendo o nível de água constante.",
    ),
    FRENCH_PRESS(
        label = "Prensa francesa",
        tip = "Moagem grossa. Despeje toda a água, espere ~4 min, quebre a crosta e pressione devagar.",
    ),
}
