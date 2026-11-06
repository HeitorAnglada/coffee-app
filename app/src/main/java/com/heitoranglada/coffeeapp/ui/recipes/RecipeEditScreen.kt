package com.heitoranglada.coffeeapp.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.ui.components.CoffeeFormFields
import com.heitoranglada.coffeeapp.ui.components.ErrorCard
import com.heitoranglada.coffeeapp.ui.repoViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    recipeId: Long,
    fromBrewId: Long,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    vm: RecipeEditViewModel = viewModel(
        key = "edit-$recipeId-$fromBrewId",
        factory = repoViewModelFactory { RecipeEditViewModel(it, recipeId, fromBrewId) },
    ),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val coffees by vm.coffees.collectAsStateWithLifecycle()
    val grinders by vm.grinders.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedId) { state.savedId?.let(onSaved) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "Nova receita" else "Editar receita") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") }
                },
            )
        },
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CoffeeFormFields(
                form = state.form,
                onChange = vm::onFormChange,
                coffees = coffees,
                grinders = grinders,
                showName = true,
                showRating = false,
                timeLabel = "Tempo-alvo",
                poursHint = "Informe quando cada despejo começa (m:ss) e quantos gramas de água ele tem. O primeiro costuma ser o bloom.",
            )
            ErrorCard(state.errors)
            Button(onClick = vm::save, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Salvar receita")
            }
        }
    }
}
