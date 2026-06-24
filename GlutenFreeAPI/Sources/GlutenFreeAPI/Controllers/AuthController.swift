import Vapor
import Fluent
import JWT

struct AuthController: RouteCollection {

    func boot(routes: any RoutesBuilder) throws {
        routes.post("register", use: register)
        routes.post("login", use: login)

        let protected = routes.grouped(UserMiddleware())
        protected.post("favorites", "sync", use: syncFavorites)
        protected.get("favorites", use: getFavorites)
        protected.post("favorites", ":foodID", use: addFavorite)
        protected.delete("favorites", ":foodID", use: deleteFavorite)
    }

    // DTOs
    struct AuthInput: Content {
        var email: String
        var password: String
    }

    struct AuthResponse: Content {
        var token: String
        var user: UserResponse
    }

    struct UserResponse: Content {
        var id: UUID
        var email: String
    }

    struct SyncFavoritesInput: Content {
        var foodIds: [String]
    }

    struct SyncResponse: Content {
        var success: Bool
        var syncedCount: Int
    }

    // JWT Payload
    struct UserPayload: JWTPayload, Authenticatable {
        var subject: SubjectClaim
        var expiration: ExpirationClaim
        var email: String

        func verify(using signer: JWTSigner) throws {
            try self.expiration.verifyNotExpired()
        }
    }

    // Register User
    func register(req: Request) async throws -> Response {
        try AuthInput.validate(content: req)
        let input = try req.content.decode(AuthInput.self)

        guard !input.email.isEmpty, !input.password.isEmpty else {
            throw Abort(.badRequest, reason: "Email and password cannot be empty")
        }

        // Check duplicate
        let existingUser = try await User.query(on: req.db)
            .filter(\.$email == input.email)
            .first()

        if existingUser != nil {
            throw Abort(.conflict, reason: "Email is already registered")
        }

        // Hash password
        let passwordHash = try req.password.hash(input.password)

        // Save User
        let user = User(email: input.email, passwordHash: passwordHash)
        try await user.save(on: req.db)

        guard let userID = user.id else {
            throw Abort(.internalServerError)
        }

        // Generate Token
        let payload = UserPayload(
            subject: .init(value: userID.uuidString),
            expiration: .init(value: Date().addingTimeInterval(2592000)), // 30 days
            email: user.email
        )
        let token = try req.jwt.sign(payload)

        let response = AuthResponse(
            token: token,
            user: UserResponse(id: userID, email: user.email)
        )

        return try await response.encodeResponse(status: .created, for: req)
    }

    // Login User
    func login(req: Request) async throws -> AuthResponse {
        let input = try req.content.decode(AuthInput.self)

        // Find User
        guard let user = try await User.query(on: req.db)
            .filter(\.$email == input.email)
            .first() else {
            throw Abort(.unauthorized, reason: "Invalid email or password")
        }

        // Verify password
        let isPasswordCorrect = try req.password.verify(input.password, created: user.passwordHash)
        guard isPasswordCorrect else {
            throw Abort(.unauthorized, reason: "Invalid email or password")
        }

        guard let userID = user.id else {
            throw Abort(.internalServerError)
        }

        // Generate Token
        let payload = UserPayload(
            subject: .init(value: userID.uuidString),
            expiration: .init(value: Date().addingTimeInterval(2592000)), // 30 days
            email: user.email
        )
        let token = try req.jwt.sign(payload)

        return AuthResponse(
            token: token,
            user: UserResponse(id: userID, email: user.email)
        )
    }

