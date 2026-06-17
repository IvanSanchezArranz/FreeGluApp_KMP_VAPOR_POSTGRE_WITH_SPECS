# Requirements Checklist: UUID ID Alignment

- [x] All `foods` table queries decode the `id` field as a `UUID` natively without throw.
- [x] Vapor backend compiles on Swift 6.
- [x] KMP shared module compiles successfully on Java 17 / Gradle.
- [x] KMP DTO and Domain mappers map string IDs directly.
