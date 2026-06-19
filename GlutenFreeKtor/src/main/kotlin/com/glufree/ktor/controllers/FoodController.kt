package com.glufree.ktor.controllers

import com.glufree.ktor.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Route.foodRoutes() {
    
    // GET /
    get("/") {
        call.respondText("API Gluten Free funcionando 🚀", contentType = ContentType.Text.Plain)
    }

    route("/foods") {
        
        // GET /foods
        get {
            val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val per = call.request.queryParameters["per"]?.toIntOrNull()?.coerceAtLeast(1) ?: 20

            val limit = per
            val offset = ((page - 1) * per).toLong()

            val pageResponse = newSuspendedTransaction(Dispatchers.IO) {
                val totalCount = FoodsTable.selectAll().count()
                
                val items = FoodsTable.selectAll()
                    .limit(limit, offset = offset)
                    .map { mapFoodRow(it) }

                PageResponse(
                    items = items,
                    metadata = PaginationMetadata(
                        page = page,
                        per = per,
                        total = totalCount
                    )
                )
            }

            call.respond(pageResponse)
        }

        // GET /foods/search
        get("/search") {
            val searchTerm = call.request.queryParameters["q"]
            if (searchTerm.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = true, reason = "Missing or empty search query parameter 'q'")
                )
                return@get
            }

            val page = call.request.queryParameters["page"]?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val per = call.request.queryParameters["per"]?.toIntOrNull()?.coerceAtLeast(1) ?: 20

            val limit = per
            val offset = ((page - 1) * per).toLong()
            val searchPattern = "%$searchTerm%"

            val pageResponse = newSuspendedTransaction(Dispatchers.IO) {
                // Perform case-insensitive search by using .lowerCase() and %pattern% on name, categories, and brand.
                // Using .or to match if any condition is satisfied.
                val searchExpression = (FoodsTable.name.lowerCase() like searchPattern.lowercase()) or
                        (FoodsTable.categories.lowerCase() like searchPattern.lowercase()) or
                        (FoodsTable.brand.lowerCase() like searchPattern.lowercase())

                val totalCount = FoodsTable.selectAll().where { searchExpression }.count()

                val items = FoodsTable.selectAll().where { searchExpression }
                    .limit(limit, offset = offset)
                    .map { mapFoodRow(it) }

                PageResponse(
                    items = items,
                    metadata = PaginationMetadata(
                        page = page,
                        per = per,
                        total = totalCount
                    )
                )
            }

            call.respond(pageResponse)
        }

        // GET /foods/{foodID}
        get("/{foodID}") {
            val foodIdString = call.parameters["foodID"]
            if (foodIdString.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Missing or empty foodID"))
                return@get
            }

            val idInt = foodIdString.toIntOrNull()
            if (idInt == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Invalid ID format (expected integer)"))
                return@get
            }

            val food = newSuspendedTransaction(Dispatchers.IO) {
                FoodsTable.selectAll()
                    .where { FoodsTable.id eq idInt }
                    .map { mapFoodRow(it) }
                    .firstOrNull()
            }

            if (food != null) {
                call.respond(food)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(error = true, reason = "Food not found"))
            }
        }
    }
}
