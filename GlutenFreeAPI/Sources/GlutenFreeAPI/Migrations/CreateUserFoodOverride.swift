import Fluent

struct CreateUserFoodOverride: AsyncMigration {
    func prepare(on database: any Database) async throws {
        try await database.schema("user_food_overrides")
            .id()
            .field("user_id", .uuid, .required, .references("users", "id", onDelete: .cascade))
            .field("food_id", .uuid, .references("foods", "id", onDelete: .cascade))
            .field("is_deleted", .bool, .required)
            .field("code", .string)
            .field("name", .string)
            .field("brand", .string)
            .field("categories", .string)
            .field("ingredients", .string)
            .field("image_url", .string)
            .field("countries", .string)
            .field("gluten_free", .bool)
            .field("created_at", .datetime)
            .create()
    }

    func revert(on database: any Database) async throws {
        try await database.schema("user_food_overrides").delete()
    }
}
