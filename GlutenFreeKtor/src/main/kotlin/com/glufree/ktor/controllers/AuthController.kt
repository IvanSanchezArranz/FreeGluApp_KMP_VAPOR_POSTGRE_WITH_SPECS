package com.glufree.ktor.controllers

import com.glufree.ktor.models.*
import com.glufree.ktor.security.JwtConfig
import com.glufree.ktor.security.PasswordHasher
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

// Extension to get authenticated user ID from Bearer token
fun ApplicationCall.getAuthenticatedUserId(): UUID? {
    val authHeader = request.headers["Authorization"] ?: return null
    if (!authHeader.startsWith("Bearer ")) return null
    val token = authHeader.removePrefix("Bearer ")
    return try {
        val decoded = JwtConfig.verifyToken(token) ?: return null
        UUID.fromString(decoded.subject)
    } catch (e: Exception) {
        null
    }
}

// Authentication DSL helper for routes
suspend fun ApplicationCall.withAuth(block: suspend (UUID) -> Unit) {
    val userId = getAuthenticatedUserId()
    if (userId == null) {
        respond(HttpStatusCode.Unauthorized, ErrorResponse(error = true, reason = "Bearer token missing, invalid or expired"))
        return
    }
    val userExists = newSuspendedTransaction(Dispatchers.IO) {
        UsersTable.selectAll().where { UsersTable.id eq userId }.count() > 0
    }
    if (!userExists) {
        respond(HttpStatusCode.Unauthorized, ErrorResponse(error = true, reason = "User not found in database"))
        return
    }
    block(userId)
}

