import Vapor
import Fluent

struct FoodController: RouteCollection {

    func boot(routes: any RoutesBuilder) throws {

        let foods = routes.grouped("foods")

        // GET /foods
        foods.get { req async throws -> Page<Food> in
            let baseQuery = Food.query(on: req.db)
            if let userID = getAuthenticatedUserID(req: req) {
                return try await applyUserSpecificCatalog(req: req, baseQuery: baseQuery, userID: userID)
            }
            return try await baseQuery.paginate(for: req)
        }

        // GET /foods/search?q={query}
        foods.get("search") { req async throws -> Page<Food> in
            guard let searchTerm: String = req.query["q"], !searchTerm.isEmpty else {
                throw Abort(.badRequest, reason: "Missing or empty search query parameter 'q'")
            }

            let searchPattern = "%\(searchTerm)%"
            let baseQuery = Food.query(on: req.db)
                .group(.or) { orGroup in
                    orGroup.filter(.sql(embed: "name ILIKE \(bind: searchPattern)"))
                    orGroup.filter(.sql(embed: "categories ILIKE \(bind: searchPattern)"))
                    orGroup.filter(.sql(embed: "brand ILIKE \(bind: searchPattern)"))
                }

            if let userID = getAuthenticatedUserID(req: req) {
                return try await applyUserSpecificCatalog(req: req, baseQuery: baseQuery, userID: userID)
            }
            return try await baseQuery.paginate(for: req)
        }

        // GET /foods/:id
        foods.get(":foodID") { req async throws -> Food in
            guard let foodIDString = req.parameters.get("foodID"), let foodID = UUID(uuidString: foodIDString) else {
                throw Abort(.badRequest, reason: "Invalid product ID")
            }

            // If user is authenticated, check if this is a custom food or if there is an override
            if let userID = getAuthenticatedUserID(req: req) {
                // Check if it's a custom food first
                if let customOverride = try await UserFoodOverride.query(on: req.db)
                    .filter(\.$user.$id == userID)
                    .filter(\.$id == foodID)
                    .filter(\.$food.$id == nil)
                    .first() {
                    return Food(
                        id: customOverride.id,
                        code: customOverride.code ?? "",
                        name: customOverride.name ?? "",
                        brand: customOverride.brand,
                        categories: customOverride.categories,
                        ingredients: customOverride.ingredients,
                        imageUrl: customOverride.imageUrl,
                        countries: customOverride.countries,
                        glutenFree: customOverride.glutenFree ?? true
                    )
                }

                // Check if it's deleted
                let isDeleted = try await UserFoodOverride.query(on: req.db)
                    .filter(\.$user.$id == userID)
                    .filter(\.$food.$id == foodID)
                    .filter(\.$isDeleted == true)
                    .first() != nil
                
                if isDeleted {
                    throw Abort(.notFound)
                }

                // Check if there is an edit override
                if let override = try await UserFoodOverride.query(on: req.db)
                    .filter(\.$user.$id == userID)
                    .filter(\.$food.$id == foodID)
                    .first() {
                    
                    guard let food = try await Food.find(foodID, on: req.db) else {
                        throw Abort(.notFound)
                    }

                    if let code = override.code { food.code = code }
                    if let name = override.name { food.name = name }
                    if let brand = override.brand { food.brand = brand }
                    if let categories = override.categories { food.categories = categories }
                    if let ingredients = override.ingredients { food.ingredients = ingredients }
                    if let imageUrl = override.imageUrl { food.imageUrl = imageUrl }
                    if let countries = override.countries { food.countries = countries }
                    if let glutenFree = override.glutenFree { food.glutenFree = glutenFree }

                    return food
                }
            }

            // Default base food fetch
            guard let food = try await Food.find(foodID, on: req.db) else {
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

    // Helper to get authenticated user ID if bearer token is present
    func getAuthenticatedUserID(req: Request) -> UUID? {
        guard let token = req.headers.bearerAuthorization?.token else {
            return nil
        }
        do {
            let payload = try req.jwt.verify(token, as: AuthController.UserPayload.self)
            return UUID(uuidString: payload.subject.value)
        } catch {
            return nil
        }
    }

    // Helper to apply user overrides and append custom foods
    func applyUserSpecificCatalog(
        req: Request,
        baseQuery: QueryBuilder<Food>,
        userID: UUID
    ) async throws -> Page<Food> {
        // 1. Fetch all overrides for this user
        let overrides = try await UserFoodOverride.query(on: req.db)
            .filter(\.$user.$id == userID)
            .all()

        let deletedIDs = Set(overrides.filter { $0.isDeleted }.compactMap { $0.$food.id })
        let editedMap = Dictionary(uniqueKeysWithValues: overrides.filter { !$0.isDeleted && $0.$food.id != nil }.map { ($0.$food.id!, $0) })
        
        let customFoods = overrides.filter { $0.$food.id == nil }.map { override in
            Food(
                id: override.id,
                code: override.code ?? "",
                name: override.name ?? "",
                brand: override.brand,
                categories: override.categories,
                ingredients: override.ingredients,
                imageUrl: override.imageUrl,
                countries: override.countries,
                glutenFree: override.glutenFree ?? true
            )
        }

        // 2. Query all matching base foods excluding deleted ones
        var filteredQuery = baseQuery
        if !deletedIDs.isEmpty {
            filteredQuery = filteredQuery.filter(\.$id !~ deletedIDs)
        }

        // 3. Paginate base foods
        let basePage = try await filteredQuery.paginate(for: req)

        // 4. Apply overrides to base foods in current page
        for food in basePage.items {
            if let foodID = food.id, let override = editedMap[foodID] {
                if let code = override.code { food.code = code }
                if let name = override.name { food.name = name }
                if let brand = override.brand { food.brand = brand }
                if let categories = override.categories { food.categories = categories }
                if let ingredients = override.ingredients { food.ingredients = ingredients }
                if let imageUrl = override.imageUrl { food.imageUrl = imageUrl }
                if let countries = override.countries { food.countries = countries }
                if let glutenFree = override.glutenFree { food.glutenFree = glutenFree }
            }
        }

        // 5. If we are on the first page, prepend the user's custom foods
        var mergedItems = basePage.items
        if basePage.metadata.page == 1 {
            mergedItems.insert(contentsOf: customFoods, at: 0)
        }

        return Page(
            items: mergedItems,
            metadata: PageMetadata(
                page: basePage.metadata.page,
                per: basePage.metadata.per,
                total: basePage.metadata.total + customFoods.count
            )
        )
    }

    // POST /foods
    func createFood(req: Request) async throws -> Response {
        let payload = try req.auth.require(AuthController.UserPayload.self)
        guard let userID = UUID(uuidString: payload.subject.value) else {
            throw Abort(.unauthorized)
        }

        let input = try req.content.decode(FoodInput.self)
        req.logger.info("[VAPOR CRUD] Creating new food for user \(userID). Name: '\(input.name)', Code: '\(input.code)'")

        guard !input.code.isEmpty, !input.name.isEmpty else {
            throw Abort(.badRequest, reason: "Product code and name cannot be empty")
        }

        // Create a private custom food override with food_id = nil
        let override = UserFoodOverride(
            userID: userID,
            foodID: nil,
            isDeleted: false,
            code: input.code,
            name: input.name,
            brand: input.brand,
            categories: input.categories,
            ingredients: input.ingredients,
            imageUrl: input.imageUrl,
            countries: input.countries,
            glutenFree: input.glutenFree
        )

        try await override.save(on: req.db)
        
        // Map to a Food object to return in response
        let food = Food(
            id: override.id,
            code: input.code,
            name: input.name,
            brand: input.brand,
            categories: input.categories,
            ingredients: input.ingredients,
            imageUrl: input.imageUrl,
            countries: input.countries,
            glutenFree: input.glutenFree
        )
        
        req.logger.info("[VAPOR CRUD] Successfully created private custom food with ID: \(override.id?.uuidString ?? "nil")")
        return try await food.encodeResponse(status: .created, for: req)
    }

    // PUT /foods/:foodID
    func updateFood(req: Request) async throws -> Food {
        let payload = try req.auth.require(AuthController.UserPayload.self)
        guard let userID = UUID(uuidString: payload.subject.value) else {
            throw Abort(.unauthorized)
        }

        guard let foodIDString = req.parameters.get("foodID"), let foodID = UUID(uuidString: foodIDString) else {
            throw Abort(.badRequest, reason: "Invalid product ID")
        }

        let input = try req.content.decode(FoodInput.self)
        guard !input.code.isEmpty, !input.name.isEmpty else {
            throw Abort(.badRequest, reason: "Product code and name cannot be empty")
        }

        req.logger.info("[VAPOR CRUD] User \(userID) requested update for food ID: \(foodID) to name: '\(input.name)'")

        // Check if this is a custom private food (food_id is nil, id matches foodID)
        if let existingOverride = try await UserFoodOverride.query(on: req.db)
            .filter(\.$user.$id == userID)
            .filter(\.$id == foodID)
            .filter(\.$food.$id == nil)
            .first() {
            
            existingOverride.code = input.code
            existingOverride.name = input.name
            existingOverride.brand = input.brand
            existingOverride.categories = input.categories
            existingOverride.ingredients = input.ingredients
            existingOverride.imageUrl = input.imageUrl
            existingOverride.countries = input.countries
            existingOverride.glutenFree = input.glutenFree
            
            try await existingOverride.update(on: req.db)
            
            return Food(
                id: existingOverride.id,
                code: input.code,
                name: input.name,
                brand: input.brand,
                categories: input.categories,
                ingredients: input.ingredients,
                imageUrl: input.imageUrl,
                countries: input.countries,
                glutenFree: input.glutenFree
            )
        }

        // Otherwise, this is a base food item. Create or update an override.
        if let existingOverride = try await UserFoodOverride.query(on: req.db)
            .filter(\.$user.$id == userID)
            .filter(\.$food.$id == foodID)
            .first() {
            
            existingOverride.code = input.code
            existingOverride.name = input.name
            existingOverride.brand = input.brand
            existingOverride.categories = input.categories
            existingOverride.ingredients = input.ingredients
            existingOverride.imageUrl = input.imageUrl
            existingOverride.countries = input.countries
            existingOverride.glutenFree = input.glutenFree
            existingOverride.isDeleted = false
            
            try await existingOverride.update(on: req.db)
        } else {
            let newOverride = UserFoodOverride(
                userID: userID,
                foodID: foodID,
                isDeleted: false,
                code: input.code,
                name: input.name,
                brand: input.brand,
                categories: input.categories,
                ingredients: input.ingredients,
                imageUrl: input.imageUrl,
                countries: input.countries,
                glutenFree: input.glutenFree
            )
            try await newOverride.save(on: req.db)
        }

        return Food(
            id: foodID,
            code: input.code,
            name: input.name,
            brand: input.brand,
            categories: input.categories,
            ingredients: input.ingredients,
            imageUrl: input.imageUrl,
            countries: input.countries,
            glutenFree: input.glutenFree
        )
    }

    // DELETE /foods/:foodID
    func deleteFood(req: Request) async throws -> HTTPStatus {
        let payload = try req.auth.require(AuthController.UserPayload.self)
        guard let userID = UUID(uuidString: payload.subject.value) else {
            throw Abort(.unauthorized)
        }

        guard let foodIDString = req.parameters.get("foodID"), let foodID = UUID(uuidString: foodIDString) else {
            throw Abort(.badRequest, reason: "Invalid product ID")
        }

        req.logger.info("[VAPOR CRUD] User \(userID) requested deletion for food ID: \(foodID)")

        // Check if this is a custom private food
        if let existingOverride = try await UserFoodOverride.query(on: req.db)
            .filter(\.$user.$id == userID)
            .filter(\.$id == foodID)
            .filter(\.$food.$id == nil)
            .first() {
            
            try await existingOverride.delete(on: req.db)
            req.logger.info("[VAPOR CRUD] Successfully deleted custom private food \(foodID)")
            return .noContent
        }

        // Otherwise, this is a base food item. Create or update an override setting isDeleted = true.
        if let existingOverride = try await UserFoodOverride.query(on: req.db)
            .filter(\.$user.$id == userID)
            .filter(\.$food.$id == foodID)
            .first() {
            
            existingOverride.isDeleted = true
            try await existingOverride.update(on: req.db)
        } else {
            let newOverride = UserFoodOverride(
                userID: userID,
                foodID: foodID,
                isDeleted: true
            )
            try await newOverride.save(on: req.db)
        }

        req.logger.info("[VAPOR CRUD] Successfully marked base food \(foodID) as deleted for user \(userID)")
        return .noContent
    }
}
