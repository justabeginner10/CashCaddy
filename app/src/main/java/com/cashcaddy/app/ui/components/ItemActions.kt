package com.cashcaddy.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemActionsBox(
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    contentDescription: String,
    deleteTitle: String,
    deleteBody: String,
    modifier: Modifier = Modifier,
    openLabel: String = "Edit",
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable BoxScope.(Modifier) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var menuOpen by remember { mutableStateOf(false) }
    var confirmOpen by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    Box(modifier) {
        content(
            Modifier
                .clip(shape)
                .semantics(mergeDescendants = true) {
                    this.contentDescription = contentDescription
                    role = Role.Button
                    customActions = listOf(
                        CustomAccessibilityAction(openLabel) {
                            onOpen()
                            true
                        },
                        CustomAccessibilityAction("Delete") {
                            confirmOpen = true
                            true
                        },
                    )
                }
                .combinedClickable(
                    onClickLabel = openLabel,
                    onLongClickLabel = "Show actions",
                    onClick = onOpen,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuOpen = true
                    },
                ),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(1.dp),
        ) {
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        menuOpen = false
                        onOpen()
                    },
                    leadingIcon = {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuOpen = false
                        confirmOpen = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = scheme.error,
                        )
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = scheme.error,
                        leadingIconColor = scheme.error,
                    ),
                )
            }
        }
    }

    if (confirmOpen) {
        ConfirmDeleteDialog(
            title = deleteTitle,
            body = deleteBody,
            onConfirm = {
                confirmOpen = false
                onDelete()
            },
            onDismiss = { confirmOpen = false },
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
