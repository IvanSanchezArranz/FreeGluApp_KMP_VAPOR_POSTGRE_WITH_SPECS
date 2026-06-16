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
            let invalidId = 999999
            try await app.testing().test(.GET, "foods/\(invalidId)", afterResponse: { res async throws in
                #expect(res.status == .notFound)
            })
        }
    }
}
