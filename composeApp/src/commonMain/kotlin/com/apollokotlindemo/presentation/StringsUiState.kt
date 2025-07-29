package com.apollokotlindemo.presentation

/**
 * UI state for the strings screen
 */
data class StringsUiState(
  val strings: List<String> = emptyList(),
  val inputText: String = "",
  val editingIndex: Int? = null,
  val editText: String = "",
  val isInitialLoading: Boolean = false,
  val isAddingString: Boolean = false,
  val isUpdatingString: Boolean = false,
  val isDeletingString: Boolean = false,
  val errorMessage: String? = null,
  val snackbarMessage: String? = null // New state for triggering snackbar with received WebSocket message
) {
  val isInputValid: Boolean
    get() = inputText.trim().isNotEmpty()

  val isEditTextValid: Boolean
    get() = editText.trim().isNotEmpty()

  val hasStrings: Boolean
    get() = strings.isNotEmpty()

  val showEmptyState: Boolean
    get() = strings.isEmpty() && !isInitialLoading
}