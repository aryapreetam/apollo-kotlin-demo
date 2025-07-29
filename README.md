# Apollo Kotlin GraphQL Subscriptions Demo

This project demonstrates **GraphQL subscriptions working with WebAssembly (WasmJS) target** in Apollo Kotlin. It uses
a custom Apollo Kotlin build with WASM support that is automatically built from source in CI.

This was created as a demo for [PR Link](https://github.com/apollographql/apollo-kotlin/pull/6637)

## 🚀 What This Project Demonstrates

- **GraphQL Subscriptions** working across all Kotlin Multiplatform targets including **WasmJS**
- **Real-time communication** between GraphQL server and clients using WebSockets
- **Multiplatform GraphQL client** implementation with Apollo Kotlin
- **Cross-platform UI** built with Compose Multiplatform
- **CI/CD pipeline** that builds Apollo Kotlin from source with WASM support

## 📱 Supported Platforms

- **Android** - Native Android app
- **iOS** - Native iOS app
- **Desktop** - JVM desktop application (macOS, Windows, Linux)
- **Web (WasmJS)** - WebAssembly web application (requires custom Apollo build)
- **Server** - Ktor GraphQL server with subscription support

## 🏗️ Project Structure

### `/server`

Ktor-based GraphQL server providing:

- GraphQL queries, mutations, and **subscriptions**
- WebSocket support for real-time subscriptions
- CORS configuration for cross-platform access
- GraphiQL playground at `/graphiql`

### `/composeApp`

Compose Multiplatform client application supporting:

- **Android** target
- **iOS** targets (iosX64, iosArm64, iosSimulatorArm64)
- **Desktop (JVM)** target
- **Web (WasmJS)** target - *The main focus of this demo*

### `/shared`

Shared business logic and models used across all client platforms.

### `/iosApp`

iOS-specific entry point and configuration for the native iOS application.

## 🛠️ Setup & Installation

### Prerequisites

- **JDK 17+** (required for Apollo Kotlin build)
- **Android Studio** (for Android builds)
- **Xcode** (for iOS builds, macOS only)
- **Node.js** (for web builds)

### Clone the Repository

```bash
git clone https://github.com/your-username/apollo-kotlin-demo.git
cd apollo-kotlin-demo
```

### Apollo Kotlin WASM Support

This project uses Apollo Kotlin `5.0.0-alpha.local-SNAPSHOT` with WASM support. The CI automatically:

1. **Builds Apollo Kotlin from source** with the latest WASM support
2. **Publishes to local Maven** repository
3. **Uses the fresh build** for WasmJS compilation

For local development, you can either:

- **Use the CI**: Push changes and let GitHub Actions build everything
- **Build Apollo locally**: Follow the steps in `LOCAL_CI_SETUP.md`

## 🚀 Running the Project

### 1. Start the GraphQL Server

```bash
./gradlew :server:run
```

The server will start at `http://localhost:8080`

- GraphQL endpoint: `http://localhost:8080/graphql`
- GraphiQL playground: `http://localhost:8080/graphiql`

### 2. Run Client Applications

#### Android

```bash
./gradlew :composeApp:installDebug
# Or open in Android Studio and run
```

#### iOS

```bash
./gradlew :composeApp:iosSimulatorArm64App
# Or open iosApp/iosApp.xcodeproj in Xcode
```

#### Desktop

```bash
./gradlew :composeApp:run
```

#### Web (WasmJS) - **Main Demo Target**

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Access at `http://localhost:8080` (different port from server)

## 🌐 Live Demo

### Deployed Server

The GraphQL server is deployed at: **https://apollo-kotlin-demo.onrender.com/graphql**

- Use this endpoint in clients to test without running the server locally
- GraphiQL playground: **https://apollo-kotlin-demo.onrender.com/graphiql**

### Web App (WasmJS)

Live WasmJS demo: **https://aryapreetam.github.io/apollo-kotlin-demo**

### Downloadable Executables

Pre-built executables will be available in the [Releases](../../releases) section:(TBD)

- **Android APK**
- **Desktop applications** (macOS .dmg, Windows .msi, Linux .deb)
- **iOS IPA** (for testing on devices)

## 📹 Demo Video

<table>
  <tr>
    <td style="border-bottom:1px solid #ccc;">web(wasmJs)</td>
    <td rowspan="2">android</td>
    <td rowspan="2">ios</td>
  </tr>
  <tr>
    <td>desktop</td>
  </tr>
</table>

https://github.com/user-attachments/assets/f394db05-c9d0-40da-9183-cde85d480e56

Websockets with binary data(send/receive) in wasmJs:

https://github.com/user-attachments/assets/5bf70590-7f40-436e-8194-0368bd1b3862

## 🔧 Development

### Local Development Setup

1. **Server**: Use `localhost:8080` when testing locally
2. **Android**: Use your computer's IP address (e.g., `192.168.1.xxx:8080`) for device testing
3. **iOS**: Configure network permissions in `Info.plist` for local server access
4. **Web**: Served on a different port, but connects to GraphQL server on port 8080

### Building for Production

#### Android APK

```bash
./gradlew :composeApp:assembleRelease
```

#### Desktop Executables

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

#### Web (WasmJS) Build

```bash
./gradlew :composeApp:wasmJsBrowserProductionWebpack
```

## 🧪 Testing GraphQL Subscriptions

1. **Start the server** with `./gradlew :server:run`
2. **Open GraphiQL** at `http://localhost:8080/graphiql`
3. **Test subscription**:
   ```graphql
   subscription {
     stringListUpdates
   }
   ```
4. **Trigger updates** with mutations:
   ```graphql
   mutation {
     addString(value: "Hello World")
   }
   ```
5. **Run any client** and observe real-time updates

## 🎯 Key Features Demonstrated

- ✅ **GraphQL Subscriptions on WasmJS** (main contribution)
- ✅ **WebSocket communication** across all platforms
- ✅ **Real-time UI updates** with Compose Multiplatform
- ✅ **Cross-platform GraphQL client** with Apollo Kotlin
- ✅ **Type-safe GraphQL operations** generated from schema

## 🤝 Contributing

This project serves as a demonstration for GraphQL subscriptions support in Apollo Kotlin's WasmJS target. Feel free to:

- Test the implementation
- Report issues
- Suggest improvements
- Use as reference for your own projects

## 📚 Learn More

- [Apollo Kotlin Documentation](https://www.apollographql.com/docs/kotlin/)
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- [Kotlin/Wasm](https://kotl.in/wasm/)

## 📄 License

This project is open source and available under the [MIT License](LICENSE).