package com.studyasist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.studyasist.R

val GoogleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private fun googleFontFamily(name: String): FontFamily {
    val font = GoogleFont(name)
    return FontFamily(
        Font(googleFont = font, fontProvider = GoogleFontProvider, weight = FontWeight.Normal),
        Font(googleFont = font, fontProvider = GoogleFontProvider, weight = FontWeight.Medium),
        Font(googleFont = font, fontProvider = GoogleFontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = font, fontProvider = GoogleFontProvider, weight = FontWeight.Bold)
    )
}

val InterFamily = googleFontFamily("Inter")
val PoppinsFamily = googleFontFamily("Poppins")
val MontserratFamily = googleFontFamily("Montserrat")
val MerriweatherFamily = googleFontFamily("Merriweather")
val LoraFamily = googleFontFamily("Lora")
val LatoFamily = googleFontFamily("Lato")
val RobotoSlabFamily = googleFontFamily("Roboto Slab")
val RobotoMonoFamily = googleFontFamily("Roboto Mono")

private fun buildTypography(
    bodyFamily: FontFamily,
    headlineFamily: FontFamily = bodyFamily,
    baseBodySize: Int = 16,
    baseLabelSize: Int = 12
): Typography = Typography(
    displayLarge = TextStyle(fontFamily = headlineFamily, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = headlineFamily, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = headlineFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = headlineFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = headlineFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = headlineFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = headlineFamily, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Medium, fontSize = baseBodySize.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Normal, fontSize = baseBodySize.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Normal, fontSize = baseLabelSize.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Medium, fontSize = baseLabelSize.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = bodyFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
)

internal fun interRobotoTypography() = buildTypography(bodyFamily = InterFamily)

internal fun poppinsMerriweatherTypography() = buildTypography(
    bodyFamily = PoppinsFamily,
    headlineFamily = MerriweatherFamily
)

internal fun merriweatherRobotoTypography() = buildTypography(
    bodyFamily = InterFamily,
    headlineFamily = MerriweatherFamily
)

internal fun montserratInterTypography() = buildTypography(
    bodyFamily = InterFamily,
    headlineFamily = MontserratFamily
)

internal fun interLargeTypography() = buildTypography(
    bodyFamily = InterFamily,
    baseBodySize = 18,
    baseLabelSize = 14
)

internal fun poppinsInterTypography() = buildTypography(
    bodyFamily = InterFamily,
    headlineFamily = PoppinsFamily
)

internal fun robotoSlabTypography() = buildTypography(
    bodyFamily = InterFamily,
    headlineFamily = RobotoSlabFamily
)

internal fun latoMerriweatherTypography() = buildTypography(
    bodyFamily = LatoFamily,
    headlineFamily = MerriweatherFamily
)

internal fun interMonoTypography(): Typography {
    val base = buildTypography(bodyFamily = InterFamily)
    return base.copy(
        labelLarge = base.labelLarge.copy(fontFamily = RobotoMonoFamily),
        labelMedium = base.labelMedium.copy(fontFamily = RobotoMonoFamily),
        labelSmall = base.labelSmall.copy(fontFamily = RobotoMonoFamily)
    )
}
