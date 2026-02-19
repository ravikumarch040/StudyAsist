package com.studyasist.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class ThemeConfig(
    val lightScheme: ColorScheme,
    val darkScheme: ColorScheme,
    val typography: Typography,
    val shapes: Shapes,
    val forceDark: Boolean = false
)

enum class AppTheme(val displayNameRes: Int) {
    DYNAMIC(com.studyasist.R.string.theme_dynamic),
    MINIMAL_LIGHT(com.studyasist.R.string.theme_minimal_light),
    MINIMAL_DARK(com.studyasist.R.string.theme_minimal_dark),
    PASTEL_CALM(com.studyasist.R.string.theme_pastel_calm),
    ACADEMIC_PAPER(com.studyasist.R.string.theme_academic_paper),
    VIBRANT_STUDY(com.studyasist.R.string.theme_vibrant_study),
    DARK_HIGH_CONTRAST(com.studyasist.R.string.theme_dark_high_contrast),
    NEO_GLASS(com.studyasist.R.string.theme_neo_glass),
    RETRO_CHALKBOARD(com.studyasist.R.string.theme_retro_chalkboard),
    NATURE_EARTHY(com.studyasist.R.string.theme_nature_earthy),
    PRODUCTIVITY_DASHBOARD(com.studyasist.R.string.theme_productivity_dashboard);

    val isDynamic: Boolean get() = this == DYNAMIC

    val config: ThemeConfig
        get() = themeConfigs.getValue(this)

    val previewColors: PreviewColors
        get() = previewColorsMap.getValue(this)
}

data class PreviewColors(
    val primary: Color,
    val surface: Color,
    val accent: Color
)

private val previewColorsMap = mapOf(
    AppTheme.DYNAMIC to PreviewColors(Color(0xFF6750A4), Color(0xFFFEF7FF), Color(0xFF625B71)),
    AppTheme.MINIMAL_LIGHT to PreviewColors(Color(0xFF0B5FFF), Color(0xFFF6F8FF), Color(0xFF00B37E)),
    AppTheme.MINIMAL_DARK to PreviewColors(Color(0xFF7DA7FF), Color(0xFF111827), Color(0xFFFBBF24)),
    AppTheme.PASTEL_CALM to PreviewColors(Color(0xFF7CB9E8), Color(0xFFFFF8F3), Color(0xFFF7B7A3)),
    AppTheme.ACADEMIC_PAPER to PreviewColors(Color(0xFF003366), Color(0xFFFCFBF8), Color(0xFF6B6B6B)),
    AppTheme.VIBRANT_STUDY to PreviewColors(Color(0xFFFF6B6B), Color(0xFFFFF8F9), Color(0xFFFFD93D)),
    AppTheme.DARK_HIGH_CONTRAST to PreviewColors(Color(0xFF00E5FF), Color(0xFF0D1117), Color(0xFFFFB86B)),
    AppTheme.NEO_GLASS to PreviewColors(Color(0xFF8A5FFC), Color(0xFF1A1A2E), Color(0xFFE040FB)),
    AppTheme.RETRO_CHALKBOARD to PreviewColors(Color(0xFF0B3D2E), Color(0xFFF0F4F1), Color(0xFFFFD166)),
    AppTheme.NATURE_EARTHY to PreviewColors(Color(0xFF2E8B57), Color(0xFFFAF9F7), Color(0xFFF4A261)),
    AppTheme.PRODUCTIVITY_DASHBOARD to PreviewColors(Color(0xFF0F62FE), Color(0xFFF3F4F6), Color(0xFFFF4D4D))
)

// ── Minimal Light ──────────────────────────────────────────────────────────────

private val MinimalLightLight = lightColorScheme(
    primary = Color(0xFF0B5FFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF00B37E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB8F0D8),
    onSecondaryContainer = Color(0xFF002114),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0B1733),
    surface = Color(0xFFF6F8FF),
    onSurface = Color(0xFF0B1733),
    surfaceVariant = Color(0xFFE0E4EC),
    onSurfaceVariant = Color(0xFF43474E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val MinimalLightDark = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468C),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF9CD4BD),
    onSecondary = Color(0xFF003826),
    secondaryContainer = Color(0xFF005139),
    onSecondaryContainer = Color(0xFFB8F0D8),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C6CF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Minimal Dark ───────────────────────────────────────────────────────────────

