package com.apollokotlindemo

import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.flow.Flow

/**
 * GraphQL subscription operations for string list changes
 */
class StringListSubscription : Subscription {

  /**
   * Subscribe to string list changes
   * Returns a Flow that emits the updated list whenever changes occur
   */
  fun stringListChanges(): Flow<List<String>> {
    return StringListService.instance.stringListUpdates
  }
}