    // Sync Favorites
    func syncFavorites(req: Request) async throws -> SyncResponse {
        let payload = try req.auth.require(UserPayload.self)
        guard let userID = UUID(uuidString: payload.subject.value) else {
            throw Abort(.unauthorized)
        }

        let input = try req.content.decode(SyncFavoritesInput.self)

        var syncedCount = 0
        for foodIDStr in input.foodIds {
            guard let foodID = UUID(uuidString: foodIDStr) else {
                continue // Skip invalid/legacy IDs (barcodes, etc.) gracefully
            }
            
            // Check if already favorited
            let exists = try await UserFavorite.query(on: req.db)
                .filter(\.$user.$id == userID)
                .filter(\.$food.$id == foodID)
                .first()

            if exists == nil {
                // Verify food exists
                if let food = try await Food.find(foodID, on: req.db) {
                    let favorite = UserFavorite(userID: userID, foodID: try food.requireID())
                    try await favorite.save(on: req.db)
                    syncedCount += 1
                }
            } else {
                syncedCount += 1
            }
        }

        return SyncResponse(success: true, syncedCount: syncedCount)
    }

    // Get Favorites
    func getFavorites(req: Request) async throws -> [Food] {
        let payload = try req.auth.require(UserPayload.self)
        guard let userID = UUID(uuidString: payload.subject.value) else {
            throw Abort(.unauthorized)
        }

        let favorites = try await UserFavorite.query(on: req.db)
            .filter(\.$user.$id == userID)
            .with(\.$food)
            .all()

        return favorites.map { $0.food }
    }

    struct MessageResponse: Content {
        var success: Bool
        var message: String
    }

    // Add single favorite
    func addFavorite(req: Request) async throws -> Response {
        let payload = try req.auth.require(UserPayload.self)
        guard let userID = UUID(uuidString: payload.subject.value) else {
            throw Abort(.unauthorized)
        }

        guard let foodIDString = req.parameters.get("foodID"), let foodID = UUID(uuidString: foodIDString) else {
            throw Abort(.badRequest, reason: "Invalid food ID")
        }

        // Verify food exists
        guard let food = try await Food.find(foodID, on: req.db) else {
            throw Abort(.notFound, reason: "Food not found")
        }

        // Check if already favorited
        let resolvedFoodID = try food.requireID()
        let exists = try await UserFavorite.query(on: req.db)
            .filter(\.$user.$id == userID)
            .filter(\.$food.$id == resolvedFoodID)
            .first()

        if exists != nil {
            return try await MessageResponse(success: true, message: "Already in favorites").encodeResponse(status: .ok, for: req)
        }

        let favorite = UserFavorite(userID: userID, foodID: resolvedFoodID)
        try await favorite.save(on: req.db)

        return try await MessageResponse(success: true, message: "Favorite added").encodeResponse(status: .created, for: req)
    }

    // Delete single favorite
    func deleteFavorite(req: Request) async throws -> MessageResponse {
        let payload = try req.auth.require(UserPayload.self)
        guard let userID = UUID(uuidString: payload.subject.value) else {
            throw Abort(.unauthorized)
        }

        guard let foodIDString = req.parameters.get("foodID"), let foodID = UUID(uuidString: foodIDString) else {
            throw Abort(.badRequest, reason: "Invalid food ID")
        }

        // Find favorite and delete
        let exists = try await UserFavorite.query(on: req.db)
            .filter(\.$user.$id == userID)
            .filter(\.$food.$id == foodID)
            .first()

        if let favorite = exists {
            try await favorite.delete(on: req.db)
        }

        return MessageResponse(success: true, message: "Favorite removed")
    }
}

// Simple authenticating middleware
struct UserMiddleware: AsyncMiddleware {
    func respond(to request: Request, chainingTo next: any AsyncResponder) async throws -> Response {
        guard let token = request.headers.bearerAuthorization?.token else {
            throw Abort(.unauthorized, reason: "Bearer token missing")
        }

        do {
            let payload = try request.jwt.verify(token, as: AuthController.UserPayload.self)
            request.auth.login(payload)
        } catch {
            throw Abort(.unauthorized, reason: "Bearer token invalid or expired")
        }

        return try await next.respond(to: request)
    }
}

extension AuthController.AuthInput: Validatable {
    static func WoodKey(_ email: String) -> Bool {
        return email.contains("@") && email.contains(".")
    }

    static func validations(_ validations: inout Validations) {
        validations.add("email", as: String.self, is: .email)
        validations.add("password", as: String.self, is: .count(8...))
    }
}
