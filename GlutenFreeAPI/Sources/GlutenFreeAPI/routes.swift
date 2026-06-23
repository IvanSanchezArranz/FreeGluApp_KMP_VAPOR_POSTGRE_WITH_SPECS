import Fluent
import Vapor

func routes(_ app: Application) throws {

    app.get { req async -> String in
        return "API Gluten Free funcionando 🚀"
    }

    try app.register(collection: FoodController())
    try app.register(collection: AuthController())
}
