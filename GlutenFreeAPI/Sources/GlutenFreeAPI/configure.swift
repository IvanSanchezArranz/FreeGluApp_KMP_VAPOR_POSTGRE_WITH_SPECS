import NIOSSL
import Fluent
import FluentPostgresDriver
import Leaf
import Vapor

// configures your application
public func configure(_ app: Application) async throws {
    // uncomment to serve files from /Public folder
    // app.middleware.use(FileMiddleware(publicDirectory: app.directory.publicDirectory))

    let corsConfiguration = CORSMiddleware.Configuration(
        allowedOrigin: .all,
        allowedMethods: [.GET, .POST, .PUT, .OPTIONS, .DELETE, .PATCH],
        allowedHeaders: [.accept, .authorization, .contentType, .origin, .xRequestedWith, .userAgent, .accessControlAllowOrigin]
    )
    let cors = CORSMiddleware(configuration: corsConfiguration)
    app.middleware.use(cors, at: .beginning)

    let postgresConfig = SQLPostgresConfiguration(
        hostname: "localhost",
        username: "admin",
        password: "admin",
        database: "glutenfree",
        tls: .disable
    )
    app.databases.use(.postgres(configuration: postgresConfig), as: .psql)
    app.views.use(.leaf)

    // register routes
    try routes(app)
}