private val MinimalDarkLight = lightColorScheme(
    primary = Color(0xFF4A6FA5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C3A),
    secondary = Color(0xFFC29020),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE08C),
    onSecondaryContainer = Color(0xFF3D2E00),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1A1C20),
    surface = Color(0xFFF0F2F6),
    onSurface = Color(0xFF1A1C20),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val MinimalDarkDark = darkColorScheme(
    primary = Color(0xFF7DA7FF),
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF004787),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF3D2E00),
    secondaryContainer = Color(0xFF594400),
    onSecondaryContainer = Color(0xFFFFE08C),
    background = Color(0xFF0F1724),
    onBackground = Color(0xFFE6EEF8),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE6EEF8),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C6CF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Pastel Calm ────────────────────────────────────────────────────────────────

private val PastelCalmLight = lightColorScheme(
    primary = Color(0xFF7CB9E8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD0E8F7),
    onPrimaryContainer = Color(0xFF001E31),
    secondary = Color(0xFFF7B7A3),
    onSecondary = Color(0xFF442B20),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF3B1A0E),
    background = Color(0xFFFFF8F3),
    onBackground = Color(0xFF1F2D3D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F2D3D),
    surfaceVariant = Color(0xFFF0E4DC),
    onSurfaceVariant = Color(0xFF52443C),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val PastelCalmDark = darkColorScheme(
    primary = Color(0xFF9CCCF0),
    onPrimary = Color(0xFF003550),
    primaryContainer = Color(0xFF004D72),
    onPrimaryContainer = Color(0xFFD0E8F7),
    secondary = Color(0xFFFFB59E),
    onSecondary = Color(0xFF5C2712),
    secondaryContainer = Color(0xFF773D26),
    onSecondaryContainer = Color(0xFFFFDBCF),
    background = Color(0xFF1A1410),
    onBackground = Color(0xFFEFE1D7),
    surface = Color(0xFF201A16),
    onSurface = Color(0xFFEFE1D7),
    surfaceVariant = Color(0xFF52443C),
    onSurfaceVariant = Color(0xFFD7C3B8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Academic Paper ─────────────────────────────────────────────────────────────

private val AcademicPaperLight = lightColorScheme(
    primary = Color(0xFF003366),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF6B6B6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8E8E8),
    onSecondaryContainer = Color(0xFF1F1F1F),
    background = Color(0xFFFCFBF8),
    onBackground = Color(0xFF1B1A17),
    surface = Color(0xFFFCFBF8),
    onSurface = Color(0xFF1B1A17),
    surfaceVariant = Color(0xFFE3E1DB),
    onSurfaceVariant = Color(0xFF464540),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val AcademicPaperDark = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003259),
    primaryContainer = Color(0xFF00497E),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFC8C6C1),
    onSecondary = Color(0xFF313029),
    secondaryContainer = Color(0xFF47463F),
    onSecondaryContainer = Color(0xFFE5E2DB),
    background = Color(0xFF1B1A17),
    onBackground = Color(0xFFE6E2DB),
    surface = Color(0xFF1B1A17),
    onSurface = Color(0xFFE6E2DB),
    surfaceVariant = Color(0xFF464540),
    onSurfaceVariant = Color(0xFFC8C6C0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Vibrant Study ──────────────────────────────────────────────────────────────

private val VibrantStudyLight = lightColorScheme(
    primary = Color(0xFFFF6B6B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF4D96FF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E3FF),
    onSecondaryContainer = Color(0xFF001B3E),
    tertiary = Color(0xFFFFD93D),
    onTertiary = Color(0xFF3E2E00),
    background = Color(0xFFFFF8F9),
    onBackground = Color(0xFF201A1A),
    surface = Color(0xFFFFF8F9),
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF534341),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val VibrantStudyDark = darkColorScheme(
    primary = Color(0xFFFFB3AD),
    onPrimary = Color(0xFF680008),
    primaryContainer = Color(0xFF930012),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFA8C8FF),
    onSecondary = Color(0xFF003063),
    secondaryContainer = Color(0xFF00468C),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = Color(0xFFFFD93D),
    onTertiary = Color(0xFF3E2E00),
    background = Color(0xFF201A1A),
    onBackground = Color(0xFFEDE0DE),
    surface = Color(0xFF201A1A),
    onSurface = Color(0xFFEDE0DE),
    surfaceVariant = Color(0xFF534341),
    onSurfaceVariant = Color(0xFFD8C2BF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Dark High-Contrast ─────────────────────────────────────────────────────────

private val DarkHighContrastLight = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFFB06D1C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDDB5),
    onSecondaryContainer = Color(0xFF3A2000),
    background = Color(0xFFF5FAFB),
    onBackground = Color(0xFF171D1E),
    surface = Color(0xFFF5FAFB),
    onSurface = Color(0xFF171D1E),
    surfaceVariant = Color(0xFFDBE4E6),
    onSurfaceVariant = Color(0xFF3F484A),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val DarkHighContrastDark = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = Color(0xFFFFB86B),
    onSecondary = Color(0xFF5B3700),
    secondaryContainer = Color(0xFF814F00),
    onSecondaryContainer = Color(0xFFFFDDB5),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0D1117),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF3F484A),
    onSurfaceVariant = Color(0xFFBFC8CA),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Neo-Glass ──────────────────────────────────────────────────────────────────

