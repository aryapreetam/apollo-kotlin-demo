package com.apollokotlindemo

import com.expediagroup.graphql.server.operations.Mutation

class StringListMutation : Mutation {

  fun addString(value: String): Boolean {
    return StringListService.instance.addString(value)
  }

  fun updateStringByIndex(index: Int, newValue: String): Boolean {
    return StringListService.instance.updateString(index, newValue)
  }

  fun deleteString(index: Int): Boolean {
    return StringListService.instance.deleteString(index)
  }
}