package com.apollokotlindemo

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Service to manage string list operations with reactive updates
 */
class StringListService {
  private val stringList = mutableListOf<String>()
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
    _stringListUpdates.tryEmit(stringList)
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
      _stringListUpdates.tryEmit(stringList)
      return true
    }
    return false
  }

  companion object {
    val instance = StringListService()
  }
}