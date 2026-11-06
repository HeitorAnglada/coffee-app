package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.heitoranglada.coffeeapp.domain.StepKind

val StepKind.icon: ImageVector
    get() = when (this) {
        StepKind.POUR -> Icons.Filled.WaterDrop
        StepKind.STIR -> Icons.Filled.Autorenew
        StepKind.FLIP -> Icons.Filled.SwapVert
        StepKind.PRESS -> Icons.Filled.VerticalAlignBottom
        StepKind.DILUTE -> Icons.Filled.Opacity
    }

/** Texto do botão de adicionar etapa ("despejo", "mexer"…). */
val StepKind.addLabel: String get() = label.lowercase()

/** O que a pessoa marcou no preparo livre ("despejei", "mexi"…). */
val StepKind.doneLabel: String
    get() = when (this) {
        StepKind.POUR -> "despejo"
        StepKind.STIR -> "mexeu"
        StepKind.FLIP -> "virou"
        StepKind.PRESS -> "prensou"
        StepKind.DILUTE -> "diluiu"
    }
