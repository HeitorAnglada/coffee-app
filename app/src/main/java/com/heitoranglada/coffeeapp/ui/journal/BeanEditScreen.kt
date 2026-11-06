package com.heitoranglada.coffeeapp.ui.journal

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heitoranglada.coffeeapp.data.journal.Bean
import com.heitoranglada.coffeeapp.ui.appViewModelFactory
import com.heitoranglada.coffeeapp.ui.components.BigPillButton
import com.heitoranglada.coffeeapp.ui.components.Blob
import com.heitoranglada.coffeeapp.ui.components.CircleIconButton
import com.heitoranglada.coffeeapp.ui.components.ErrorSticker
import com.heitoranglada.coffeeapp.ui.components.PillField
import com.heitoranglada.coffeeapp.ui.components.bouncyClick
import com.heitoranglada.coffeeapp.ui.components.enterCascade
import com.heitoranglada.coffeeapp.ui.components.popIn
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType
import java.io.File

private val processes = listOf("Natural", "Lavado", "Honey", "Fermentado")
private val roasts = listOf("Clara", "Média", "Escura")

/** Cadastro e edição de um grão, com fotos da câmera ou da galeria. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BeanEditScreen(
    beanId: Long,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    vm: BeanEditViewModel = viewModel(
        key = "bean-edit-$beanId",
        factory = appViewModelFactory { BeanEditViewModel(it, beanId) },
    ),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.savedId) { state.savedId?.let(onSaved) }

    var cameraFile by rememberSaveable { mutableStateOf<String?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = cameraFile
        cameraFile = null
        if (path != null) {
            val f = File(path)
            if (ok) vm.addPhoto(Uri.fromFile(f)) { vm.photos.clearCameraTemp(f) } else vm.photos.clearCameraTemp(f)
        }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(4)) { uris ->
        uris.forEach { vm.addPhoto(it) }
    }

    if (state.loading) {
        Box(Modifier.fillMaxSize().background(Bloom.Cream), contentAlignment = Alignment.Center) {
            Blob(Bloom.Mustard, Modifier.size(110.dp), periodMs = 2000, wobble = 0.12f)
        }
        return
    }
    val form = state.form
    val onChange = vm::onChange

    Column(
        Modifier
            .fillMaxSize()
            .background(Bloom.Cream)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onBack)
            Spacer(Modifier.weight(1f))
            Text("Tudo opcional, menos o nome", style = BloomType.bodySmall, color = Bloom.Muted)
        }
        Text(
            buildAnnotatedString {
                append(if (beanId == 0L) "Novo " else "Editar ")
                withStyle(SpanStyle(color = Bloom.Terra)) { append("grão") }
            },
            style = BloomType.display(40.sp, -0.035f),
            color = Bloom.Ink,
            modifier = Modifier.enterCascade(0),
        )

        LazyRow(Modifier.enterCascade(1), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item(key = "add") {
                Column(
                    Modifier
                        .size(width = 120.dp, height = 150.dp)
                        .drawBehind {
                            drawRoundRect(
                                Bloom.Muted,
                                cornerRadius = CornerRadius(22.dp.toPx()),
                                style = Stroke(width = 2.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f))),
                            )
                        }
                        .padding(3.dp)
                        .clip(RoundedCornerShape(19.dp)),
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .bouncyClick(onClickLabel = "Tirar foto") {
                                val (file, uri) = vm.photos.newCameraTarget()
                                cameraFile = file.path
                                camera.launch(uri)
                            }
                            .background(Bloom.Terra),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null, tint = Bloom.Ink, modifier = Modifier.size(26.dp))
                        Text("Tirar foto", style = BloomType.display(14.sp, 0f), color = Bloom.Ink, modifier = Modifier.padding(top = 4.dp))
                    }
                    Row(
                        Modifier
                            .weight(0.7f)
                            .fillMaxWidth()
                            .bouncyClick(onClickLabel = "Escolher da galeria") {
                                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Image, null, tint = Bloom.Ink, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Galeria", style = BloomType.chip, color = Bloom.Ink)
                    }
                }
            }
            items(form.photos, key = { it }) { photo ->
                Box(Modifier.size(width = 110.dp, height = 150.dp).popIn().animateItem()) {
                    BeanPicture(
                        Bean(name = form.name.ifBlank { "Café" }),
                        Modifier.fillMaxSize(),
                        corner = 22.dp,
                        photo = photo,
                        showLabel = false,
                    )
                    CircleIconButton(
                        Icons.Filled.Close,
                        "Remover foto",
                        { vm.removePhoto(photo) },
                        Modifier.align(Alignment.TopEnd).padding(6.dp),
                        background = Bloom.Cream,
                        size = 30.dp,
                    )
                }
            }
            if (state.importing) {
                item(key = "importing") {
                    Box(Modifier.size(width = 110.dp, height = 150.dp).clip(RoundedCornerShape(22.dp)).background(Bloom.Sand), contentAlignment = Alignment.Center) {
                        Blob(Bloom.Terra, Modifier.size(48.dp), periodMs = 1600, wobble = 0.14f)
                    }
                }
            }
        }
        Text(
            "Fotografe a frente e o verso do pacote. As fotos ficam só no celular.",
            style = BloomType.bodySmall,
            color = Bloom.Muted,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillField(form.name, { onChange(form.copy(name = it)) }, Modifier.weight(1.3f), label = "Nome do café", placeholder = "Santa Inês", highlight = state.errors.isNotEmpty())
            PillField(form.roaster, { onChange(form.copy(roaster = it)) }, Modifier.weight(1f), label = "Torrefação", placeholder = "Quem torrou")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillField(form.origin, { onChange(form.copy(origin = it)) }, Modifier.weight(1.3f), label = "Origem", placeholder = "Fazenda, região")
            PillField(form.variety, { onChange(form.copy(variety = it)) }, Modifier.weight(1f), label = "Variedade", placeholder = "Catuaí")
        }
        ChoiceGroup("Processo", processes, form.process) { onChange(form.copy(process = it)) }
        ChoiceGroup("Torra", roasts, form.roast) { onChange(form.copy(roast = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillField(form.lot, { onChange(form.copy(lot = it)) }, Modifier.weight(1f), label = "Lote", placeholder = "0423")
            PillField(form.roastDate, { onChange(form.copy(roastDate = it)) }, Modifier.weight(1f), label = "Data da torra", placeholder = "24/10/2026", keyboardType = KeyboardType.Number)
        }
        PillField(form.flavorNotes, { onChange(form.copy(flavorNotes = it)) }, label = "Notas de sabor", placeholder = "Chocolate, caramelo, laranja…", singleLine = false)

        ErrorSticker(state.errors)
        BigPillButton("Salvar grão", vm::save, enabled = !state.importing)
        Spacer(Modifier.height(16.dp))
    }
}

/** Pílulas de escolha única; tocar de novo na escolhida limpa. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceGroup(label: String, options: List<String>, value: String, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = BloomType.label, color = Bloom.Muted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { option ->
                val on = option.equals(value, ignoreCase = true)
                val bg by animateColorAsState(if (on) Bloom.Ink else Bloom.Cream, label = "choice-bg")
                val fg by animateColorAsState(if (on) Bloom.Cream else Bloom.Ink, label = "choice-fg")
                Box(
                    Modifier
                        .height(40.dp)
                        .bouncyClick { onPick(if (on) "" else option) }
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .border(2.dp, Bloom.Ink, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp)
                        .semantics {
                            role = Role.RadioButton
                            selected = on
                        },
                    contentAlignment = Alignment.Center,
                ) { Text(option, style = BloomType.chip.copy(fontSize = 14.sp), color = fg) }
            }
        }
    }
}
