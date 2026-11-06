package com.heitoranglada.coffeeapp.ui.components

import androidx.annotation.DrawableRes
import com.heitoranglada.coffeeapp.R
import com.heitoranglada.coffeeapp.domain.BrewMethod

/** Ícone Bloombrew de cada método de preparo. */
@DrawableRes
fun BrewMethod.iconRes(): Int = when (this) {
    BrewMethod.V60 -> R.drawable.ic_bb_v60
    BrewMethod.MELITTA -> R.drawable.ic_bb_melitta
    BrewMethod.FRENCH_PRESS -> R.drawable.ic_bb_french_press
}
