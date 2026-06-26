package com.glufree.ktor.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : UUIDTable("users", "id") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = datetime("created_at").nullable()
    val updatedAt = datetime("updated_at").nullable()
}

object UserFavoritesTable : UUIDTable("user_favorites", "id") {
    val userId = reference("user_id", UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val foodId = reference("food_id", FoodsTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").nullable()
}

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse
)

@Serializable
data class SyncFavoritesRequest(
    val foodIds: List<String>
)

@Serializable
data class SyncResponse(
    val success: Boolean,
    val syncedCount: Int
)

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String
)
