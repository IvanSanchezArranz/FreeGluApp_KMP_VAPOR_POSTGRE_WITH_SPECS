package com.glufree.ktor.configure

import com.glufree.ktor.controllers.foodRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        foodRoutes()
    }
}
