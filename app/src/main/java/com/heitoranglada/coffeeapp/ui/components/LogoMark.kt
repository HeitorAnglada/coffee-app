package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import com.heitoranglada.coffeeapp.R
import com.heitoranglada.coffeeapp.ui.theme.Bloom

/** Logo Bloombrew (o mesmo desenho do ícone do app) em um quadrado verde arredondado. */
@Composable
fun LogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(23))
            .background(Bloom.Green),
        contentAlignment = Alignment.Center,
    ) {
        // O vetor do ícone adaptativo tem margem de segurança; ampliamos para preencher.
        Image(painterResource(R.drawable.ic_launcher_foreground), contentDescription = "Bloombrew", modifier = Modifier.fillMaxSize().scale(1.5f))
    }
}
