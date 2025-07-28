package com.apollokotlindemo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Service to manage string list operations with reactive updates
 */
class StringListService {
  private val stringList = mutableListOf<String>()
  private val serviceScope = CoroutineScope(Dispatchers.Default)

  private val _stringListUpdates = MutableSharedFlow<List<String>>(
    replay = 1, // Keep the last value for new subscribers
    extraBufferCapacity = 0
  )
  val stringListUpdates: SharedFlow<List<String>> = _stringListUpdates.asSharedFlow()

  // Binary updates as Base64-encoded JSON strings
  val binaryStringListUpdates: SharedFlow<String> = _stringListUpdates
    .map { stringList: List<String> ->
      val jsonString = Json.encodeToString(stringList)
      Base64.getEncoder().encodeToString(jsonString.toByteArray())
    }
    .shareIn(serviceScope, SharingStarted.Eagerly, replay = 1)

  init {
    // Add some initial data for testing
    stringList.addAll(listOf("Hello", "World", "GraphQL"))
    // Emit initial state
    _stringListUpdates.tryEmit(stringList.toList())
  }

  // Get current list of strings
  fun getStrings(): List<String> = stringList.toList()

  // Add new string to the list
  fun addString(value: String): Boolean {
    stringList.add(0, value)
    if(stringList.size > 10){
      stringList.removeAt(11)
    }
    _stringListUpdates.tryEmit(stringList.toList())
    return true
  }

  // Update existing string at index
  fun updateString(index: Int, newValue: String): Boolean {
    if (index >= 0 && index < stringList.size) {
      stringList[index] = newValue
      val currentList = stringList.toList()
      _stringListUpdates.tryEmit(currentList)
      return true
    }
    return false
  }

  // Update existing string by old value
  fun deleteString(index: Int): Boolean {
    if (index >= 0 && index < stringList.size) {
      stringList.removeAt(index)
      val currentList = stringList.toList()
      _stringListUpdates.tryEmit(currentList)
      return true
    }
    return false
  }
  
  companion object {
    val instance = StringListService()
  }
}