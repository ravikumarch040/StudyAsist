package com.studyasist.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the result of an Apple sign-in that was completed via deep link.
 * MainActivity sets this when it receives studyasist://apple-signin; SettingsViewModel observes it.
 */
@Singleton
class AppleSignInResultHolder @Inject constructor() {
    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result.asStateFlow()

    fun setResult(message: String?) {
        _result.value = message
    }

    fun clear() {
        _result.value = null
    }
}
