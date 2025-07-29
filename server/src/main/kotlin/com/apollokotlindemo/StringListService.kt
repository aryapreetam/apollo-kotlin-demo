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
  private val _events = MutableSharedFlow<String>(extraBufferCapacity = 64)
  val events: Flow<String> get() = _events.asSharedFlow()

  private val _stringListUpdates = MutableSharedFlow<List<String>>(
    replay = 1, // Keep the last value for new subscribers
    extraBufferCapacity = 0
  )
  val stringListUpdates: SharedFlow<List<String>> = _stringListUpdates.asSharedFlow()

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
    _events.tryEmit("Item added: $value")
    return true
  }

  // Update existing string at index
  fun updateString(index: Int, newValue: String): Boolean {
    if (index >= 0 && index < stringList.size) {
      stringList[index] = newValue
      val currentList = stringList.toList()
      _stringListUpdates.tryEmit(currentList)
      _events.tryEmit("Item updated: $newValue")
      return true
    }
    return false
  }

  // Update existing string by old value
  fun deleteString(index: Int): Boolean {
    if (index >= 0 && index < stringList.size) {
      val removedItem = stringList.removeAt(index)
      val currentList = stringList.toList()
      _stringListUpdates.tryEmit(currentList)
      _events.tryEmit("Item removed: $removedItem")
      return true
    }
    return false
  }
  
  companion object {
    val instance = StringListService()
  }
}