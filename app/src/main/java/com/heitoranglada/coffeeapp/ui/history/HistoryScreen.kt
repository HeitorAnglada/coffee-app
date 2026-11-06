package com.heitoranglada.coffeeapp.ui.history

import com.heitoranglada.coffeeapp.data.waterGrams
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.domain.formatDateTime
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.domain.formatRatio
import com.heitoranglada.coffeeapp.ui.components.BloomAnimation
import com.heitoranglada.coffeeapp.ui.components.GrinderAnimation
import com.heitoranglada.coffeeapp.ui.components.RatingBar
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.components.iconRes
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpenBrew: (Long) -> Unit,
    vm: HistoryViewModel = viewModel(factory = repoViewModelFactory { HistoryViewModel(it) }),
) {
    val brews by vm.brews.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("Histórico") }) }) { padding ->
        val list = brews
        when {
            list == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                GrinderAnimation(Modifier.size(120.dp))
            }
            list.isEmpty() -> Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BloomAnimation(Modifier.size(150.dp))
                Text(
                    "Nenhum preparo salvo ainda.\nOs preparos que você finalizar aparecem aqui.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(list, key = { _, it -> it.id }) { index, brew ->
                    BrewCard(brew, Modifier.enterCascade(index)) { onOpenBrew(brew.id) }
                }
            }
        }
    }
}

@Composable
private fun BrewCard(brew: Brew, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(brew.method.iconRes()), contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "${brew.method.label} · ${brew.recipeName ?: "Preparo livre"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(formatDuration(brew.totalTimeSeconds), style = MaterialTheme.typography.titleMedium)
            }
            Text(formatDateTime(brew.brewedAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                listOfNotNull(
                    brew.coffee.takeIf { it.isNotBlank() },
                    brew.doseGrams?.let { "${formatGrams(it)} → ${formatGrams(brew.waterGrams)}" },
                    formatRatio(brew.doseGrams, brew.waterGrams),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (brew.rating != null) RatingBar(brew.rating)
        }
    }
}
