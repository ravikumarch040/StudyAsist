package com.studyasist.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography

@Composable
fun WearTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        typography = Typography(),
        content = content
    )
}
