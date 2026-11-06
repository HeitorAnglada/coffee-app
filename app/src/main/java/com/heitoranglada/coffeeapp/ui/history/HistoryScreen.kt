package com.heitoranglada.coffeeapp.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.Brew
import com.heitoranglada.coffeeapp.data.waterGrams
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.domain.formatDuration
import com.heitoranglada.coffeeapp.domain.formatGrams
import com.heitoranglada.coffeeapp.ui.components.Blob
import com.heitoranglada.coffeeapp.ui.components.MethodSilhouette
import com.heitoranglada.coffeeapp.ui.components.MiniBeans
import com.heitoranglada.coffeeapp.ui.components.Wordmark
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.components.popIn
import com.heitoranglada.coffeeapp.ui.recipes.FloatingNavSpace
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import com.heitoranglada.coffeeapp.ui.theme.palette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onOpenBrew: (Long) -> Unit,
    vm: HistoryViewModel = viewModel(factory = repoViewModelFactory { HistoryViewModel(it) }),
) {
    val brews by vm.brews.collectAsStateWithLifecycle()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val list = brews

    LazyColumn(
        Modifier.fillMaxSize().background(Bloom.Cream),
        contentPadding = PaddingValues(bottom = FloatingNavSpace + navBottom),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "header") {
            Column(Modifier.statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 6.dp)) {
                Wordmark(Modifier.enterCascade(0))
                Text(
                    buildAnnotatedString {
                        append("Seus ")
                        withStyle(SpanStyle(color = Bloom.Terra)) { append("preparos") }
                    },
                    style = BloomType.display(44.sp, -0.035f),
                    color = Bloom.Ink,
                    modifier = Modifier.padding(top = 12.dp).enterCascade(1),
                )
                WeekStrip(list.orEmpty(), Modifier.padding(top = 16.dp).enterCascade(2))
            }
        }
        when {
            list == null -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Blob(Bloom.Terra, Modifier.size(110.dp), periodMs = 2000, wobble = 0.12f)
                }
            }
            list.isEmpty() -> item(key = "empty") {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Blob(Bloom.Terra, Modifier.size(150.dp).enterCascade(0)) {
                        MethodSilhouette(BrewMethod.V60, Bloom.Ink, Modifier.size(70.dp))
                    }
                    Text("Nenhum preparo ainda", style = BloomType.display(22.sp, -0.02f), color = Bloom.Ink, textAlign = TextAlign.Center)
                    Text(
                        "Faça um preparo guiado ou livre e salve no final: ele aparece aqui.",
                        style = BloomType.body,
                        color = Bloom.Body,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> itemsIndexed(list, key = { _, b -> b.id }) { index, brew ->
                BrewRow(
                    brew,
                    onClick = { onOpenBrew(brew.id) },
                    modifier = Modifier.animateItem().enterCascade(index + 3),
                )
            }
        }
    }
}

/** Os últimos 7 dias, com uma bolinha na cor do método de cada preparo. Hoje fica destacado. */
@Composable
private fun WeekStrip(brews: List<Brew>, modifier: Modifier = Modifier) {
    val days = remember(brews) {
        val cal = Calendar.getInstance()
        (6 downTo 0).map { back ->
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -back)
            val y = c.get(Calendar.YEAR)
            val d = c.get(Calendar.DAY_OF_YEAR)
            val methods = brews.filter {
                val bc = Calendar.getInstance().apply { timeInMillis = it.brewedAt }
                bc.get(Calendar.YEAR) == y && bc.get(Calendar.DAY_OF_YEAR) == d
            }.map { it.method }
            val letter = when (c.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "D"
                Calendar.MONDAY -> "S"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "Q"
                Calendar.THURSDAY -> "Q"
                Calendar.FRIDAY -> "S"
                else -> "S"
            }
            Triple(letter, methods, back == 0)
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Bloom.Sand)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        days.forEachIndexed { i, (letter, methods, today) ->
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (today) Bloom.Ink else Bloom.Sand)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(letter, style = BloomType.chip.copy(fontSize = 12.sp), color = if (today) Bloom.Cream else Bloom.Muted)
                Column(Modifier.height(30.dp), verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Bottom)) {
                    methods.take(3).forEachIndexed { k, m ->
                        Box(
                            Modifier
                                .size(12.dp)
                                .popIn(200L + i * 60L + k * 80L)
                                .clip(CircleShape)
                                .background(if (today && (m == BrewMethod.FRENCH_PRESS || m == BrewMethod.ESPRESSO)) Bloom.Cream else m.palette.bg),
                        )
                    }
                }
            }
        }
    }
}

private val timeFmt = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR"))
private val dateFmt = SimpleDateFormat("dd/MM", Locale.forLanguageTag("pt-BR"))

/** "Hoje, 08:12", "Ontem, 07:48", "Dom, 16:30" ou "02/11, 09:05". */
private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

fun relativeWhen(millis: Long): String {
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    val dayDiff = Math.round((startOfDay(System.currentTimeMillis()) - startOfDay(millis)) / 86_400_000.0).toInt()
    val day = when (dayDiff) {
        0 -> "Hoje"
        1 -> "Ontem"
        in 2..6 -> listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")[then.get(Calendar.DAY_OF_WEEK) - 1]
        else -> dateFmt.format(Date(millis))
    }
    return "$day, ${timeFmt.format(Date(millis))}"
}

@Composable
private fun BrewRow(brew: Brew, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = brew.method.palette
    Row(
        modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .bouncyClick(pressedScale = 0.97f, onClickLabel = "Abrir preparo", onClick = onClick)
            .clip(RoundedCornerShape(24.dp))
            .background(Bloom.White)
            .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(p.bg),
            contentAlignment = Alignment.Center,
        ) {
            MethodSilhouette(brew.method, p.fg, Modifier.size(38.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                brew.recipeName ?: "Preparo livre",
                style = BloomType.display(17.sp, -0.01f),
                color = Bloom.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val out = if (brew.method.isEspresso) brew.yieldGrams else brew.waterGrams.takeIf { it > 0 }
            val amounts = listOfNotNull(brew.doseGrams?.let(::formatGrams), out?.let(::formatGrams)).joinToString(" → ")
            Text(
                listOf(relativeWhen(brew.brewedAt), amounts).filter { it.isNotBlank() }.joinToString(" · "),
                style = BloomType.bodySmall.copy(fontSize = 12.sp),
                color = Bloom.Muted,
                maxLines = 1,
                modifier = Modifier.padding(top = 3.dp),
            )
            if (brew.rating != null) MiniBeans(brew.rating, Modifier.padding(top = 6.dp))
        }
        Text(
            if (brew.method.isEspresso) "${(brew.shotSeconds ?: brew.totalTimeSeconds.toDouble()).toInt()} s" else formatDuration(brew.totalTimeSeconds),
            style = BloomType.display(24.sp, -0.02f),
            color = Bloom.Ink,
        )
    }
}
