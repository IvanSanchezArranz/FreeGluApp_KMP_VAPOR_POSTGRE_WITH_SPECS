package com.glufree.ktor.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.format.DateTimeFormatter

object FoodsTable : UUIDTable("foods", "id") {
    val code = varchar("code", 255)
    val name = varchar("name", 255)
    val brand = varchar("brand", 255).nullable()
    val categories = text("categories").nullable()
    val ingredients = text("ingredients").nullable()
    val imageUrl = varchar("image_url", 512).nullable()
    val countries = varchar("countries", 255).nullable()
    val glutenFree = bool("gluten_free")
    val createdAt = datetime("created_at").nullable()
}

@Serializable
data class FoodResponse(
    val id: String,
    val code: String,
    val name: String,
    val brand: String? = null,
    val categories: String? = null,
    val ingredients: String? = null,
    val imageUrl: String? = null,
    val countries: String? = null,
    val glutenFree: Boolean,
    val createdAt: String? = null
)

@Serializable
data class FoodInput(
    val code: String,
    val name: String,
    val brand: String? = null,
    val categories: String? = null,
    val ingredients: String? = null,
    val imageUrl: String? = null,
    val countries: String? = null,
    val glutenFree: Boolean
)

private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

fun mapFoodRow(row: org.jetbrains.exposed.sql.ResultRow): FoodResponse {
    return FoodResponse(
        id = row[FoodsTable.id].value.toString(),
        code = row[FoodsTable.code],
        name = row[FoodsTable.name],
        brand = row[FoodsTable.brand],
        categories = row[FoodsTable.categories],
        ingredients = row[FoodsTable.ingredients],
        imageUrl = row[FoodsTable.imageUrl],
        countries = row[FoodsTable.countries],
        glutenFree = row[FoodsTable.glutenFree],
        createdAt = row[FoodsTable.createdAt]?.format(formatter)
    )
}
