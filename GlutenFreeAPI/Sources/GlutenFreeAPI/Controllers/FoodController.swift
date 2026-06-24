//
//  FoodController.swift
//  GlutenFreeAPI
//
//  Created by Ivan Sanchez Arranz on 12/6/26.
//

import Vapor
import Fluent

struct FoodController: RouteCollection {

    func boot(routes: any RoutesBuilder) throws {

        let foods = routes.grouped("foods")

        // GET /foods
        foods.get { req async throws -> Page<Food> in
            try await Food.query(on: req.db)
                .paginate(for: req)
        }

        // GET /foods/search?q={query}
        foods.get("search") { req async throws -> Page<Food> in
            guard let searchTerm: String = req.query["q"], !searchTerm.isEmpty else {
                throw Abort(.badRequest, reason: "Missing or empty search query parameter 'q'")
            }

            let searchPattern = "%\(searchTerm)%"
            return try await Food.query(on: req.db)
                .group(.or) { orGroup in
                    orGroup.filter(.sql(embed: "name ILIKE \(bind: searchPattern)"))
                    orGroup.filter(.sql(embed: "categories ILIKE \(bind: searchPattern)"))
                    orGroup.filter(.sql(embed: "brand ILIKE \(bind: searchPattern)"))
                }
                .paginate(for: req)
        }

        // GET /foods/:id
        foods.get(":foodID") { req async throws -> Food in

            guard let food = try await Food.find(
                req.parameters.get("foodID"),
                on: req.db
            ) else {
                throw Abort(.notFound)
            }

            return food
        }

        // Authenticated write routes
        let protectedFoods = foods.grouped(UserMiddleware())

        // POST /foods
        protectedFoods.post(use: createFood)

        // PUT /foods/:foodID
        protectedFoods.put(":foodID", use: updateFood)

        // DELETE /foods/:foodID
        protectedFoods.delete(":foodID", use: deleteFood)
    }

    struct FoodInput: Content {
        var code: String
        var name: String
        var brand: String?
        var categories: String?
        var ingredients: String?
        var imageUrl: String?
        var countries: String?
        var glutenFree: Bool
    }

    // POST /foods
    func createFood(req: Request) async throws -> Response {
        let input = try req.content.decode(FoodInput.self)
        req.logger.info("[VAPOR CRUD] Creating new food. Name: '\(input.name)', Code: '\(input.code)', Brand: '\(input.brand ?? "nil")'")

        guard !input.code.isEmpty, !input.name.isEmpty else {
            throw Abort(.badRequest, reason: "Product code and name cannot be empty")
        }

        let food = Food(
            code: input.code,
            name: input.name,
            brand: input.brand,
            categories: input.categories,
            ingredients: input.ingredients,
            imageUrl: input.imageUrl,
            countries: input.countries,
            glutenFree: input.glutenFree
        )

        try await food.save(on: req.db)
        req.logger.info("[VAPOR CRUD] Successfully created food with ID: \(food.id?.uuidString ?? "nil")")
        return try await food.encodeResponse(status: .created, for: req)
    }

    // PUT /foods/:foodID
    func updateFood(req: Request) async throws -> Food {
        guard let foodIDString = req.parameters.get("foodID"), let foodID = UUID(uuidString: foodIDString) else {
            throw Abort(.badRequest, reason: "Invalid product ID")
        }

        guard let food = try await Food.find(foodID, on: req.db) else {
            throw Abort(.notFound, reason: "Product not found")
        }

        let input = try req.content.decode(FoodInput.self)
        req.logger.info("[VAPOR CRUD] Updating food with ID: \(foodID) from name: '\(food.name)' to: '\(input.name)'")

        guard !input.code.isEmpty, !input.name.isEmpty else {
            throw Abort(.badRequest, reason: "Product code and name cannot be empty")
        }

        food.code = input.code
        food.name = input.name
        food.brand = input.brand
        food.categories = input.categories
        food.ingredients = input.ingredients
        food.imageUrl = input.imageUrl
        food.countries = input.countries
        food.glutenFree = input.glutenFree

        try await food.update(on: req.db)
        req.logger.info("[VAPOR CRUD] Successfully updated database entry for \(foodID) to name: '\(food.name)'")
        return food
    }

    // DELETE /foods/:foodID
    func deleteFood(req: Request) async throws -> HTTPStatus {
        guard let foodIDString = req.parameters.get("foodID"), let foodID = UUID(uuidString: foodIDString) else {
            throw Abort(.badRequest, reason: "Invalid product ID")
        }

        guard let food = try await Food.find(foodID, on: req.db) else {
            throw Abort(.notFound, reason: "Product not found")
        }

        try await food.delete(on: req.db)
        return .noContent
    }
}
