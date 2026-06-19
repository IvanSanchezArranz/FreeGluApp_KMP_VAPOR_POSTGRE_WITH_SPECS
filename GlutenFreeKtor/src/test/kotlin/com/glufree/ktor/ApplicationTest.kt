package com.glufree.ktor

import com.glufree.ktor.configure.configureRouting
import com.glufree.ktor.configure.configureSerialization
import com.glufree.ktor.models.FoodsTable
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    private val testId1 = 1
    private val testId2 = 2

    @BeforeTest
    fun setup() {
        // Connect to H2 in-memory DB and create schema
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.drop(FoodsTable)
            SchemaUtils.create(FoodsTable)
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
        println("SEARCH RESPONSE: ${response.bodyAsText()}")
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
        // Fetch row 1 by integer ID
        val response = client.get("/foods/$testId1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Gluten Free Almond Bread"))
        
        // Fetch invalid ID format
        val badUuidResponse = client.get("/foods/invalid-id")
        assertEquals(HttpStatusCode.BadRequest, badUuidResponse.status)

        // Fetch non-existent ID
        val emptyResponse = client.get("/foods/999999")
        assertEquals(HttpStatusCode.NotFound, emptyResponse.status)
    }
}
