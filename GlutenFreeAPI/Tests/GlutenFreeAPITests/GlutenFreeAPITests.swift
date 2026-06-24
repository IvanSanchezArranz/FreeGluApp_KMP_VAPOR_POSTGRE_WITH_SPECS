@testable import GlutenFreeAPI
import VaporTesting
import Testing
import Fluent
import Vapor
import SQLKit

@Suite("App Tests with DB", .serialized)
struct GlutenFreeAPITests {
    private func withApp(_ test: (Application) async throws -> ()) async throws {
        let app = try await Application.make(.testing)
        do {
            try await configure(app)
            
            if let sql = app.db as? any SQLDatabase {
                try await sql.raw("DROP TABLE IF EXISTS user_favorites CASCADE;").run()
                try await sql.raw("DROP TABLE IF EXISTS users CASCADE;").run()
                try await sql.raw("DROP TABLE IF EXISTS foods CASCADE;").run()
                try await sql.raw("DROP TABLE IF EXISTS _fluent_migrations CASCADE;").run()
            }
            
            try await app.autoMigrate()
            try await Food.query(on: app.db).delete()
            try await test(app)
        } catch {
            try await app.asyncShutdown()
            throw error
        }
        try await app.asyncShutdown()
    }
    
    @Test("Test Register User Success")
    func registerUserSuccess() async throws {
        try await withApp { app in
            let body = AuthController.AuthInput(email: "register@example.com", password: "Password123!")
            try await app.testing().test(.POST, "register", beforeRequest: { req in
                try req.content.encode(body)
            }, afterResponse: { res async throws in
                #expect(res.status == .created)
                let response = try res.content.decode(AuthController.AuthResponse.self)
                #expect(response.user.email == "register@example.com")
                #expect(!response.token.isEmpty)
            })
        }
    }

    @Test("Test Register Duplicate User Conflict")
    func registerUserConflict() async throws {
        try await withApp { app in
            let body = AuthController.AuthInput(email: "register@example.com", password: "Password123!")
            // Create user first
            try await app.testing().test(.POST, "register", beforeRequest: { req in
                try req.content.encode(body)
            }, afterResponse: { res async in
                #expect(res.status == .created)
            })

            // Try registering again
            try await app.testing().test(.POST, "register", beforeRequest: { req in
                try req.content.encode(body)
            }, afterResponse: { res async in
                #expect(res.status == .conflict)
            })
        }
    }

    @Test("Test Login User Success")
    func loginUserSuccess() async throws {
        try await withApp { app in
            let body = AuthController.AuthInput(email: "login@example.com", password: "Password123!")
            // Register
            try await app.testing().test(.POST, "register", beforeRequest: { req in
                try req.content.encode(body)
            }, afterResponse: { res async in
                #expect(res.status == .created)
            })

            // Login
            try await app.testing().test(.POST, "login", beforeRequest: { req in
                try req.content.encode(body)
            }, afterResponse: { res async throws in
                #expect(res.status == .ok)
                let response = try res.content.decode(AuthController.AuthResponse.self)
                #expect(response.user.email == "login@example.com")
                #expect(!response.token.isEmpty)
            })
        }
    }

    @Test("Test Login User Invalid Credentials")
    func loginUserInvalidCredentials() async throws {
        try await withApp { app in
            let body = AuthController.AuthInput(email: "login@example.com", password: "WrongPassword")
            try await app.testing().test(.POST, "login", beforeRequest: { req in
                try req.content.encode(body)
            }, afterResponse: { res async in
                #expect(res.status == .unauthorized)
            })
        }
    }
    
    @Test("Test Hello World Route")
    func helloWorld() async throws {
        try await withApp { app in
            try await app.testing().test(.GET, "", afterResponse: { res async in
                #expect(res.status == .ok)
                #expect(res.body.string == "API Gluten Free funcionando 🚀")
            })
        }
    }
    
    @Test("Getting all the Foods")
    func getAllFoods() async throws {
        try await withApp { app in
            let sampleFoods = [
                Food(code: "001", name: "Gluten Free Bread", brand: "Schär", categories: "Bread", ingredients: "Rice flour", imageUrl: nil, countries: "Spain", glutenFree: true),
                Food(code: "002", name: "Gluten Free Pasta", brand: "Barilla", categories: "Pasta", ingredients: "Corn flour", imageUrl: nil, countries: "Italy", glutenFree: true)
            ]
            try await sampleFoods.create(on: app.db)
            
            try await app.testing().test(.GET, "foods", afterResponse: { res async throws in
                #expect(res.status == .ok)
                let page = try res.content.decode(Page<Food>.self)
                #expect(page.items.count == 2)
                let sortedItems = page.items.sorted { $0.name < $1.name }
                #expect(sortedItems[0].name == "Gluten Free Bread")
                #expect(sortedItems[1].name == "Gluten Free Pasta")
            })
        }
    }
    
