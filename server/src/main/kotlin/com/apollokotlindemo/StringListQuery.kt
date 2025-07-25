package com.apollokotlindemo

import com.expediagroup.graphql.server.operations.Query

class StringListQuery : Query {

  fun strings(): List<String> {
    return StringListService.instance.getStrings()
  }
}