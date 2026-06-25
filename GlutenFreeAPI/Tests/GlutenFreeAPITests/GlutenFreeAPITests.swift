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
                try await sql.raw("DROP TABLE IF EXISTS user_food_overrides CASCADE;").run()
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
            try await app.testing().test(.GET, "foods/\(id)", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: token)
            }, afterResponse: { res async throws in
                #expect(res.status == .ok)
                let fetchedFood = try res.content.decode(Food.self)
                #expect(fetchedFood.name == "Updated Name")
                #expect(fetchedFood.brand == "Updated Brand")
            })
        }
    }

    @Test("Test Orphaned JWT Returns Unauthorized")
    func orphanedJwtReturnsUnauthorized() async throws {
        try await withApp { app in
            // 1. Register a user and get token
            let registerBody = AuthController.AuthInput(email: "orphaned@example.com", password: "Password123!")
            var token = ""
            try await app.testing().test(.POST, "register", beforeRequest: { req in
                try req.content.encode(registerBody)
            }, afterResponse: { res async throws in
                #expect(res.status == .created)
                let response = try res.content.decode(AuthController.AuthResponse.self)
                token = response.token
            })

            // 2. Delete the user from the database to orphan the token
            try await User.query(on: app.db).delete()

            // 3. Try accessing favorites (authenticated route)
            try await app.testing().test(.GET, "favorites", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: token)
            }, afterResponse: { res async throws in
                // Should return 401 Unauthorized
                #expect(res.status == .unauthorized)
            })
        }
    }

    @Test("Test User Specific Catalog Isolation")
    func userSpecificCatalogIsolation() async throws {
        try await withApp { app in
            // 1. Setup base food
            let baseFood = Food(code: "1111", name: "Base Food", brand: "Base Brand", categories: "Snack", ingredients: "Sugar", imageUrl: nil, countries: "US", glutenFree: true)
            try await baseFood.save(on: app.db)
            let baseFoodID = try baseFood.requireID()

            // 2. Register User A and User B
            let userABody = AuthController.AuthInput(email: "usera@example.com", password: "Password123!")
            var tokenA = ""
            try await app.testing().test(.POST, "register", beforeRequest: { req in
                try req.content.encode(userABody)
            }, afterResponse: { res async throws in
                #expect(res.status == .created)
                let response = try res.content.decode(AuthController.AuthResponse.self)
                tokenA = response.token
            })

            let userBBody = AuthController.AuthInput(email: "userb@example.com", password: "Password123!")
            var tokenB = ""
            try await app.testing().test(.POST, "register", beforeRequest: { req in
                try req.content.encode(userBBody)
            }, afterResponse: { res async throws in
                #expect(res.status == .created)
                let response = try res.content.decode(AuthController.AuthResponse.self)
                tokenB = response.token
            })

            // 3. User A deletes the base food
            try await app.testing().test(.DELETE, "foods/\(baseFoodID.uuidString)", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: tokenA)
            }, afterResponse: { res async throws in
                #expect(res.status == .noContent)
            })

            // 4. Verify User A does NOT see the base food
            try await app.testing().test(.GET, "foods", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: tokenA)
            }, afterResponse: { res async throws in
                #expect(res.status == .ok)
                let page = try res.content.decode(Page<Food>.self)
                #expect(page.items.isEmpty) // Base food deleted for User A
            })

            // 5. Verify User B STILL sees the base food perfectly
            try await app.testing().test(.GET, "foods", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: tokenB)
            }, afterResponse: { res async throws in
                #expect(res.status == .ok)
                let page = try res.content.decode(Page<Food>.self)
                #expect(page.items.count == 1)
                #expect(page.items[0].id == baseFoodID)
            })

            // 6. User A creates a custom private food
            let customFoodInput = FoodController.FoodInput(
                code: "9999",
                name: "User A Private Food",
                brand: "Private Brand",
                categories: "Snacks",
                ingredients: "Rice",
                imageUrl: nil,
                countries: "US",
                glutenFree: true
            )
            try await app.testing().test(.POST, "foods", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: tokenA)
                try req.content.encode(customFoodInput)
            }, afterResponse: { res async throws in
                #expect(res.status == .created)
            })

            // 7. Verify User A sees their custom private food
            try await app.testing().test(.GET, "foods", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: tokenA)
            }, afterResponse: { res async throws in
                #expect(res.status == .ok)
                let page = try res.content.decode(Page<Food>.self)
                #expect(page.items.count == 1) // Only their custom private food (base is deleted)
                #expect(page.items[0].name == "User A Private Food")
            })

            // 8. Verify User B does NOT see User A's custom private food
            try await app.testing().test(.GET, "foods", beforeRequest: { req in
                req.headers.bearerAuthorization = BearerAuthorization(token: tokenB)
            }, afterResponse: { res async throws in
                #expect(res.status == .ok)
                let page = try res.content.decode(Page<Food>.self)
                #expect(page.items.count == 1) // Only the base food
                #expect(page.items[0].id == baseFoodID)
            })
        }
    }
}