    @Test("Search Foods by name")
    func searchFoods() async throws {
        try await withApp { app in
            let sampleFoods = [
                Food(code: "001", name: "Apple", brand: "Nature", categories: "Fruits", ingredients: "Apple", imageUrl: nil, countries: "Spain", glutenFree: true),
                Food(code: "002", name: "Gluten Free Cookies", brand: "Gullon", categories: "Snacks", ingredients: "Corn flour, Sugar", imageUrl: nil, countries: "Spain", glutenFree: true),
                Food(code: "003", name: "Chocolate Cookies", brand: "Milka", categories: "Snacks", ingredients: "Wheat flour", imageUrl: nil, countries: "Germany", glutenFree: false)
            ]
            try await sampleFoods.create(on: app.db)
            
            try await app.testing().test(.GET, "foods/search?q=cookies", afterResponse: { res async throws in
                #expect(res.status == .ok)
                let page = try res.content.decode(Page<Food>.self)
                #expect(page.items.count == 2)
                let names = page.items.map { $0.name }
                #expect(names.contains("Gluten Free Cookies"))
                #expect(names.contains("Chocolate Cookies"))
            })
        }
    }
    
    @Test("Get Food by ID")
    func getFoodById() async throws {
        try await withApp { app in
            let food = Food(code: "001", name: "Gluten Free Bread", brand: "Schär", categories: "Bread", ingredients: "Rice flour", imageUrl: nil, countries: "Spain", glutenFree: true)
            try await food.create(on: app.db)
            let id = try food.requireID()
            
            try await app.testing().test(.GET, "foods/\(id)", afterResponse: { res async throws in
                #expect(res.status == .ok)
                let returnedFood = try res.content.decode(Food.self)
                #expect(returnedFood.id == id)
                #expect(returnedFood.name == "Gluten Free Bread")
            })
        }
    }
    
    @Test("Get Food by Invalid ID returns 404")
    func getFoodByInvalidId() async throws {
        try await withApp { app in
            let invalidId = UUID()
            try await app.testing().test(.GET, "foods/\(invalidId)", afterResponse: { res async throws in
                #expect(res.status == .notFound)
            })
        }
    }

    @Test("Test Update Food Success")
    func updateFoodSuccess() async throws {
        try await withApp { app in
            // 1. Create a food item in the DB
            let food = Food(code: "001", name: "Original Name", brand: "Original Brand", categories: "Bread", ingredients: "Flour", imageUrl: nil, countries: "Spain", glutenFree: true)
            try await food.create(on: app.db)
            let id = try food.requireID()

            // 2. Register and Login to get a valid JWT token
            let registerBody = AuthController.AuthInput(email: "update@example.com", password: "Password123!")
            var token = ""
            try await app.testing().test(.POST, "register", beforeRequest: { req in
                try req.content.encode(registerBody)
            }, afterResponse: { res async throws in
                #expect(res.status == .created)
                let response = try res.content.decode(AuthController.AuthResponse.self)
                token = response.token
            })

            // 3. Send PUT request to update the food item
            let updateBody = FoodController.FoodInput(
                code: "001-updated",
                name: "Updated Name",
                brand: "Updated Brand",
                categories: "Bread, Snacks",
                ingredients: "Rice Flour",
                imageUrl: "http://updated.com/img.png",
                countries: "Spain, Italy",
                glutenFree: true
            )

            try await app.testing().test(.PUT, "foods/\(id)", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: token)
                try req.content.encode(updateBody)
            }, afterResponse: { res async throws in
                #expect(res.status == .ok)
                let returnedFood = try res.content.decode(Food.self)
                #expect(returnedFood.id == id)
                #expect(returnedFood.name == "Updated Name")
                #expect(returnedFood.brand == "Updated Brand")
            })

            // 4. Fetch details to confirm database persistence
            try await app.testing().test(.GET, "foods/\(id)", afterResponse: { res async throws in
                #expect(res.status == .ok)
                let fetchedFood = try res.content.decode(Food.self)
                #expect(fetchedFood.name == "Updated Name")
                #expect(fetchedFood.brand == "Updated Brand")
            })
        }
    }
}
