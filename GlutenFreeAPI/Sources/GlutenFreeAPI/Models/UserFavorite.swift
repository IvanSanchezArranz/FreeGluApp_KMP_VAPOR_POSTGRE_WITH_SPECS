import Vapor
import Fluent
import Foundation

final class UserFavorite: Model, Content, @unchecked Sendable {

    static let schema = "user_favorites"

    @ID(key: .id)
    var id: UUID?

    @Parent(key: "user_id")
    var user: User

    @Parent(key: "food_id")
    var food: Food

    @Timestamp(key: "created_at", on: .create)
    var createdAt: Date?

    init() {}

    init(id: UUID? = nil, userID: User.IDValue, foodID: Food.IDValue) {
        self.id = id
        self.$user.id = userID
        self.$food.id = foodID
    }
}
