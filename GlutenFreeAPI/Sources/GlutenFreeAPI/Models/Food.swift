//
//  Food.swift
//  GlutenFreeAPI
//
//  Created by Ivan Sanchez Arranz on 12/6/26.
//

import Vapor
import Fluent

final class Food: Model, Content, @unchecked Sendable {

    static let schema = "foods"

    @ID(custom: .id, generatedBy: .database)
    var id: Int?

    @Field(key: "code")
    var code: String

    @Field(key: "name")
    var name: String

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

    @Field(key: "gluten_free")
    var glutenFree: Bool

    @Timestamp(key: "created_at", on: .create)
    var createdAt: Date?

    init() {}

    init(
        id: Int? = nil,
        code: String,
        name: String,
        brand: String?,
        categories: String?,
        ingredients: String?,
        imageUrl: String?,
        countries: String?,
        glutenFree: Bool
    ) {
        self.id = id
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
