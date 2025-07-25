package com.apollokotlindemo

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import com.expediagroup.graphql.server.ktor.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.*

fun main() {
  val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
  embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
    .start(wait = true)
}

fun Application.module() {
  // Install WebSockets for GraphQL subscriptions
  install(WebSockets)

  // Configure CORS for GraphQL and WebSocket support
  install(CORS) {
    // HTTP Methods
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post) // Required for GraphQL mutations and queries
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)

    // Headers
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType) // Required for GraphQL JSON requests
    allowHeader(HttpHeaders.Accept)
    allowHeader("X-Requested-With")
    allowHeader("Apollo-Require-Preflight") // Apollo Client specific
    allowHeader("X-Apollo-Operation-Name") // Apollo Client operation tracking

    // WebSocket specific headers
    allowHeader(HttpHeaders.SecWebSocketProtocol)
    allowHeader(HttpHeaders.SecWebSocketExtensions)

    allowHost("*")  // For web client

    // Credentials and other settings
    allowCredentials = true
    allowNonSimpleContentTypes = true

    // Max age for preflight requests (optional optimization)
    maxAgeInSeconds = 86400 // 24 hours
  }
  // Configure GraphQL
  install(GraphQL) {
    schema {
      packages = listOf("com.apollokotlindemo")
      queries = listOf(StringListQuery())
      mutations = listOf(StringListMutation())
      subscriptions = listOf(StringListSubscription())
    }
  }

  // Install status pages for GraphQL error handling
  install(StatusPages) {
    defaultGraphQLStatusPages()
  }

  routing {
    get("/") {
      call.respondText("GraphQL Ktor Server is running! Visit /graphiql for GraphiQL playground")
    }
    graphQLGetRoute()
    // GraphQL HTTP endpoint
    graphQLPostRoute()
    // GraphQL subscriptions WebSocket endpoint
    graphQLSubscriptionsRoute()
    // GraphiQL playground
    graphQLSDLRoute()
    graphiQLRoute()
  }
}