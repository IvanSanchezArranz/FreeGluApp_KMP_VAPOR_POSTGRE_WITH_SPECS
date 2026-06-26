package com.glufree.ktor.controllers

import com.glufree.ktor.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

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

            val userId = call.getAuthenticatedUserId()

            val pageResponse = newSuspendedTransaction(Dispatchers.IO) {
                if (userId != null) {
                    // Fetch all overrides for this user
                    val overrides = UserFoodOverridesTable.selectAll()
                        .where { UserFoodOverridesTable.userId eq userId }
                        .toList()

                    val deletedIDs = overrides.filter { it[UserFoodOverridesTable.isDeleted] }
                        .mapNotNull { it[UserFoodOverridesTable.foodId]?.value }
                        .toSet()

                    val editedMap = overrides.filter { !it[UserFoodOverridesTable.isDeleted] && it[UserFoodOverridesTable.foodId] != null }
                        .associateBy { it[UserFoodOverridesTable.foodId]!!.value }

                    val customFoods = overrides.filter { it[UserFoodOverridesTable.foodId] == null }
                        .map { row ->
                            FoodResponse(
                                id = row[UserFoodOverridesTable.id].value.toString(),
                                code = row[UserFoodOverridesTable.code] ?: "",
                                name = row[UserFoodOverridesTable.name] ?: "",
                                brand = row[UserFoodOverridesTable.brand],
                                categories = row[UserFoodOverridesTable.categories],
                                ingredients = row[UserFoodOverridesTable.ingredients],
                                imageUrl = row[UserFoodOverridesTable.imageUrl],
                                countries = row[UserFoodOverridesTable.countries],
                                glutenFree = row[UserFoodOverridesTable.glutenFree] ?: true,
                                createdAt = row[UserFoodOverridesTable.createdAt]?.toString()
                            )
                        }

                    // Query base foods excluding deleted ones
                    var query = FoodsTable.selectAll()
                    if (deletedIDs.isNotEmpty()) {
                        query = query.where { FoodsTable.id notInList deletedIDs }
                    }

                    val totalCount = query.count()
                    val baseItems = query.limit(limit, offset = offset).map { mapFoodRow(it) }

                    // Apply overrides
                    val updatedItems = baseItems.map { food ->
                        val foodUuid = try { UUID.fromString(food.id) } catch (e: Exception) { null }
                        val override = if (foodUuid != null) editedMap[foodUuid] else null
                        if (override != null) {
                            food.copy(
                                code = override[UserFoodOverridesTable.code] ?: food.code,
                                name = override[UserFoodOverridesTable.name] ?: food.name,
                                brand = override[UserFoodOverridesTable.brand] ?: food.brand,
                                categories = override[UserFoodOverridesTable.categories] ?: food.categories,
                                ingredients = override[UserFoodOverridesTable.ingredients] ?: food.ingredients,
                                imageUrl = override[UserFoodOverridesTable.imageUrl] ?: food.imageUrl,
                                countries = override[UserFoodOverridesTable.countries] ?: food.countries,
                                glutenFree = override[UserFoodOverridesTable.glutenFree] ?: food.glutenFree
                            )
                        } else {
                            food
                        }
                    }

                    var finalItems = updatedItems
                    if (page == 1) {
                        finalItems = customFoods + updatedItems
                    }

                    PageResponse(
                        items = finalItems,
                        metadata = PaginationMetadata(
                            page = page,
                            per = per,
                            total = totalCount + customFoods.size
                        )
                    )
                } else {
                    val totalCount = FoodsTable.selectAll().count()
                    val items = FoodsTable.selectAll().limit(limit, offset = offset).map { mapFoodRow(it) }
                    PageResponse(
                        items = items,
                        metadata = PaginationMetadata(
                            page = page,
                            per = per,
                            total = totalCount
                        )
                    )
                }
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

            val userId = call.getAuthenticatedUserId()

            val pageResponse = newSuspendedTransaction(Dispatchers.IO) {
                val searchExpression = (FoodsTable.name.lowerCase() like searchPattern.lowercase()) or
                        (FoodsTable.categories.lowerCase() like searchPattern.lowercase()) or
                        (FoodsTable.brand.lowerCase() like searchPattern.lowercase())

                if (userId != null) {
                    val overrides = UserFoodOverridesTable.selectAll()
                        .where { UserFoodOverridesTable.userId eq userId }
                        .toList()

                    val deletedIDs = overrides.filter { it[UserFoodOverridesTable.isDeleted] }
                        .mapNotNull { it[UserFoodOverridesTable.foodId]?.value }
                        .toSet()

                    val editedMap = overrides.filter { !it[UserFoodOverridesTable.isDeleted] && it[UserFoodOverridesTable.foodId] != null }
                        .associateBy { it[UserFoodOverridesTable.foodId]!!.value }

                    val customFoods = overrides.filter {
                        it[UserFoodOverridesTable.foodId] == null && (
                                (it[UserFoodOverridesTable.name]?.lowercase()?.contains(searchTerm.lowercase()) == true) ||
                                        (it[UserFoodOverridesTable.categories]?.lowercase()?.contains(searchTerm.lowercase()) == true) ||
                                        (it[UserFoodOverridesTable.brand]?.lowercase()?.contains(searchTerm.lowercase()) == true)
                                )
                    }.map { row ->
                        FoodResponse(
                            id = row[UserFoodOverridesTable.id].value.toString(),
                            code = row[UserFoodOverridesTable.code] ?: "",
                            name = row[UserFoodOverridesTable.name] ?: "",
                            brand = row[UserFoodOverridesTable.brand],
                            categories = row[UserFoodOverridesTable.categories],
                            ingredients = row[UserFoodOverridesTable.ingredients],
                            imageUrl = row[UserFoodOverridesTable.imageUrl],
                            countries = row[UserFoodOverridesTable.countries],
                            glutenFree = row[UserFoodOverridesTable.glutenFree] ?: true,
                            createdAt = row[UserFoodOverridesTable.createdAt]?.toString()
                        )
                    }

                    var query = FoodsTable.selectAll().where { searchExpression }
                    if (deletedIDs.isNotEmpty()) {
                        query = query.andWhere { FoodsTable.id notInList deletedIDs }
                    }

                    val totalCount = query.count()
                    val baseItems = query.limit(limit, offset = offset).map { mapFoodRow(it) }

                    val updatedItems = baseItems.map { food ->
                        val foodUuid = try { UUID.fromString(food.id) } catch (e: Exception) { null }
                        val override = if (foodUuid != null) editedMap[foodUuid] else null
                        if (override != null) {
                            food.copy(
                                code = override[UserFoodOverridesTable.code] ?: food.code,
                                name = override[UserFoodOverridesTable.name] ?: food.name,
                                brand = override[UserFoodOverridesTable.brand] ?: food.brand,
                                categories = override[UserFoodOverridesTable.categories] ?: food.categories,
                                ingredients = override[UserFoodOverridesTable.ingredients] ?: food.ingredients,
                                imageUrl = override[UserFoodOverridesTable.imageUrl] ?: food.imageUrl,
                                countries = override[UserFoodOverridesTable.countries] ?: food.countries,
                                glutenFree = override[UserFoodOverridesTable.glutenFree] ?: food.glutenFree
                            )
                        } else {
                            food
                        }
                    }

                    var finalItems = updatedItems
                    if (page == 1) {
                        finalItems = customFoods + updatedItems
                    }

                    PageResponse(
                        items = finalItems,
                        metadata = PaginationMetadata(
                            page = page,
                            per = per,
                            total = totalCount + customFoods.size
                        )
                    )
                } else {
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
            }

            call.respond(pageResponse)
        }

        // GET /foods/{foodID}
        get("/{foodID}") {
            val foodIDString = call.parameters["foodID"]
            if (foodIDString.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Missing or empty foodID"))
                return@get
            }

            val foodId = try { UUID.fromString(foodIDString) } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Invalid ID format (expected UUID)"))
                return@get
            }

            val userId = call.getAuthenticatedUserId()

            val response = newSuspendedTransaction(Dispatchers.IO) {
                if (userId != null) {
                    // Check custom private food override with food_id = null and id = foodID
                    val customOverride = UserFoodOverridesTable.selectAll()
                        .where { (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.id eq foodId) and (UserFoodOverridesTable.foodId.isNull()) }
                        .firstOrNull()

                    if (customOverride != null) {
                        return@newSuspendedTransaction HttpStatusCode.OK to FoodResponse(
                            id = customOverride[UserFoodOverridesTable.id].value.toString(),
                            code = customOverride[UserFoodOverridesTable.code] ?: "",
                            name = customOverride[UserFoodOverridesTable.name] ?: "",
                            brand = customOverride[UserFoodOverridesTable.brand],
                            categories = customOverride[UserFoodOverridesTable.categories],
                            ingredients = customOverride[UserFoodOverridesTable.ingredients],
                            imageUrl = customOverride[UserFoodOverridesTable.imageUrl],
                            countries = customOverride[UserFoodOverridesTable.countries],
                            glutenFree = customOverride[UserFoodOverridesTable.glutenFree] ?: true,
                            createdAt = customOverride[UserFoodOverridesTable.createdAt]?.toString()
                        )
                    }

                    // Check if base food is deleted for this user
                    val isDeleted = UserFoodOverridesTable.selectAll()
                        .where { (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.foodId eq foodId) and (UserFoodOverridesTable.isDeleted eq true) }
                        .count() > 0

                    if (isDeleted) {
                        return@newSuspendedTransaction HttpStatusCode.NotFound to ErrorResponse(error = true, reason = "Food not found")
                    }

                    // Check edit override
                    val override = UserFoodOverridesTable.selectAll()
                        .where { (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.foodId eq foodId) }
                        .firstOrNull()

                    val baseFoodRow = FoodsTable.selectAll().where { FoodsTable.id eq foodId }.firstOrNull()
                    if (baseFoodRow == null) {
                        return@newSuspendedTransaction HttpStatusCode.NotFound to ErrorResponse(error = true, reason = "Food not found")
                    }

                    val food = mapFoodRow(baseFoodRow)
                    val finalFood = if (override != null) {
                        food.copy(
                            code = override[UserFoodOverridesTable.code] ?: food.code,
                            name = override[UserFoodOverridesTable.name] ?: food.name,
                            brand = override[UserFoodOverridesTable.brand] ?: food.brand,
                            categories = override[UserFoodOverridesTable.categories] ?: food.categories,
                            ingredients = override[UserFoodOverridesTable.ingredients] ?: food.ingredients,
                            imageUrl = override[UserFoodOverridesTable.imageUrl] ?: food.imageUrl,
                            countries = override[UserFoodOverridesTable.countries] ?: food.countries,
                            glutenFree = override[UserFoodOverridesTable.glutenFree] ?: food.glutenFree
                        )
                    } else {
                        food
                    }

                    HttpStatusCode.OK to finalFood
                } else {
                    val baseFoodRow = FoodsTable.selectAll().where { FoodsTable.id eq foodId }.firstOrNull()
                    if (baseFoodRow != null) {
                        HttpStatusCode.OK to mapFoodRow(baseFoodRow)
                    } else {
                        HttpStatusCode.NotFound to ErrorResponse(error = true, reason = "Food not found")
                    }
                }
            }

            if (response.first == HttpStatusCode.OK) {
                call.respond(response.first, response.second as FoodResponse)
            } else {
                call.respond(response.first, response.second as ErrorResponse)
            }
        }

        // POST /foods
        post {
            call.withAuth { userId ->
                val input = call.receive<FoodInput>()
                if (input.code.isBlank() || input.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Product code and name cannot be empty"))
                    return@withAuth
                }

                val newOverrideId = UUID.randomUUID()
                newSuspendedTransaction(Dispatchers.IO) {
                    UserFoodOverridesTable.insert {
                        it[id] = newOverrideId
                        it[UserFoodOverridesTable.userId] = userId
                        it[foodId] = null
                        it[isDeleted] = false
                        it[code] = input.code
                        it[name] = input.name
                        it[brand] = input.brand
                        it[categories] = input.categories
                        it[ingredients] = input.ingredients
                        it[imageUrl] = input.imageUrl
                        it[countries] = input.countries
                        it[glutenFree] = input.glutenFree
                        it[createdAt] = LocalDateTime.now()
                    }
                }

                val food = FoodResponse(
                    id = newOverrideId.toString(),
                    code = input.code,
                    name = input.name,
                    brand = input.brand,
                    categories = input.categories,
                    ingredients = input.ingredients,
                    imageUrl = input.imageUrl,
                    countries = input.countries,
                    glutenFree = input.glutenFree
                )

                call.respond(HttpStatusCode.Created, food)
            }
        }

        // PUT /foods/{foodID}
        put("/{foodID}") {
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

                val input = call.receive<FoodInput>()
                if (input.code.isBlank() || input.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = true, reason = "Product code and name cannot be empty"))
                    return@withAuth
                }

                val updatedFood = newSuspendedTransaction(Dispatchers.IO) {
                    // Check if custom private food
                    val isCustom = UserFoodOverridesTable.selectAll()
                        .where { (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.id eq foodId) and (UserFoodOverridesTable.foodId.isNull()) }
                        .count() > 0

                    if (isCustom) {
                        UserFoodOverridesTable.update({ (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.id eq foodId) and (UserFoodOverridesTable.foodId.isNull()) }) {
                            it[code] = input.code
                            it[name] = input.name
                            it[brand] = input.brand
                            it[categories] = input.categories
                            it[ingredients] = input.ingredients
                            it[imageUrl] = input.imageUrl
                            it[countries] = input.countries
                            it[glutenFree] = input.glutenFree
                        }
                        FoodResponse(
                            id = foodId.toString(),
                            code = input.code,
                            name = input.name,
                            brand = input.brand,
                            categories = input.categories,
                            ingredients = input.ingredients,
                            imageUrl = input.imageUrl,
                            countries = input.countries,
                            glutenFree = input.glutenFree
                        )
                    } else {
                        val baseFoodExists = FoodsTable.selectAll().where { FoodsTable.id eq foodId }.count() > 0
                        if (!baseFoodExists) {
                            return@newSuspendedTransaction null
                        }

                        val existingOverride = UserFoodOverridesTable.selectAll()
                            .where { (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.foodId eq foodId) }
                            .firstOrNull()

                        if (existingOverride != null) {
                            UserFoodOverridesTable.update({ (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.foodId eq foodId) }) {
                                it[code] = input.code
                                it[name] = input.name
                                it[brand] = input.brand
                                it[categories] = input.categories
                                it[ingredients] = input.ingredients
                                it[imageUrl] = input.imageUrl
                                it[countries] = input.countries
                                it[glutenFree] = input.glutenFree
                                it[isDeleted] = false
                            }
                        } else {
                            UserFoodOverridesTable.insert {
                                it[id] = UUID.randomUUID()
                                it[UserFoodOverridesTable.userId] = userId
                                it[this.foodId] = foodId
                                it[isDeleted] = false
                                it[code] = input.code
                                it[name] = input.name
                                it[brand] = input.brand
                                it[categories] = input.categories
                                it[ingredients] = input.ingredients
                                it[imageUrl] = input.imageUrl
                                it[countries] = input.countries
                                it[glutenFree] = input.glutenFree
                                it[createdAt] = LocalDateTime.now()
                            }
                        }

                        FoodResponse(
                            id = foodId.toString(),
                            code = input.code,
                            name = input.name,
                            brand = input.brand,
                            categories = input.categories,
                            ingredients = input.ingredients,
                            imageUrl = input.imageUrl,
                            countries = input.countries,
                            glutenFree = input.glutenFree
                        )
                    }
                }

                if (updatedFood != null) {
                    call.respond(HttpStatusCode.OK, updatedFood)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = true, reason = "Food not found"))
                }
            }
        }

        // DELETE /foods/{foodID}
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

                val success = newSuspendedTransaction(Dispatchers.IO) {
                    // Check custom private
                    val isCustom = UserFoodOverridesTable.selectAll()
                        .where { (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.id eq foodId) and (UserFoodOverridesTable.foodId.isNull()) }
                        .count() > 0

                    if (isCustom) {
                        UserFoodOverridesTable.deleteWhere { (UserFoodOverridesTable.userId eq userId) and (id eq foodId) and (UserFoodOverridesTable.foodId.isNull()) }
                        true
                    } else {
                        val baseFoodExists = FoodsTable.selectAll().where { FoodsTable.id eq foodId }.count() > 0
                        if (!baseFoodExists) {
                            return@newSuspendedTransaction false
                        }

                        val existingOverride = UserFoodOverridesTable.selectAll()
                            .where { (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.foodId eq foodId) }
                            .firstOrNull()

                        if (existingOverride != null) {
                            UserFoodOverridesTable.update({ (UserFoodOverridesTable.userId eq userId) and (UserFoodOverridesTable.foodId eq foodId) }) {
                                it[isDeleted] = true
                            }
                        } else {
                            UserFoodOverridesTable.insert {
                                it[id] = UUID.randomUUID()
                                it[UserFoodOverridesTable.userId] = userId
                                it[this.foodId] = foodId
                                it[isDeleted] = true
                                it[createdAt] = LocalDateTime.now()
                            }
                        }
                        true
                    }
                }

                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = true, reason = "Food not found"))
                }
            }
        }
    }
}
