package com.heitoranglada.coffeeapp.ui.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.R
import com.heitoranglada.coffeeapp.domain.CalcState
import com.heitoranglada.coffeeapp.domain.formatNumber
import com.heitoranglada.coffeeapp.domain.formatRatioValue
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType

/** Botão da calculadora no topo das telas. Com uma conta escolhida, mostra "1:16 · 15 g". */
@Composable
fun CalculatorPill(plan: CalcState?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(40.dp)
            .bouncyClick(onClickLabel = "Abrir calculadora", onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(if (plan != null) Bloom.Mustard else Bloom.Sand)
            .padding(horizontal = 14.dp)
            .semantics { role = Role.Button },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(R.drawable.ic_bb_calculator), null, tint = Bloom.Ink, modifier = Modifier.size(18.dp))
        AnimatedContent(
            plan?.let { "${formatRatioValue(it.ratio)} · ${formatNumber(it.coffeeGrams)} g" } ?: "Calculadora",
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.8f)).togetherWith(fadeOut()) },
            label = "calc-pill",
        ) { text ->
            Text(text, style = BloomType.chip.copy(fontSize = 14.sp), color = Bloom.Ink, maxLines = 1)
        }
    }
}
