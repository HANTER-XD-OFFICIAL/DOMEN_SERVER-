package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@JsonClass(generateAdapter = true)
data class DomainRequest(
    @Json(name = "id") val id: String = UUID.randomUUID().toString(),
    @Json(name = "domain_name") val domainName: String = "",
    @Json(name = "github_username") val githubUsername: String = "",
    @Json(name = "github_repo") val githubRepo: String = "",
    @Json(name = "client_name") val clientName: String = "",
    @Json(name = "client_email") val clientEmail: String = "",
    @Json(name = "request_notes") val requestNotes: String? = null,
    @Json(name = "status") val status: String = "pending", // pending, approved, active, rejected, suspended
    @Json(name = "ssl_status") val sslStatus: String = "pending", // pending, active, failed
    @Json(name = "cname_target") val cnameTarget: String? = null,
    @Json(name = "dns_verification_token") val dnsVerificationToken: String? = null,
    @Json(name = "dns_configured") val dnsConfigured: Boolean = false,
    @Json(name = "admin_notes") val adminNotes: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
) {
    val isApex: Boolean
        get() = domainName.count { it == '.' } == 1

    val resolvedCnameTarget: String
        get() = cnameTarget ?: "${githubUsername.lowercase().trim()}.github.io"

    val verificationToken: String
        get() = dnsVerificationToken ?: "gh-verify-${id.take(8)}"

    val cnameFileContent: String
        get() = domainName.trim().lowercase()

    val formattedDate: String
        get() {
            if (createdAt.isNullOrBlank()) return "Just now"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
                val date = inputFormat.parse(createdAt.substringBefore("."))
                date?.let { outputFormat.format(it) } ?: createdAt
            } catch (e: Exception) {
                createdAt
            }
        }
}

@JsonClass(generateAdapter = true)
data class NewDomainRequestPayload(
    @Json(name = "id") val id: String,
    @Json(name = "domain_name") val domainName: String,
    @Json(name = "github_username") val githubUsername: String,
    @Json(name = "github_repo") val githubRepo: String,
    @Json(name = "client_name") val clientName: String,
    @Json(name = "client_email") val clientEmail: String,
    @Json(name = "request_notes") val requestNotes: String?,
    @Json(name = "status") val status: String = "pending",
    @Json(name = "ssl_status") val sslStatus: String = "pending",
    @Json(name = "cname_target") val cnameTarget: String,
    @Json(name = "dns_verification_token") val dnsVerificationToken: String,
    @Json(name = "dns_configured") val dnsConfigured: Boolean = false,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class UpdateDomainPayload(
    @Json(name = "status") val status: String? = null,
    @Json(name = "ssl_status") val sslStatus: String? = null,
    @Json(name = "dns_configured") val dnsConfigured: Boolean? = null,
    @Json(name = "admin_notes") val adminNotes: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

data class DnsRecordItem(
    val type: String, // CNAME, A, TXT
    val host: String, // e.g., @ or subdomain or _github-pages-challenge
    val value: String, // target or IP
    val ttl: String = "3600",
    val description: String
)

data class SystemMetrics(
    val totalRequests: Int = 0,
    val pendingCount: Int = 0,
    val approvedCount: Int = 0,
    val activeCount: Int = 0,
    val rejectedCount: Int = 0,
    val sslActiveCount: Int = 0,
    val supabaseOnline: Boolean = false,
    val latencyMs: Long = 0L,
    val lastSyncTime: String = "Never"
)
