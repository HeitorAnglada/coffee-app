package com.heitoranglada.coffeeapp.domain

/** Métodos de preparo suportados. */
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
    AEROPRESS(
        label = "AeroPress",
        tip = "Moagem média-fina. Infusão de 1 a 2 min, mexa, e prense devagar até o primeiro chiado.",
    ),
    ESPRESSO(
        label = "Espresso",
        tip = "Moagem fina. Pese a dose e o que sai na xícara: o clássico é o dobro, em 25 a 30 s.",
    ),
    ;

    /** Nome curto para chips e títulos. */
    val shortLabel: String get() = if (this == FRENCH_PRESS) "Prensa" else label

    /** Espresso não tem despejos: a receita é dose, rendimento e janela de tempo. */
    val isEspresso: Boolean get() = this == ESPRESSO
}
