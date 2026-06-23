import Fluent

struct CreateUserFavorite: AsyncMigration {
    func prepare(on database: any Database) async throws {
        try await database.schema("user_favorites")
            .id()
            .field("user_id", .uuid, .required, .references("users", "id", onDelete: .cascade))
            .field("food_id", .uuid, .required, .references("foods", "id", onDelete: .cascade))
            .field("created_at", .datetime)
            .unique(on: "user_id", "food_id")
            .create()
    }

    func revert(on database: any Database) async throws {
        try await database.schema("user_favorites").delete()
    }
}
