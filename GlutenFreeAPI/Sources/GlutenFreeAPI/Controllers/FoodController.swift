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
    }
}
