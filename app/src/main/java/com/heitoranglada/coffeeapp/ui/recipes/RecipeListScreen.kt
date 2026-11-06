package com.heitoranglada.coffeeapp.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.Recipe
import com.heitoranglada.coffeeapp.domain.BrewMethod
import com.heitoranglada.coffeeapp.ui.components.BigPillButton
import com.heitoranglada.coffeeapp.ui.components.Blob
import com.heitoranglada.coffeeapp.ui.components.MethodChips
import com.heitoranglada.coffeeapp.ui.components.MethodSilhouette
import com.heitoranglada.coffeeapp.ui.components.Pill
import com.heitoranglada.coffeeapp.ui.components.shortPills
import com.heitoranglada.coffeeapp.ui.components.Wordmark
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.components.overlapNext
import com.heitoranglada.coffeeapp.ui.components.sway
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import com.heitoranglada.coffeeapp.ui.theme.palette

/** Espaço no fim das listas para a barra de navegação flutuante. */
val FloatingNavSpace = 110.dp

@Composable
fun RecipeListScreen(
    onOpenRecipe: (Long) -> Unit,
    onBrewRecipe: (Long) -> Unit,
    onNewRecipe: () -> Unit,
    vm: RecipeListViewModel = viewModel(factory = repoViewModelFactory { RecipeListViewModel(it) }),
) {
    val recipes by vm.recipes.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<BrewMethod?>(null) }
    val shown = recipes?.filter { filter == null || it.method == filter }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        Modifier.fillMaxSize().background(Bloom.Cream),
        contentPadding = PaddingValues(bottom = FloatingNavSpace + navBottom),
    ) {
        item(key = "header") {
            Column(Modifier.statusBarsPadding().padding(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 18.dp)) {
                Wordmark(Modifier.enterCascade(0))
                Text(
                    buildAnnotatedString {
                        append("O que vamos\npassar ")
                        withStyle(SpanStyle(color = Bloom.Terra)) { append("hoje?") }
                    },
                    style = BloomType.display(44.sp, -0.035f),
                    color = Bloom.Ink,
                    modifier = Modifier.padding(top = 12.dp).enterCascade(1),
                )
                MethodChips(filter, { filter = it }, Modifier.padding(top = 18.dp).enterCascade(2), showAll = true)
            }
        }
        when {
            shown == null -> item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Blob(Bloom.Terra, Modifier.size(120.dp), periodMs = 2000, wobble = 0.12f)
                }
            }
            shown.isEmpty() -> item(key = "empty") {
                EmptyRecipes(filtered = filter != null, onNewRecipe = onNewRecipe)
            }
            else -> itemsIndexed(shown, key = { _, r -> r.id }) { index, recipe ->
                RecipeStackCard(
                    recipe = recipe,
                    isLast = index == shown.lastIndex,
                    onOpen = { onOpenRecipe(recipe.id) },
                    onBrew = { onBrewRecipe(recipe.id) },
                    modifier = Modifier
                        .animateItem()
                        .enterCascade(index + 2, distance = 60.dp),
                )
            }
        }
    }
}

/** Cartão colorido do método. Os cartões se empilham, cada um cobrindo a base do anterior. */
@Composable
private fun RecipeStackCard(
    recipe: Recipe,
    isLast: Boolean,
    onOpen: () -> Unit,
    onBrew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = recipe.method.palette
    val shape = RoundedCornerShape(30.dp)
    Box(
        modifier
            .overlapNext(if (isLast) 0.dp else 44.dp)
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .height(214.dp)
            .bouncyClick(pressedScale = 0.97f, onClickLabel = "Abrir receita", onClick = onOpen)
            .shadow(14.dp, shape, ambientColor = Bloom.Ink, spotColor = Bloom.Ink)
            .clip(shape)
            .background(p.bg),
    ) {
        MethodSilhouette(
            recipe.method,
            p.fg.copy(alpha = 0.18f),
            Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .offset(x = 36.dp, y = (-10).dp)
                .sway(4f, 5000 + (recipe.id % 3).toInt() * 600),
        )
        Column(Modifier.padding(20.dp)) {
            Text(recipe.method.label.uppercase(), style = BloomType.eyebrow, color = p.fg.copy(alpha = 0.75f))
            Text(
                recipe.name,
                style = BloomType.display(28.sp, -0.03f),
                color = p.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp, end = 40.dp),
            )
            if (recipe.coffee.isNotBlank()) {
                Text(recipe.coffee, style = BloomType.bodySmall, color = p.fg.copy(alpha = 0.8f), maxLines = 1, modifier = Modifier.padding(top = 6.dp))
            }
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                recipe.shortPills().forEach { Pill(it, p.chip, contentColor = p.fg, height = 30.dp) }
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 62.dp)
                .size(58.dp)
                .bouncyClick(pressedScale = 0.85f, onClickLabel = "Preparar", onClick = onBrew)
                .clip(CircleShape)
                .background(p.fg)
                .semantics { contentDescription = "Preparar ${recipe.name}" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, null, tint = p.bg, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun EmptyRecipes(filtered: Boolean, onNewRecipe: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Blob(Bloom.Mustard, Modifier.size(170.dp).enterCascade(0)) {
            MethodSilhouette(BrewMethod.V60, Bloom.Ink, Modifier.size(80.dp).sway())
        }
        Text(
            if (filtered) "Nenhuma receita com esse método." else "Seu caderno de receitas está vazio.",
            style = BloomType.display(22.sp, -0.02f),
            color = Bloom.Ink,
            textAlign = TextAlign.Center,
        )
        Text(
            "Cadastre a primeira ou faça um preparo livre e salve como receita.",
            style = BloomType.body,
            color = Bloom.Body,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        BigPillButton("Criar receita", onNewRecipe)
    }
}
