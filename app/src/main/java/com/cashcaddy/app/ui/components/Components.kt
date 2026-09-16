package com.cashcaddy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.cashcaddy.app.util.parseHexColor

val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@Composable
fun EmojiTile(
    emoji: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    corner: Dp = 12.dp,
) {
    val tint = parseHexColor(colorHex).copy(alpha = 0.22f)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(tint),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = (size.value * 0.45f).sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashCaddySheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        content()
    }
}