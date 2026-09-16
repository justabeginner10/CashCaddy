package com.cashcaddy.app.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Single-line money (or any numeric) label. Shrinks to fit; if it still
 * overflows at [minTextSize], the tail is ellipsized.
 */
@Composable
fun MoneyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    minTextSize: TextUnit = (style.fontSize.value * 0.5f).coerceAtLeast(11f).sp,
) {
    var fontSize by remember(text, style.fontSize) { mutableStateOf(style.fontSize) }
    var ready by remember(text, style.fontSize) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent { if (ready) drawContent() },
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        style = style.copy(fontSize = fontSize),
        onTextLayout = { layout ->
            if (ready) return@Text
            if (layout.hasVisualOverflow && fontSize.value > minTextSize.value + 0.4f) {
                fontSize = (fontSize.value * 0.88f).coerceAtLeast(minTextSize.value).sp
            } else {
                ready = true
            }
        },
    )
}
