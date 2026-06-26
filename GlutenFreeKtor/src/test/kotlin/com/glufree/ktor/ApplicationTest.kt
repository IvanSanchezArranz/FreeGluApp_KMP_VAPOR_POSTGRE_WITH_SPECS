package com.glufree.ktor

import com.glufree.ktor.configure.configureRouting
import com.glufree.ktor.configure.configureSerialization
import com.glufree.ktor.models.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    private val testId1 = UUID.randomUUID()
    private val testId2 = UUID.randomUUID()

    @BeforeTest
    fun setup() {
        // Connect to H2 in-memory DB and create schema
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.drop(UserFoodOverridesTable, UserFavoritesTable, UsersTable, FoodsTable)
            SchemaUtils.create(FoodsTable, UsersTable, UserFavoritesTable, UserFoodOverridesTable)
            
            FoodsTable.insert {
                it[id] = this@ApplicationTest.testId1
                it[code] = "12345"
                it[name] = "Gluten Free Almond Bread"
                it[brand] = "Glufree Brand"
                it[categories] = "Snacks, Bread"
                it[ingredients] = "Almond flour, water, yeast"
                it[imageUrl] = "http://example.com/almond.jpg"
                it[countries] = "Spain"
                it[glutenFree] = true
                it[createdAt] = LocalDateTime.now()
            }
            FoodsTable.insert {
                it[id] = this@ApplicationTest.testId2
                it[code] = "67890"
                it[name] = "Normal Wheat Pasta"
                it[brand] = "Gluten Brand"
                it[categories] = "Pasta"
                it[ingredients] = "Wheat flour, water"
                it[imageUrl] = "http://example.com/pasta.jpg"
                it[countries] = "Spain"
                it[glutenFree] = false
                it[createdAt] = LocalDateTime.now()
            }
        }
    }

    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("API Gluten Free funcionando 🚀", response.bodyAsText())
    }

    @Test
    fun testGetFoods() = testApplication {
        application {
            configureSerialization()
            configureRouting()
        }
        val response = client.get("/foods?page=1&per=10")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Gluten Free Almond Bread"))
        assertTrue(response.bodyAsText().contains("Normal Wheat Pasta"))
        assertTrue(response.bodyAsText().contains("\"total\": 2"))
    }

    @Test
    fun testSearchFoods() = testApplication {
        application {
            configureSerialization()
            configureRouting()
        }
        // Search term "almond" matches row 1 but not row 2
        val response = client.get("/foods/search?q=almond")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Gluten Free Almond Bread"))
        assertTrue(!response.bodyAsText().contains("Normal Wheat Pasta"))
        assertTrue(response.bodyAsText().contains("\"total\": 1"))
    }

    @Test
    fun testGetFoodById() = testApplication {
        application {
            configureSerialization()
            configureRouting()
        }
        // Fetch row 1 by UUID ID
        val response = client.get("/foods/$testId1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Gluten Free Almond Bread"))
        
        // Fetch invalid ID format
        val badUuidResponse = client.get("/foods/invalid-id")
        assertEquals(HttpStatusCode.BadRequest, badUuidResponse.status)

        // Fetch non-existent ID
        val emptyResponse = client.get("/foods/${UUID.randomUUID()}")
        assertEquals(HttpStatusCode.NotFound, emptyResponse.status)
    }

    @Test
    fun testRegisterAndLogin() = testApplication {
        application {
            configureSerialization()
            configureRouting()
        }
        
        // 1. Register a new user
        val regResponse = client.post("/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"email": "test@example.com", "password": "securepassword"}""")
        }
        assertEquals(HttpStatusCode.Created, regResponse.status)
        
        val regJson = Json.parseToJsonElement(regResponse.bodyAsText()).jsonObject
        assertTrue(regJson.containsKey("token"))
        assertEquals("test@example.com", regJson["user"]!!.jsonObject["email"]!!.jsonPrimitive.content)

        // 2. Login with the registered credentials
        val loginResponse = client.post("/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"email": "test@example.com", "password": "securepassword"}""")
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val loginJson = Json.parseToJsonElement(loginResponse.bodyAsText()).jsonObject
        assertTrue(loginJson.containsKey("token"))
    }

    @Test
    fun testCopyOnWriteCatalogIsolated() = testApplication {
        application {
            configureSerialization()
            configureRouting()
        }

        // 1. Register two users to test isolation
        val reg1 = client.post("/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"email": "user1@example.com", "password": "password123"}""")
        }
        val token1 = Json.parseToJsonElement(reg1.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content

        val reg2 = client.post("/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"email": "user2@example.com", "password": "password123"}""")
        }
        val token2 = Json.parseToJsonElement(reg2.bodyAsText()).jsonObject["token"]!!.jsonPrimitive.content

        // 2. User 1 adds a private custom food
        val customFoodResponse = client.post("/foods") {
            header(HttpHeaders.Authorization, "Bearer $token1")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"code": "custom-1", "name": "Private Tapioca Cake", "brand": "HomeMade", "glutenFree": true}""")
        }
        assertEquals(HttpStatusCode.Created, customFoodResponse.status)
        val customFoodId = Json.parseToJsonElement(customFoodResponse.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        // 3. User 1 should see "Private Tapioca Cake" in their catalog list
        val catalogUser1 = client.get("/foods") {
            header(HttpHeaders.Authorization, "Bearer $token1")
        }
        assertTrue(catalogUser1.bodyAsText().contains("Private Tapioca Cake"))
        assertTrue(catalogUser1.bodyAsText().contains("\"total\": 3")) // 2 base foods + 1 custom food

        // 4. User 2 should NOT see "Private Tapioca Cake" in their catalog list
        val catalogUser2 = client.get("/foods") {
            header(HttpHeaders.Authorization, "Bearer $token2")
        }
        assertTrue(!catalogUser2.bodyAsText().contains("Private Tapioca Cake"))
        assertTrue(catalogUser2.bodyAsText().contains("\"total\": 2")) // Only 2 base foods

        // 5. User 1 edits base food 1 (Almond Bread) to be named "Private Super Bread"
        val editResponse = client.put("/foods/$testId1") {
            header(HttpHeaders.Authorization, "Bearer $token1")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"code": "12345", "name": "Private Super Bread", "brand": "Glufree Brand", "glutenFree": true}""")
        }
        assertEquals(HttpStatusCode.OK, editResponse.status)

        // 6. User 1 fetches the food list and should see the modified name
        val checkEditUser1 = client.get("/foods/$testId1") {
            header(HttpHeaders.Authorization, "Bearer $token1")
        }
        assertTrue(checkEditUser1.bodyAsText().contains("Private Super Bread"))

        // 7. User 2 fetches the same food and should see the original name ("Gluten Free Almond Bread")
        val checkEditUser2 = client.get("/foods/$testId1") {
            header(HttpHeaders.Authorization, "Bearer $token2")
        }
        assertTrue(checkEditUser2.bodyAsText().contains("Gluten Free Almond Bread"))
        assertTrue(!checkEditUser2.bodyAsText().contains("Private Super Bread"))

        // 8. User 1 deletes base food 2 (Wheat Pasta)
        val deleteResponse = client.delete("/foods/$testId2") {
            header(HttpHeaders.Authorization, "Bearer $token1")
        }
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        // 9. User 1 fetches base food 2 and gets NotFound
        val checkDeleteUser1 = client.get("/foods/$testId2") {
            header(HttpHeaders.Authorization, "Bearer $token1")
        }
        assertEquals(HttpStatusCode.NotFound, checkDeleteUser1.status)

        // 10. User 2 fetches base food 2 and gets it successfully (visible)
        val checkDeleteUser2 = client.get("/foods/$testId2") {
            header(HttpHeaders.Authorization, "Bearer $token2")
        }
        assertEquals(HttpStatusCode.OK, checkDeleteUser2.status)
        assertTrue(checkDeleteUser2.bodyAsText().contains("Normal Wheat Pasta"))
    }
}
