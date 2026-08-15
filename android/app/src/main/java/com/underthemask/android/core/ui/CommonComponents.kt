package com.underthemask.android.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.underthemask.android.R
import com.underthemask.android.core.ui.theme.AppColors
import com.underthemask.android.core.ui.theme.AppTheme
import com.underthemask.android.core.websocket.ConnectionState

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(AppColors.Background, AppColors.BackgroundRaised, AppColors.Background),
                    ),
                ),
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val gap = 40.dp.toPx()
                var x = 0f
                while (x <= size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += gap
                }
                var y = 0f
                while (y <= size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += gap
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AppColors.Gold.copy(alpha = 0.07f), Color.Transparent),
                        center = Offset(size.width * 0.18f, size.height * 0.12f),
                        radius = size.minDimension * 0.75f,
                    ),
                    radius = size.minDimension * 0.75f,
                    center = Offset(size.width * 0.18f, size.height * 0.12f),
                )
            }
            content()
        }
    }
}

@Composable
fun ContentColumn(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 680.dp,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = maxWidth),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.large),
    ) {
        content()
    }
}

@Composable
fun AppTopBar(
    title: String,
    eyebrow: String? = null,
    onBack: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Background.copy(alpha = 0.94f))
                .statusBarsPadding()
                .height(72.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onBack != null) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    onClick = onBack,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "‹",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.semantics { contentDescription = "Nazad" },
                        )
                    }
                }
            } else {
                BrandLogo(size = 44.dp, containerSize = 48.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                if (eyebrow != null) {
                    Text(
                        eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
            }
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(actionLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    containerSize: Dp = 76.dp,
) {
    Surface(
        modifier = modifier.size(containerSize),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.padding((containerSize - size) / 2),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    centered: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BrandLogo()
        Column(horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start) {
            Text(
                "UNDER THE MASK",
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "SOCIAL DEDUCTION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun AppPanel(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = if (highlighted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.74f)
        else MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
        ),
        shadowElevation = 8.dp,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) { content() }
    }
}

@Composable
fun SectionHeader(
    eyebrow: String,
    title: String,
    trailing: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        if (trailing != null) {
            StatusPill(trailing)
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.32f)),
    ) {
        Text(
            text.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
fun PlayerAvatar(
    name: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    size: Dp = 44.dp,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (emphasized) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (emphasized) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun LoadingScreen(message: String = "Učitavanje...") {
    AppBackground {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandLogo(size = 52.dp, containerSize = 64.dp)
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InlineError(message: String, onDismiss: (() -> Unit)? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Canvas(Modifier.size(8.dp)) { drawCircle(AppColors.Error) }
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) { Text("U redu") }
            }
        }
    }
}

@Composable
fun ConnectionBanner(state: ConnectionState) {
    AnimatedVisibility(
        visible = state != ConnectionState.CONNECTED,
        enter = fadeIn() + scaleIn(initialScale = 0.98f),
        exit = fadeOut() + scaleOut(targetScale = 0.98f),
    ) {
        val message = when (state) {
            ConnectionState.CONNECTING -> "Ponovno povezivanje..."
            ConnectionState.ERROR -> "Veza je nestabilna. Podaci se osvežavaju u pozadini."
            ConnectionState.DISCONNECTED -> "Veza je prekinuta. Pokušavam ponovo."
            ConnectionState.CONNECTED -> ""
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun <T> SegmentedChoice(
    label: String,
    selected: T,
    options: List<Pair<T, String>>,
    enabled: Boolean = true,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEach { (value, text) ->
                val isSelected = selected == value
                val color by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "segmentColor",
                )
                Surface(
                    modifier = Modifier.weight(1f),
                    color = color,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    enabled = enabled,
                    onClick = { onSelected(value) },
                ) {
                    Text(
                        text,
                        modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            !enabled -> AppColors.Disabled
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = AppColors.Disabled,
            errorTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            cursorColor = MaterialTheme.colorScheme.primary,
            errorCursorColor = MaterialTheme.colorScheme.error,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
            disabledIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            errorIndicatorColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = AppColors.Disabled,
            errorLabelColor = MaterialTheme.colorScheme.error,
            focusedPlaceholderColor = AppColors.Muted,
            unfocusedPlaceholderColor = AppColors.Muted,
            disabledPlaceholderColor = AppColors.Disabled,
            errorPlaceholderColor = AppColors.Muted,
            focusedSupportingTextColor = AppColors.Muted,
            unfocusedSupportingTextColor = AppColors.Muted,
            disabledSupportingTextColor = AppColors.Disabled,
            errorSupportingTextColor = MaterialTheme.colorScheme.error,
        ),
    )
}

@Composable
fun PrimaryAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, label = "buttonScale")
    Button(
        modifier = modifier.fillMaxWidth().height(54.dp).scale(scale),
        enabled = enabled,
        onClick = onClick,
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = AppColors.Disabled,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        modifier = modifier.fillMaxWidth().height(54.dp),
        enabled = enabled,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = AppColors.Disabled,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            TextButton(enabled = confirmEnabled, onClick = onConfirm) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Odustani") } },
    )
}

@Composable
fun BottomActionSurface(content: @Composable () -> Unit) {
    Surface(
        color = AppColors.BackgroundRaised.copy(alpha = 0.98f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 18.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .imePadding()
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.fillMaxWidth().widthIn(max = 760.dp)) { content() }
        }
    }
}

@Composable
fun RealtimeLifecycle(onStart: () -> Unit, onStop: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_STOP -> onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) onStart()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            onStop()
        }
    }
}
