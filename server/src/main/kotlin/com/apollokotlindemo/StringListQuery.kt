package com.apollokotlindemo

import com.expediagroup.graphql.server.operations.Query
import kotlinx.serialization.json.Json
import java.util.*

class StringListQuery : Query {

  fun strings(): List<String> {
    return StringListService.instance.getStrings()
  }

  /**
   * Returns the string list as Base64-encoded binary data
   */
  fun binaryStrings(): String {
    val list = StringListService.instance.getStrings()
    val jsonString = Json.encodeToString(list)
    return Base64.getEncoder().encodeToString(jsonString.toByteArray())
  }
}