private val NeoGlassLight = lightColorScheme(
    primary = Color(0xFF8A5FFC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8DEFF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFFE040FB),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD6FE),
    onSecondaryContainer = Color(0xFF3A0048),
    background = Color(0xFFF8F5FF),
    onBackground = Color(0xFF1C1A22),
    surface = Color(0xFFF0EDFA),
    onSurface = Color(0xFF1C1A22),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val NeoGlassDark = darkColorScheme(
    primary = Color(0xFFCFBCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF6F47DB),
    onPrimaryContainer = Color(0xFFE8DEFF),
    secondary = Color(0xFFF3B0FF),
    onSecondary = Color(0xFF550065),
    secondaryContainer = Color(0xFF7A008E),
    onSecondaryContainer = Color(0xFFFFD6FE),
    background = Color(0xFF1A1A2E),
    onBackground = Color(0xFFE6E0F0),
    surface = Color(0xFF1E1B2A),
    onSurface = Color(0xFFE6E0F0),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Retro Chalkboard ───────────────────────────────────────────────────────────

private val RetroChalkboardLight = lightColorScheme(
    primary = Color(0xFF0B3D2E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA4D8C4),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFFFFD166),
    onSecondary = Color(0xFF3E2E00),
    secondaryContainer = Color(0xFFFFE599),
    onSecondaryContainer = Color(0xFF241A00),
    background = Color(0xFFF0F4F1),
    onBackground = Color(0xFF1A201D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A201D),
    surfaceVariant = Color(0xFFDBE5DE),
    onSurfaceVariant = Color(0xFF404943),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val RetroChalkboardDark = darkColorScheme(
    primary = Color(0xFF88C9AB),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF005140),
    onPrimaryContainer = Color(0xFFA4D8C4),
    secondary = Color(0xFFFFD166),
    onSecondary = Color(0xFF3E2E00),
    secondaryContainer = Color(0xFF594400),
    onSecondaryContainer = Color(0xFFFFE599),
    background = Color(0xFF0B3D2E),
    onBackground = Color(0xFFF0F4F1),
    surface = Color(0xFF0F4838),
    onSurface = Color(0xFFF0F4F1),
    surfaceVariant = Color(0xFF404943),
    onSurfaceVariant = Color(0xFFBFC9C2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Nature / Earthy ────────────────────────────────────────────────────────────

private val NatureEarthyLight = lightColorScheme(
    primary = Color(0xFF2E8B57),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB0DDCA),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFFF4A261),
    onSecondary = Color(0xFF442B10),
    secondaryContainer = Color(0xFFFFDDB5),
    onSecondaryContainer = Color(0xFF2E1800),
    background = Color(0xFFFAF9F7),
    onBackground = Color(0xFF1E2D24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E2D24),
    surfaceVariant = Color(0xFFDDE5DB),
    onSurfaceVariant = Color(0xFF414941),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF)
)

