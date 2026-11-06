package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Formulário compartilhado entre "Nova receita" e "Salvar preparo".
 * Campos essenciais ficam visíveis; temperatura, tempo e notas ficam em "Opções avançadas".
 */
@Composable
fun CoffeeFormFields(
    form: CoffeeForm,
    onChange: (CoffeeForm) -> Unit,
    coffees: List<String>,
    grinders: List<String>,
    showName: Boolean,
    showRating: Boolean,
    timeLabel: String,
    poursHint: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionCard("Básico") {
            if (showName) {
                FormTextField(form.name, { onChange(form.copy(name = it)) }, "Nome da receita", placeholder = "Ex.: V60 do sábado")
            }
            MethodSelector(form.method) { onChange(form.copy(method = it)) }
            SuggestField(form.coffee, { onChange(form.copy(coffee = it)) }, "Café usado", coffees, placeholder = "Ex.: Fazenda X, Catuaí amarelo")
            FormTextField(
                form.doseText, { onChange(form.copy(doseText = it)) }, "Dose de café",
                suffix = "g", keyboardType = KeyboardType.Decimal, placeholder = "15",
            )
        }

        SectionCard("Moagem") {
            SuggestField(form.grinder, { onChange(form.copy(grinder = it)) }, "Moedor", grinders, placeholder = "Ex.: Comandante C40")
            FormTextField(form.grindSetting, { onChange(form.copy(grindSetting = it)) }, "Ajuste de moagem", placeholder = "Ex.: 24 cliques / média-fina")
        }

        SectionCard("Despejos") {
            Text(poursHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PoursEditor(form.pours, { onChange(form.copy(pours = it)) }, form.dose)
        }

        if (showRating) {
            SectionCard("Como ficou?") {
                RatingBar(form.rating) { onChange(form.copy(rating = it)) }
            }
        }

        AdvancedSection {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormTextField(
                    form.waterTempText, { onChange(form.copy(waterTempText = it)) }, "Temperatura da água",
                    suffix = "°C", keyboardType = KeyboardType.Number, placeholder = "93",
                    modifier = Modifier.weight(1f),
                )
                FormTextField(
                    form.timeText, { onChange(form.copy(timeText = it)) }, timeLabel,
                    placeholder = "3:00", modifier = Modifier.weight(1f),
                )
            }
            FormTextField(
                form.notes, { onChange(form.copy(notes = it)) }, "Notas",
                singleLine = false, placeholder = "Sabor, acidez, doçura, o que mudar na próxima…",
            )
        }
    }
}
