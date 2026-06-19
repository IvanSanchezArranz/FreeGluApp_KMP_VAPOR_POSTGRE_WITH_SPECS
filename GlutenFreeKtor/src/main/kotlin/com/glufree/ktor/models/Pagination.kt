package com.glufree.ktor.models

import kotlinx.serialization.Serializable

@Serializable
data class PaginationMetadata(
    val page: Int,
    val per: Int,
    val total: Long
)

@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val metadata: PaginationMetadata
)

@Serializable
data class ErrorResponse(
    val error: Boolean,
    val reason: String
)