private val NatureEarthyDark = darkColorScheme(
    primary = Color(0xFF7EC8A0),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFFB0DDCA),
    secondary = Color(0xFFFFBB73),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3B00),
    onSecondaryContainer = Color(0xFFFFDDB5),
    background = Color(0xFF1A1C1A),
    onBackground = Color(0xFFE2E3DE),
    surface = Color(0xFF1A1C1A),
    onSurface = Color(0xFFE2E3DE),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC1C9BF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Productivity Dashboard ─────────────────────────────────────────────────────

private val ProductivityDashboardLight = lightColorScheme(
    primary = Color(0xFF0F62FE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFFFF4D4D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),
    background = Color(0xFFF3F4F6),
    onBackground = Color(0xFF1A1B1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1B1E),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    error = Color(0xFFFF4D4D),
    onError = Color(0xFFFFFFFF)
)

private val ProductivityDashboardDark = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468C),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFFFB3AD),
    onSecondary = Color(0xFF680008),
    secondaryContainer = Color(0xFF930012),
    onSecondaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF1A1C22),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ── Config map ─────────────────────────────────────────────────────────────────

internal val themeConfigs: Map<AppTheme, ThemeConfig> by lazy {
    mapOf(
        AppTheme.DYNAMIC to ThemeConfig(
            lightScheme = MinimalLightLight,
            darkScheme = MinimalLightDark,
            typography = interRobotoTypography(),
            shapes = minimalShapes()
        ),
        AppTheme.MINIMAL_LIGHT to ThemeConfig(
            lightScheme = MinimalLightLight,
            darkScheme = MinimalLightDark,
            typography = interRobotoTypography(),
            shapes = minimalShapes()
        ),
        AppTheme.MINIMAL_DARK to ThemeConfig(
            lightScheme = MinimalDarkLight,
            darkScheme = MinimalDarkDark,
            typography = interRobotoTypography(),
            shapes = minimalShapes(),
            forceDark = true
        ),
        AppTheme.PASTEL_CALM to ThemeConfig(
            lightScheme = PastelCalmLight,
            darkScheme = PastelCalmDark,
            typography = poppinsMerriweatherTypography(),
            shapes = pastelShapes()
        ),
        AppTheme.ACADEMIC_PAPER to ThemeConfig(
            lightScheme = AcademicPaperLight,
            darkScheme = AcademicPaperDark,
            typography = merriweatherRobotoTypography(),
            shapes = academicShapes()
        ),
        AppTheme.VIBRANT_STUDY to ThemeConfig(
            lightScheme = VibrantStudyLight,
            darkScheme = VibrantStudyDark,
            typography = montserratInterTypography(),
            shapes = vibrantShapes()
        ),
        AppTheme.DARK_HIGH_CONTRAST to ThemeConfig(
            lightScheme = DarkHighContrastLight,
            darkScheme = DarkHighContrastDark,
            typography = interLargeTypography(),
            shapes = minimalShapes(),
            forceDark = true
        ),
        AppTheme.NEO_GLASS to ThemeConfig(
            lightScheme = NeoGlassLight,
            darkScheme = NeoGlassDark,
            typography = poppinsInterTypography(),
            shapes = neoGlassShapes()
        ),
        AppTheme.RETRO_CHALKBOARD to ThemeConfig(
            lightScheme = RetroChalkboardLight,
            darkScheme = RetroChalkboardDark,
            typography = robotoSlabTypography(),
            shapes = pastelShapes()
        ),
        AppTheme.NATURE_EARTHY to ThemeConfig(
            lightScheme = NatureEarthyLight,
            darkScheme = NatureEarthyDark,
            typography = latoMerriweatherTypography(),
            shapes = minimalShapes()
        ),
        AppTheme.PRODUCTIVITY_DASHBOARD to ThemeConfig(
            lightScheme = ProductivityDashboardLight,
            darkScheme = ProductivityDashboardDark,
            typography = interMonoTypography(),
            shapes = academicShapes()
        )
    )
}
