package com.glufree.ktor.security

import org.mindrot.jbcrypt.BCrypt
import java.util.Base64

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "secure_dev_secret_key_change_in_production"

    fun generateToken(userId: String, email: String): String {
        // JWT Header: {"alg":"HS256","typ":"JWT"}
        val header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"

        // JWT Payload: {"sub":"userId","email":"email","exp":timestamp}
        val exp = (System.currentTimeMillis() / 1000) + 2592000 // 30 days
        val payload = """{"sub":"$userId","email":"$email","exp":$exp}"""
        val payloadBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())

        // Signature
        val signature = signHmacSha256("$header.$payloadBase64", secret)
        return "$header.$payloadBase64.$signature"
    }

    fun verifyToken(token: String): DecodedJwt? {
        val parts = token.split(".")
        if (parts.size != 3) return null
        val header = parts[0]
        val payload = parts[1]
        val signature = parts[2]

        // Verify HMAC-SHA256 Signature
        val expectedSignature = signHmacSha256("$header.$payload", secret)
        if (signature != expectedSignature) {
            return null
        }

        // Decode Payload
        val decodedPayload = try {
            String(Base64.getUrlDecoder().decode(payload))
        } catch (e: Exception) {
            return null
        }

        val sub = extractJsonField(decodedPayload, "sub") ?: return null
        val email = extractJsonField(decodedPayload, "email") ?: ""
        val expStr = extractJsonField(decodedPayload, "exp")
        val exp = expStr?.toLongOrNull() ?: 0L

        if (exp < (System.currentTimeMillis() / 1000)) {
            return null // Token expired
        }

        return DecodedJwt(subject = sub, email = email)
    }

    private fun signHmacSha256(data: String, key: String): String {
        val sha256HMAC = javax.crypto.Mac.getInstance("HmacSHA256")
        val secretKey = javax.crypto.spec.SecretKeySpec(key.toByteArray(), "HmacSHA256")
        sha256HMAC.init(secretKey)
        val hash = sha256HMAC.doFinal(data.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }

    private fun extractJsonField(json: String, field: String): String? {
        val pattern = "\"$field\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val match = pattern.find(json)
        if (match != null) {
            return match.groupValues[1]
        }
        val numPattern = "\"$field\"\\s*:\\s*([0-9]+)".toRegex()
        val numMatch = numPattern.find(json)
        if (numMatch != null) {
            return numMatch.groupValues[1]
        }
        return null
    }
}

data class DecodedJwt(val subject: String, val email: String)

object PasswordHasher {
    fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verify(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false
        }
    }
}
