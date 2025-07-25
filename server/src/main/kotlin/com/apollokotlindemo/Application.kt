package com.apollokotlindemo

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import com.expediagroup.graphql.server.ktor.*
import io.ktor.server.plugins.statuspages.*

fun main() {
  embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
    .start(wait = true)
}

fun Application.module() {
  // Install WebSockets for GraphQL subscriptions
  install(WebSockets)

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