package com.heitoranglada.coffeeapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heitoranglada.coffeeapp.ui.theme.Bloom
import com.heitoranglada.coffeeapp.ui.theme.BloomType

data class NavTab(val route: String, val label: String, val icon: Int)

/**
 * Barra de navegação flutuante em pílula. A aba ativa abre o nome com mola;
 * o botão terracota cria uma receita nova.
 */
@Composable
fun FloatingNavBar(
    tabs: List<NavTab>,
    current: String?,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier
            .height(62.dp)
            .shadow(16.dp, shape, ambientColor = Bloom.Ink, spotColor = Bloom.Ink)
            .clip(shape)
            .background(Bloom.Ink)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tabs.forEach { tab ->
            val selected = tab.route == current
            val bg by animateColorAsState(if (selected) Bloom.Cream else Bloom.Ink, label = "tab-bg")
            val fg by animateColorAsState(if (selected) Bloom.Ink else Bloom.Cream, label = "tab-fg")
            Row(
                Modifier
                    .height(46.dp)
                    .bouncyClick(pressedScale = 0.9f, onClickLabel = tab.label) { onSelect(tab.route) }
                    .clip(shape)
                    .background(bg)
                    .padding(horizontal = 16.dp)
                    .semantics {
                        role = Role.Tab
                        this.selected = selected
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(tab.icon), contentDescription = tab.label, tint = fg, modifier = Modifier.size(22.dp))
                AnimatedVisibility(
                    selected,
                    enter = expandHorizontally(spring(dampingRatio = 0.7f, stiffness = 400f)) + fadeIn(),
                    exit = shrinkHorizontally(spring(dampingRatio = 0.9f, stiffness = 500f)) + fadeOut(),
                ) {
                    Row {
                        Spacer(Modifier.width(8.dp))
                        Text(tab.label, style = BloomType.display(14.sp, 0f), color = fg, maxLines = 1)
                    }
                }
            }
        }
        Box(
            Modifier
                .padding(start = 4.dp)
                .size(width = 56.dp, height = 46.dp)
                .bouncyClick(pressedScale = 0.85f, onClickLabel = "Nova receita", onClick = onAdd)
                .clip(shape)
                .background(Bloom.Terra),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nova receita", tint = Bloom.Ink, modifier = Modifier.size(24.dp))
        }
    }
}
