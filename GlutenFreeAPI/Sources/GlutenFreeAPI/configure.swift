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

    let defaultDatabase = app.environment == .testing ? "glutenfree_test" : "glutenfree"

    let postgresConfig: SQLPostgresConfiguration
    if let databaseURL = Environment.get("DATABASE_URL") {
        var config = try SQLPostgresConfiguration(url: databaseURL)
        // Configure unverified TLS for database connections (required for Render/Heroku Postgres SSL)
        var tlsConfig = TLSConfiguration.makeClientConfiguration()
        tlsConfig.certificateVerification = .none
        let nioSSLContext = try NIOSSLContext(configuration: tlsConfig)
        config.coreConfiguration.tls = .require(nioSSLContext)
        postgresConfig = config
    } else {
        postgresConfig = SQLPostgresConfiguration(
            hostname: Environment.get("DATABASE_HOST") ?? "127.0.0.1",
            username: Environment.get("DATABASE_USERNAME") ?? "admin",
            password: Environment.get("DATABASE_PASSWORD") ?? "admin",
            database: Environment.get("DATABASE_NAME") ?? defaultDatabase,
            tls: .disable
        )
    }
    app.databases.use(.postgres(configuration: postgresConfig), as: .psql)
    app.views.use(.leaf)

    // Configure JWT
    let jwtSecret = Environment.get("JWT_SECRET") ?? "secure_dev_secret_key_change_in_production"
    app.jwt.signers.use(.hs256(key: jwtSecret))

    // Register migrations
    app.migrations.add(CreateFood())
    app.migrations.add(CreateUser())
    app.migrations.add(CreateUserFavorite())

    // register routes
    try routes(app)
}
