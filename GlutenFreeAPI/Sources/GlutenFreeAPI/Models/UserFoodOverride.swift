import Vapor
import Fluent
import Foundation

final class UserFoodOverride: Model, Content, @unchecked Sendable {

    static let schema = "user_food_overrides"

    @ID(key: .id)
    var id: UUID?

    @Parent(key: "user_id")
    var user: User

    @OptionalParent(key: "food_id")
    var food: Food?

    @Field(key: "is_deleted")
    var isDeleted: Bool

    @OptionalField(key: "code")
    var code: String?

    @OptionalField(key: "name")
    var name: String?

    @OptionalField(key: "brand")
    var brand: String?

    @OptionalField(key: "categories")
    var categories: String?

    @OptionalField(key: "ingredients")
    var ingredients: String?

    @OptionalField(key: "image_url")
    var imageUrl: String?

    @OptionalField(key: "countries")
    var countries: String?

    @OptionalField(key: "gluten_free")
    var glutenFree: Bool?

    @Timestamp(key: "created_at", on: .create)
    var createdAt: Date?

    init() {}

    init(
        id: UUID? = nil,
        userID: User.IDValue,
        foodID: Food.IDValue? = nil,
        isDeleted: Bool = false,
        code: String? = nil,
        name: String? = nil,
        brand: String? = nil,
        categories: String? = nil,
        ingredients: String? = nil,
        imageUrl: String? = nil,
        countries: String? = nil,
        glutenFree: Bool? = nil
    ) {
        self.id = id
        self.$user.id = userID
        self.$food.id = foodID
        self.isDeleted = isDeleted
        self.code = code
        self.name = name
        self.brand = brand
        self.categories = categories
        self.ingredients = ingredients
        self.imageUrl = imageUrl
        self.countries = countries
        self.glutenFree = glutenFree
    }
}
