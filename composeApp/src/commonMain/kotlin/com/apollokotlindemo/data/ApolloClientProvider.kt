package com.apollokotlindemo.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.apollographql.apollo.network.websocket.WebSocketNetworkTransport
import com.apollokotlindemo.getPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlin.concurrent.Volatile

/**
 * Singleton provider for Apollo GraphQL client
 * Ensures client is created only once and reused across the app
 */
object ApolloClientProvider {

  @Volatile
  private var INSTANCE: ApolloClient? = null

  // Environment-based configuration for different platforms
  private val isProduction = true // Set this based on your build configuration

  // Use local network IP for Android devices and emulators
  private val baseUrl = when {
    isAndroidDevice() -> "http://<local network ip>:8080"  // Your actual local network IP
    isProduction -> "https://apollo-kotlin-demo.onrender.com"
    else -> "http://localhost:8080"  // Desktop/web
  }

  private val wsUrl = when {
    isAndroidDevice() -> "ws://<local network ip>:8080"   // Your actual local network IP
    isProduction -> "wss://apollo-kotlin-demo.onrender.com"
    else -> "ws://localhost:8080"  // Desktop/web
  }

  fun getClient(): ApolloClient {
    return INSTANCE ?: createAndSetClient()
  }

  private fun createAndSetClient(): ApolloClient {
    if (INSTANCE == null) {
      INSTANCE = createApolloClient()
      println("Apollo Client created for $baseUrl - this should only happen once")
      println("Platform: ${getPlatform().name}")
    }
    return INSTANCE!!
  }

  @OptIn(ApolloExperimental::class)
  private fun createApolloClient(): ApolloClient {
    return ApolloClient.Builder()
      .serverUrl("$baseUrl/graphql")
      .addInterceptor(LoggingIntercepter())
      .subscriptionNetworkTransport(
        WebSocketNetworkTransport.Builder()
          .serverUrl("$wsUrl/subscriptions")
          .build()
      )
      .build()
  }

  private class LoggingIntercepter : ApolloInterceptor {
    override fun <D : Operation.Data> intercept(
      request: ApolloRequest<D>,
      chain: ApolloInterceptorChain
    ): Flow<ApolloResponse<D>> {
      // Log request details
      println("Apollo Request: ${request.operation.name()} - ${request.operation.document()}")

      // Proceed with the interceptor chain
      return chain.proceed(request).onEach {
        println("Apollo Response: ${it.operation.name()} - ${it.data}")
      }
    }
  }


  /**
   * For testing or when you need to reset the client
   */
  fun resetClient() {
    INSTANCE?.close()
    INSTANCE = null
  }

  private fun isAndroidDevice(): Boolean {
    // Check if running on Android (both emulator and real device)
    return getPlatform().name.contains("Android", ignoreCase = true)
  }
}