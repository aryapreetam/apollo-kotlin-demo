package com.apollokotlindemo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollokotlindemo.data.StringsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for managing strings list screen state and business logic
 */
class StringsViewModel(
  private val repository: StringsRepository = StringsRepository()
) : ViewModel() {

  private val _uiState = MutableStateFlow(StringsUiState())
  val uiState: StateFlow<StringsUiState> = _uiState.asStateFlow()

  // Store the subscription job for manual cancellation
  private var subscriptionJob: Job? = null

  init {
    println("ViewModel: Initializing StringsViewModel")
    loadStrings()
    println("ViewModel: StringsViewModel initialization completed")
  }

  /**
   * Load initial strings from the server
   */
  fun loadStrings() {
    println("ViewModel: Starting loadStrings")
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isInitialLoading = true, errorMessage = null)
      println("ViewModel: Set initial loading state to true")

      repository.getStrings()
        .onSuccess { strings ->
          println("ViewModel: getStrings successful with ${strings.size} items")
          _uiState.value = _uiState.value.copy(
            strings = strings,
            isInitialLoading = false,
            errorMessage = null
          )
          subscribeToUpdates()
        }
        .onFailure { error ->
          println("ViewModel: getStrings failed - ${error.message}")
          _uiState.value = _uiState.value.copy(
            isInitialLoading = false,
            errorMessage = "Failed to load strings: ${error.message}"
          )
        }
    }
  }

  /**
   * Update the input text
   */
  fun updateInputText(text: String) {
    _uiState.value = _uiState.value.copy(inputText = text)
  }

  /**
   * Add a new string
   */
  fun addString() {
    val currentState = _uiState.value
    if (!currentState.isInputValid) return

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isAddingString = true, errorMessage = null)

      repository.addString(currentState.inputText.trim())
        .onSuccess {
          _uiState.value = _uiState.value.copy(
            inputText = "",
            isAddingString = false,
            errorMessage = null
          )
        }
        .onFailure { error ->
          _uiState.value = _uiState.value.copy(
            isAddingString = false,
            errorMessage = "Error adding string: ${error.message}"
          )
        }
    }
  }

  /**
   * Start editing a string at the given index
   */
  fun startEditing(index: Int, currentValue: String) {
    _uiState.value = _uiState.value.copy(
      editingIndex = index,
      editText = currentValue
    )
  }

  /**
   * Update the edit text
   */
  fun updateEditText(text: String) {
    _uiState.value = _uiState.value.copy(editText = text)
  }

  /**
   * Save the edited string
   */
  fun saveEdit() {
    val currentState = _uiState.value
    val editingIndex = currentState.editingIndex

    if (editingIndex == null || !currentState.isEditTextValid) return

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isUpdatingString = true, errorMessage = null)

      repository.updateString(editingIndex, currentState.editText.trim())
        .onSuccess {
          _uiState.value = _uiState.value.copy(
            editingIndex = null,
            editText = "",
            isUpdatingString = false,
            errorMessage = null
          )
        }
        .onFailure { error ->
          _uiState.value = _uiState.value.copy(
            isUpdatingString = false,
            errorMessage = "Error updating string: ${error.message}"
          )
        }
    }
  }

  /**
   * Cancel editing
   */
  fun cancelEdit() {
    _uiState.value = _uiState.value.copy(
      editingIndex = null,
      editText = ""
    )
  }

  /**
   * Delete a string at the given index
   */
  fun deleteString(index: Int) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isDeletingString = true, errorMessage = null)

      repository.deleteString(index)
        .onSuccess {
          _uiState.value = _uiState.value.copy(
            isDeletingString = false,
            errorMessage = null
          )
        }
        .onFailure { error ->
          _uiState.value = _uiState.value.copy(
            isDeletingString = false,
            errorMessage = "Error deleting string: ${error.message}"
          )
        }
    }
  }

  /**
   * Clear the current error message
   */
  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }

  /**
   * Manually stop the subscription
   */
  fun stopSubscription() {
    println("ViewModel: Manually stopping subscription")
    subscriptionJob?.cancel()
    subscriptionJob = null
  }

  /**
   * Restart the subscription (useful for reconnection scenarios)
   */
  fun restartSubscription() {
    println("ViewModel: Restarting subscription")
    stopSubscription()
    subscribeToUpdates()
  }

  /**
   * Subscribe to real-time string updates
   */
  private fun subscribeToUpdates() {
    // Cancel existing subscription to prevent multiple subscriptions
    stopSubscription()

    println("ViewModel: Starting subscription to updates")
    subscriptionJob = viewModelScope.launch {
      repository.subscribeToStringUpdates()
        .catch { error ->
          println("ViewModel: Subscription error - ${error.message}")
          _uiState.value = _uiState.value.copy(
            errorMessage = "Subscription error: ${error.message}"
          )
        }
        .collect { updatedStrings ->
          println("ViewModel: Received subscription update with ${updatedStrings.size} strings")
          _uiState.value = _uiState.value.copy(strings = updatedStrings)
        }
    }
    println("ViewModel: Subscription job created with ID: ${subscriptionJob?.hashCode()}")
  }

  /**
   * Called when the ViewModel is about to be destroyed
   * This is where you should clean up resources
   */
  override fun onCleared() {
    super.onCleared()
    println("ViewModel: onCleared() called - cleaning up resources")
    stopSubscription()
    println("ViewModel: Subscription cleanup completed")
  }
}