fun Route.authRoutes() {

    // POST /register
    post("/register") {
        val input = call.receive<AuthRequest>()
        if (input.email.isBlank() || input.password.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Email and password cannot be empty"))
            return@post
        }

        if (!input.email.contains("@") || !input.email.contains(".")) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Invalid email format"))
            return@post
        }

        if (input.password.length < 8) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Password must be at least 8 characters long"))
            return@post
        }

        try {
            val authResponse = newSuspendedTransaction(Dispatchers.IO) {
                // Check if already registered
                val exists = UsersTable.selectAll().where { UsersTable.email eq input.email }.count() > 0
                if (exists) {
                    return@newSuspendedTransaction null
                }

                val newId = UUID.randomUUID()
                val passwordHash = PasswordHasher.hash(input.password)

                UsersTable.insert {
                    it[id] = newId
                    it[email] = input.email
                    it[this.passwordHash] = passwordHash
                    it[createdAt] = LocalDateTime.now()
                    it[updatedAt] = LocalDateTime.now()
                }

                val token = JwtConfig.generateToken(newId.toString(), input.email)
                AuthResponse(
                    token = token,
                    user = UserResponse(id = newId.toString(), email = input.email)
                )
            }

            if (authResponse != null) {
                call.respond(HttpStatusCode.Created, authResponse)
            } else {
                call.respond(HttpStatusCode.Conflict, ErrorResponse(error = true, reason = "Email is already registered"))
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = true, reason = e.message ?: "An unexpected error occurred"))
        }
    }

    // POST /login
    post("/login") {
        val input = call.receive<AuthRequest>()
        if (input.email.isBlank() || input.password.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Email and password cannot be empty"))
            return@post
        }

        val authResponse = newSuspendedTransaction(Dispatchers.IO) {
            val userRow = UsersTable.selectAll().where { UsersTable.email eq input.email }.firstOrNull()
                ?: return@newSuspendedTransaction null

            val isPasswordCorrect = PasswordHasher.verify(input.password, userRow[UsersTable.passwordHash])
            if (!isPasswordCorrect) {
                return@newSuspendedTransaction null
            }

            val userId = userRow[UsersTable.id].value
            val token = JwtConfig.generateToken(userId.toString(), input.email)
            AuthResponse(
                token = token,
                user = UserResponse(id = userId.toString(), email = input.email)
            )
        }

        if (authResponse != null) {
            call.respond(HttpStatusCode.OK, authResponse)
        } else {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error = true, reason = "Invalid email or password"))
        }
    }

    route("/favorites") {

        // POST /favorites/sync
        post("/sync") {
            call.withAuth { userId ->
                val input = call.receive<SyncFavoritesRequest>()
                var syncedCount = 0

                newSuspendedTransaction(Dispatchers.IO) {
                    for (foodIdStr in input.foodIds) {
                        val foodId = try { UUID.fromString(foodIdStr) } catch (e: Exception) { continue }

                        val exists = UserFavoritesTable.selectAll()
                            .where { (UserFavoritesTable.userId eq userId) and (UserFavoritesTable.foodId eq foodId) }
                            .count() > 0

                        if (!exists) {
                            val foodExists = FoodsTable.selectAll().where { FoodsTable.id eq foodId }.count() > 0
                            if (foodExists) {
                                UserFavoritesTable.insert {
                                    it[id] = UUID.randomUUID()
                                    it[this.userId] = userId
                                    it[this.foodId] = foodId
                                    it[createdAt] = LocalDateTime.now()
                                }
                                syncedCount++
                            }
                        } else {
                            syncedCount++
                        }
                    }
                }

                call.respond(SyncResponse(success = true, syncedCount = syncedCount))
            }
        }

        // GET /favorites
        get {
            call.withAuth { userId ->
                val foods = newSuspendedTransaction(Dispatchers.IO) {
                    (UserFavoritesTable innerJoin FoodsTable)
                        .select(FoodsTable.columns)
                        .where { UserFavoritesTable.userId eq userId }
                        .map { mapFoodRow(it) }
                }
                call.respond(foods)
            }
        }

        // POST /favorites/{foodID}
        post("/{foodID}") {
            call.withAuth { userId ->
                val foodIDString = call.parameters["foodID"]
                if (foodIDString.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Missing food ID"))
                    return@withAuth
                }

                val foodId = try { UUID.fromString(foodIDString) } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Invalid food ID format"))
                    return@withAuth
                }

                val response = newSuspendedTransaction(Dispatchers.IO) {
                    val foodExists = FoodsTable.selectAll().where { FoodsTable.id eq foodId }.count() > 0
                    if (!foodExists) {
                        return@newSuspendedTransaction HttpStatusCode.NotFound to MessageResponse(success = false, message = "Food not found")
                    }

                    val exists = UserFavoritesTable.selectAll()
                        .where { (UserFavoritesTable.userId eq userId) and (UserFavoritesTable.foodId eq foodId) }
                        .count() > 0

                    if (exists) {
                        HttpStatusCode.OK to MessageResponse(success = true, message = "Already in favorites")
                    } else {
                        UserFavoritesTable.insert {
                            it[id] = UUID.randomUUID()
                            it[this.userId] = userId
                            it[this.foodId] = foodId
                            it[createdAt] = LocalDateTime.now()
                        }
                        HttpStatusCode.Created to MessageResponse(success = true, message = "Favorite added")
                    }
                }

                call.respond(response.first, response.second)
            }
        }

        // DELETE /favorites/{foodID}
        delete("/{foodID}") {
            call.withAuth { userId ->
                val foodIDString = call.parameters["foodID"]
                if (foodIDString.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Missing food ID"))
                    return@withAuth
                }

                val foodId = try { UUID.fromString(foodIDString) } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Invalid food ID format"))
                    return@withAuth
                }

                newSuspendedTransaction(Dispatchers.IO) {
                    UserFavoritesTable.deleteWhere { (UserFavoritesTable.userId eq userId) and (UserFavoritesTable.foodId eq foodId) }
                }

                call.respond(HttpStatusCode.OK, MessageResponse(success = true, message = "Favorite removed"))
            }
        }
    }
}
