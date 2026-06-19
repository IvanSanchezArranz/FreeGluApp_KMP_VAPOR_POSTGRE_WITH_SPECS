package com.glufree.ktor

import com.glufree.ktor.configure.*
import com.glufree.ktor.di.appModule
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val host = "0.0.0.0"
    
    LoggerFactory.getLogger("Application").info("Starting GlutenFreeKtor Server on http://$host:$port...")
    
    embeddedServer(Netty, port = port, host = host, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Dependency Injection with Koin
    install(Koin) {
        modules(appModule)
    }

    // 2. Database Connection Pooling with HikariCP & Exposed ORM
    configureDatabase()

    // 3. CORS Configurations
    configureCORS()

    // 4. Content Negotiation and JSON Serialization
    configureSerialization()

    // 5. Routing Layer
    configureRouting()
}
