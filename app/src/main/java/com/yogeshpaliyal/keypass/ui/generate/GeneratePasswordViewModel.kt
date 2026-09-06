package com.yogeshpaliyal.keypass.ui.generate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yogeshpaliyal.common.data.PasswordConfig
import com.yogeshpaliyal.common.utils.PasswordGenerator
import com.yogeshpaliyal.common.utils.getUserSettings
import com.yogeshpaliyal.common.utils.setPasswordConfig
import com.yogeshpaliyal.keypass.ui.generate.ui.components.MAX_GENERATED_PASSWORD_LENGTH
import com.yogeshpaliyal.keypass.ui.generate.ui.components.MIN_GENERATED_PASSWORD_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.math.roundToInt
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneratePasswordViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val _viewState = MutableStateFlow(PasswordConfig.Initial)
    val viewState = _viewState.asStateFlow()

    init {
        observeState(context)
    }

    fun retrieveSavedPasswordConfig(context: Context) {
        viewModelScope.launch {
            val passwordConfig = context.getUserSettings().passwordConfig
            _viewState.value = passwordConfig.sanitizedForGenerator()
            generatePassword()
        }
    }

    fun generatePassword() {
        val currentViewState = _viewState.value.sanitizedForGenerator(
            preservePassword = true
        )
        val newPassword = PasswordGenerator(currentViewState).generatePassword()
        _viewState.value = currentViewState.copy(password = newPassword)
    }

    fun clearGeneratedPassword() {
        _viewState.update {
            it.copy(password = "")
        }
    }

    fun selectSymbolForPassword(symbol: Char) {
        val tempList = _viewState.value.listOfSymbols.toMutableList()

        if (tempList.size == PasswordGenerator.totalSymbol.size && tempList.contains(symbol)) {
            tempList.clear()
        }
        if (symbol == 's') {
            _viewState.update {
                it.copy(
                    listOfSymbols = PasswordGenerator.totalSymbol
                )
            }
            return
        }
        if (tempList.contains(symbol)) {
            tempList.remove(symbol)

            if (tempList.isEmpty()) {
                selectSymbolForPassword('s')
                return
            }
        } else {
            tempList.add(symbol)
        }

        _viewState.update {
            it.copy(
                listOfSymbols = tempList.toList()
            )
        }
    }

    fun onPasswordLengthSliderChange(value: Float) {
        val normalizedValue = value
            .roundToInt()
            .toFloat()
            .coerceIn(
                MIN_GENERATED_PASSWORD_LENGTH,
                MAX_GENERATED_PASSWORD_LENGTH
            )
        updateAndGenerate {
            it.copy(length = normalizedValue)
        }
    }

    fun onUppercaseCheckedChange(checked: Boolean) {
        updateCategoryAndGenerate(
            checked = checked,
            currentlyEnabled = _viewState.value.includeUppercaseLetters
        ) {
            it.copy(includeUppercaseLetters = checked)
        }
    }

    fun onLowercaseCheckedChange(checked: Boolean) {
        updateCategoryAndGenerate(
            checked = checked,
            currentlyEnabled = _viewState.value.includeLowercaseLetters
        ) {
            it.copy(includeLowercaseLetters = checked)
        }
    }

    fun onNumbersCheckedChange(checked: Boolean) {
        updateCategoryAndGenerate(
            checked = checked,
            currentlyEnabled = _viewState.value.includeNumbers
        ) {
            it.copy(includeNumbers = checked)
        }
    }

    fun onSymbolsCheckedChange(checked: Boolean) {
        updateCategoryAndGenerate(
            checked = checked,
            currentlyEnabled = _viewState.value.includeSymbols
        ) {
            it.copy(includeSymbols = checked)
        }
    }

    fun onBlankSpacesCheckedChange(checked: Boolean) {
        if (checked) return
        _viewState.update {
            it.copy(includeBlankSpaces = false)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeState(context: Context) {
        viewModelScope.launch {
            _viewState
                .debounce(400)
                .collectLatest { state ->
                    context.setPasswordConfig(
                        state.copy(
                            includeBlankSpaces = false,
                            password = ""
                        )
                    )
                }
        }
    }

    private fun updateAndGenerate(
        transform: (PasswordConfig) -> PasswordConfig
    ) {
        _viewState.update(transform)
        generatePassword()
    }

    private fun updateCategoryAndGenerate(
        checked: Boolean,
        currentlyEnabled: Boolean,
        transform: (PasswordConfig) -> PasswordConfig
    ) {
        if (
            currentlyEnabled &&
            !checked &&
            _viewState.value.enabledCategoryCount() <= 1
        ) {
            return
        }
        updateAndGenerate(transform)
    }

    private fun PasswordConfig.enabledCategoryCount(): Int = listOf(
        includeUppercaseLetters,
        includeLowercaseLetters,
        includeNumbers,
        includeSymbols
    ).count { it }

    private fun PasswordConfig.sanitizedForGenerator(
        preservePassword: Boolean = false
    ): PasswordConfig {
        val normalizedLength = length
            .roundToInt()
            .toFloat()
            .coerceIn(
                MIN_GENERATED_PASSWORD_LENGTH,
                MAX_GENERATED_PASSWORD_LENGTH
            )
        val hasEnabledCategory = enabledCategoryCount() > 0

        return copy(
            length = normalizedLength,
            includeLowercaseLetters = if (hasEnabledCategory) {
                includeLowercaseLetters
            } else {
                true
            },
            includeBlankSpaces = false,
            password = if (preservePassword) password else ""
        )
    }
}
