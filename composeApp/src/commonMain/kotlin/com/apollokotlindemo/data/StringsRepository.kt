package com.apollokotlindemo.data

import com.apollographql.apollo.ApolloClient
import com.apollokotlindemo.AddStringMutation
import com.apollokotlindemo.BinaryStringListUpdatesSubscription
import com.apollokotlindemo.DeleteStringMutation
import com.apollokotlindemo.GetStringsQuery
import com.apollokotlindemo.StringListUpdatesSubscription
import com.apollokotlindemo.UpdateStringMutation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

/**
 * Repository class that handles all GraphQL operations and data access
 */
class StringsRepository {

  // Use singleton Apollo client for better performance
  private val apolloClient = ApolloClientProvider.getClient()

  /**
   * Fetch all strings from the server
   */
  suspend fun getStrings(): Result<List<String>> {
    return try {
      val response = apolloClient.query(GetStringsQuery()).execute()
      val strings = response.data?.strings?.filterNotNull() ?: emptyList()
      Result.success(strings)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Add a new string to the server
   */
  suspend fun addString(value: String): Result<Boolean> {
    return try {
      val response = apolloClient.mutation(AddStringMutation(value)).execute()
      val success = response.data?.addString ?: false
      if (success) {
        Result.success(true)
      } else {
        Result.failure(Exception("Failed to add string"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Update a string at the specified index
   */
  suspend fun updateString(index: Int, newValue: String): Result<Boolean> {
    return try {
      val response = apolloClient.mutation(UpdateStringMutation(index, newValue)).execute()
      val success = response.data?.updateStringByIndex ?: false
      if (success) {
        Result.success(true)
      } else {
        Result.failure(Exception("Failed to update string"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Delete a string at the specified index
   */
  suspend fun deleteString(index: Int): Result<Boolean> {
    return try {
      val response = apolloClient.mutation(DeleteStringMutation(index)).execute()
      val success = response.data?.deleteString ?: false
      if (success) {
        Result.success(true)
      } else {
        Result.failure(Exception("Failed to delete string"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Subscribe to string list updates using WebSocket subscription
   */
  fun subscribeToStringUpdates(): Flow<List<String>> {
    return apolloClient.subscription(StringListUpdatesSubscription())
      .toFlow()
      .catch { e ->
        println("WebSocket subscription error: ${e.message}")
        throw e
      }
      .map { response ->
        println("Received WebSocket subscription update: ${response.data?.stringListChanges}")
        response.data?.stringListChanges?.filterNotNull() ?: emptyList()
      }
  }

  /**
   * Subscribe to binary string list updates using WebSocket subscription
   */
  fun binarySubscribeToStringUpdates(): Flow<String> {
    println("binarySubscribeToStringUpdates()")
    return apolloClient.subscription(BinaryStringListUpdatesSubscription())
      .toFlow()
      .catch { e ->
        println("WebSocket subscription error: ${e.message}")
        throw e
      }
      .map { response ->
        val base64StringData = response.data?.binaryStringListChanges ?: ""
        println("Received binary subscription - Base64 length: ${base64StringData.length}")

        try {
          // Decode Base64 to get JSON string
          val decodedBytes = Base64.decode(base64StringData)
          val jsonString = decodedBytes.decodeToString()
          println("Decoded JSON: $jsonString")

          // Deserialize JSON to List<String>
          val stringList = Json.decodeFromString<List<String>>(jsonString)
          println("Deserialized list: $stringList")

          "Binary data decoded successfully: $stringList"
        } catch (e: Exception) {
          println("Error decoding binary data: ${e.message}")
          "Error decoding binary data: ${e.message}"
        }
      }
  }
}