package com.glufree.ktor.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object UserFoodOverridesTable : UUIDTable("user_food_overrides", "id") {
    val userId = reference("user_id", UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val foodId = reference("food_id", FoodsTable.id, onDelete = ReferenceOption.CASCADE).nullable()
    val isDeleted = bool("is_deleted")
    val code = varchar("code", 255).nullable()
    val name = varchar("name", 255).nullable()
    val brand = varchar("brand", 255).nullable()
    val categories = text("categories").nullable()
    val ingredients = text("ingredients").nullable()
    val imageUrl = varchar("image_url", 512).nullable()
    val countries = varchar("countries", 255).nullable()
    val glutenFree = bool("gluten_free").nullable()
    val createdAt = datetime("created_at").nullable()
}
