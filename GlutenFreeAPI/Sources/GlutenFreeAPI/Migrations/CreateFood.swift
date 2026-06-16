import Fluent

struct CreateFood: AsyncMigration {
    func prepare(on database: any Database) async throws {
        try await database.schema("foods")
            .field(.id, .int, .identifier(auto: true))
            .field("code", .string, .required)
            .field("name", .string, .required)
            .field("brand", .string)
            .field("categories", .string)
            .field("ingredients", .string)
            .field("image_url", .string)
            .field("countries", .string)
            .field("gluten_free", .bool, .required)
            .field("created_at", .datetime)
            .create()
    }

    func revert(on database: any Database) async throws {
        try await database.schema("foods").delete()
    }
}
