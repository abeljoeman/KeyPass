package com.yogeshpaliyal.keypass.ui.style

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.yogeshpaliyal.keypass.R

@Composable
private fun keyPassDarkColorScheme(): ColorScheme =
    darkColorScheme(
        primary = colorResource(R.color.keypass_primary),
        onPrimary = colorResource(R.color.keypass_on_primary),
        primaryContainer = colorResource(R.color.keypass_primary_container),
        onPrimaryContainer = colorResource(R.color.keypass_on_primary_container),
        inversePrimary = colorResource(R.color.keypass_inverse_primary),
        secondary = colorResource(R.color.keypass_secondary),
        onSecondary = colorResource(R.color.keypass_on_secondary),
        secondaryContainer = colorResource(R.color.keypass_secondary_container),
        onSecondaryContainer = colorResource(R.color.keypass_on_secondary_container),
        tertiary = colorResource(R.color.keypass_brand_accent),
        onTertiary = colorResource(R.color.keypass_on_brand_accent),
        tertiaryContainer = colorResource(R.color.keypass_brand_accent_container),
        onTertiaryContainer = colorResource(R.color.keypass_on_brand_accent_container),
        background = colorResource(R.color.keypass_background),
        onBackground = colorResource(R.color.keypass_on_surface),
        surface = colorResource(R.color.keypass_surface),
        onSurface = colorResource(R.color.keypass_on_surface),
        surfaceVariant = colorResource(R.color.keypass_surface_container),
        onSurfaceVariant = colorResource(R.color.keypass_on_surface_variant),
        surfaceTint = colorResource(R.color.keypass_primary),
        inverseSurface = colorResource(R.color.keypass_inverse_surface),
        inverseOnSurface = colorResource(R.color.keypass_inverse_on_surface),
        error = colorResource(R.color.keypass_error),
        onError = colorResource(R.color.keypass_on_error),
        errorContainer = colorResource(R.color.keypass_error_container),
        onErrorContainer = colorResource(R.color.keypass_on_error_container),
        outline = colorResource(R.color.keypass_outline),
        outlineVariant = colorResource(R.color.keypass_outline_variant),
        scrim = colorResource(R.color.keypass_scrim),
        surfaceBright = colorResource(R.color.keypass_surface_container_highest),
        surfaceDim = colorResource(R.color.keypass_background),
        surfaceContainer = colorResource(R.color.keypass_surface_container),
        surfaceContainerHigh = colorResource(R.color.keypass_surface_container_high),
        surfaceContainerHighest = colorResource(R.color.keypass_surface_container_highest),
        surfaceContainerLow = colorResource(R.color.keypass_surface),
        surfaceContainerLowest = colorResource(R.color.keypass_background),
    )

private fun Typography.withRahsaHierarchy(): Typography =
    copy(
        titleLarge = titleLarge.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium,
        ),
        titleMedium = titleMedium.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
        ),
        bodyLarge = bodyLarge.copy(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
        ),
        bodyMedium = bodyMedium.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
        ),
        labelLarge = labelLarge.copy(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        ),
        labelMedium = labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp,
        ),
    )

private fun Shapes.withRahsaShapes(): Shapes =
    copy(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )

@Composable
fun KeyPassTheme(content: @Composable () -> Unit) {
    Mdc3Theme {
        MaterialTheme(
            colorScheme = keyPassDarkColorScheme(),
            typography = MaterialTheme.typography.withRahsaHierarchy(),
            shapes = MaterialTheme.shapes.withRahsaShapes(),
            content = content,
        )
    }
}
