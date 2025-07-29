package com.apollokotlindemo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.network.websocket.WebSocketEngine
import com.apollographql.apollo.network.websocket.WebSocketListener
import com.apollokotlindemo.data.ApolloClientProvider.wsUrl
import com.apollokotlindemo.data.StringsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.use

/**
 * ViewModel for managing strings list screen state and business logic
 */
@OptIn(ApolloExperimental::class)
class StringsViewModel(
  private val repository: StringsRepository = StringsRepository()
) : ViewModel() {

  private val _uiState = MutableStateFlow(StringsUiState())
  val uiState: StateFlow<StringsUiState> = _uiState.asStateFlow()

  // Store the subscription job for manual cancellation
  private var subscriptionJob: Job? = null

  // WebSocket instance for persistent connection
  private var webSocket: com.apollographql.apollo.network.websocket.WebSocket? = null

  // Channel for receiving WebSocket messages
  private val messageChannel = Channel<String>(Channel.UNLIMITED)

  init {
    println("ViewModel: Initializing StringsViewModel")
    loadStrings()
    subscribeToNotifications()
    println("ViewModel: StringsViewModel initialization completed")
  }

  @OptIn(ApolloExperimental::class)
  private fun subscribeToNotifications() {
    viewModelScope.launch {
      // Create and open the WebSocket connection
      val engine = WebSocketEngine()
      webSocket = engine.newWebSocket(
        "${wsUrl}/ws",
        headers = listOf(),
        listener = object : WebSocketListener {
          override fun onOpen() {
            println("WebSocket opened")
          }

          override fun onMessage(text: String) {
            println("onMessage (text): $text")
            messageChannel.trySend(text)
          }

          override fun onMessage(data: ByteArray) {
            val message = data.decodeToString()
            println("onMessage (binary): ${data.size} bytes, decoded: $message")
            messageChannel.trySend(message)
          }

          override fun onError(cause: ApolloException) {
            println("onError: ${cause.message}")
          }

          override fun onClosed(code: Int?, reason: String?) {
            println("onClosed: $code $reason")
          }
        }
      )

      // Launch a collector for the channel to update UI state
      launch {
        for (message in messageChannel) {
          _uiState.value = _uiState.value.copy(snackbarMessage = message)
          // Clear after a short delay to allow UI to show it briefly
          delay(2000) // Adjust duration as needed
          _uiState.value = _uiState.value.copy(snackbarMessage = null)
        }
      }
    }
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
      // Send WebSocket message to server
      val payload = "Action: adding ${currentState.inputText.trim()}".encodeToByteArray()
      webSocket?.send(payload)
      
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
      // Send WebSocket message to server
      val payload = "Action: updating data at index $editingIndex to ${currentState.editText.trim()}".encodeToByteArray()
      webSocket?.send(payload)
      
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
      // Send WebSocket message to server
      val payload = "Action: deleting item at index $index".encodeToByteArray()
      webSocket?.send(payload)
      
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

    // Launch text-based subscription in its own coroutine
    val textSubscriptionJob = viewModelScope.launch {
      repository.subscribeToStringUpdates()
        .catch { error ->
          println("ViewModel: Text subscription error - ${error.message}")
          _uiState.value = _uiState.value.copy(
            errorMessage = "Subscription error: ${error.message}"
          )
        }
        .collect { updatedStrings ->
          println("ViewModel: Received text subscription update with ${updatedStrings.size} strings")
          _uiState.value = _uiState.value.copy(strings = updatedStrings)
        }
    }

    // Store both jobs (you might want to store them separately if needed)
    subscriptionJob = textSubscriptionJob

    println("ViewModel: Both subscription jobs created")
  }

  /**
   * Called when the ViewModel is about to be destroyed
   * This is where you should clean up resources
   */
  override fun onCleared() {
    super.onCleared()
    println("ViewModel: onCleared() called - cleaning up resources")
    stopSubscription()
    webSocket?.close(1000, "ViewModel cleared") // Normal close
    webSocket = null
    messageChannel.close()
    println("ViewModel: Subscription and WebSocket cleanup completed")
  }
}
