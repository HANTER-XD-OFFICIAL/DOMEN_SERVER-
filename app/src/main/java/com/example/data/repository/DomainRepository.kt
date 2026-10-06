package com.example.data.repository

import android.util.Log
import com.example.data.model.DnsRecordItem
import com.example.data.model.DomainRequest
import com.example.data.model.NewDomainRequestPayload
import com.example.data.model.SystemMetrics
import com.example.data.model.UpdateDomainPayload
import com.example.data.supabase.SupabaseClient
import com.example.data.supabase.SupabaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class SupabaseConnectionState {
    object Checking : SupabaseConnectionState()
    data class Connected(val latencyMs: Long, val recordCount: Int) : SupabaseConnectionState()
    data class TableNotFound(val message: String) : SupabaseConnectionState()
    data class Error(val error: String) : SupabaseConnectionState()
}

data class DnsCheckResult(
    val domain: String,
    val resolvedIps: List<String>,
    val matchesGitHubIps: Boolean,
    val cnameFound: Boolean,
    val cnameTarget: String,
    val isOnline: Boolean,
    val message: String
)

class DomainRepository {

    private val api = SupabaseClient.api
    private val httpClient = SupabaseClient.rawOkHttpClient

    private val _domains = MutableStateFlow<List<DomainRequest>>(emptyList())
    val domains: StateFlow<List<DomainRequest>> = _domains.asStateFlow()

    private val _connectionState = MutableStateFlow<SupabaseConnectionState>(SupabaseConnectionState.Checking)
    val connectionState: StateFlow<SupabaseConnectionState> = _connectionState.asStateFlow()

    private val _metrics = MutableStateFlow(SystemMetrics())
    val metrics: StateFlow<SystemMetrics> = _metrics.asStateFlow()

    // Default pre-loaded realistic requests to ensure rich admin UX immediately
    private val sampleDomains = listOf(
        DomainRequest(
            id = "c7891234-5678-4321-8765-abcdef123456",
            domainName = "alexdev.me",
            githubUsername = "alexraselchodhury",
            githubRepo = "alexraselchodhury.github.io",
            clientName = "Alex Rasel",
            clientEmail = "alexraselchodhury@gmail.com",
            requestNotes = "Portfolio and technical blog hosted on GitHub Pages. Need custom domain with automatic SSL.",
            status = "pending",
            sslStatus = "pending",
            cnameTarget = "alexraselchodhury.github.io",
            dnsVerificationToken = "gh-verify-c7891234",
            dnsConfigured = false,
            adminNotes = "Awaiting DNS record propagation verification",
            createdAt = "2026-10-06T08:30:00Z"
        ),
        DomainRequest(
            id = "d1234567-89ab-cdef-0123-456789abcdef",
            domainName = "docs.cloudcraft.io",
            githubUsername = "devteam-ops",
            githubRepo = "cloudcraft-docs",
            clientName = "Sarah Jenkins",
            clientEmail = "sarah.j@cloudcraft.io",
            requestNotes = "Documentation subdomain for our open source developer framework.",
            status = "approved",
            sslStatus = "active",
            cnameTarget = "devteam-ops.github.io",
            dnsVerificationToken = "gh-verify-d1234567",
            dnsConfigured = true,
            adminNotes = "Approved by admin. CNAME records verified.",
            createdAt = "2026-10-05T14:15:00Z"
        ),
        DomainRequest(
            id = "e9876543-210f-edcb-a987-654321fedcba",
            domainName = "nordicstudio.design",
            githubUsername = "nordic-design",
            githubRepo = "nordic-studio-site",
            clientName = "Marcus Lindqvist",
            clientEmail = "marcus@nordicstudio.design",
            requestNotes = "High-performance agency showcase site on GitHub Pages with Astro static build.",
            status = "active",
            sslStatus = "active",
            cnameTarget = "nordic-design.github.io",
            dnsVerificationToken = "gh-verify-e9876543",
            dnsConfigured = true,
            adminNotes = "Production domain active. Enforce HTTPS enabled.",
            createdAt = "2026-10-04T10:00:00Z"
        ),
        DomainRequest(
            id = "f4567890-1234-5678-90ab-cdef12345678",
            domainName = "crypto-moon-tracker.xyz",
            githubUsername = "anon-trader",
            githubRepo = "pump-tracker",
            clientName = "Anonymous",
            clientEmail = "anon@protonmail.com",
            requestNotes = "Spam/unverified bot dashboard.",
            status = "rejected",
            sslStatus = "failed",
            cnameTarget = "anon-trader.github.io",
            dnsVerificationToken = "gh-verify-f4567890",
            dnsConfigured = false,
            adminNotes = "Rejected: Incomplete GitHub verification and invalid contact info.",
            createdAt = "2026-10-03T09:20:00Z"
        )
    )

    init {
        _domains.value = sampleDomains
        updateMetrics()
    }

    suspend fun refresh(): Result<List<DomainRequest>> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            _connectionState.value = SupabaseConnectionState.Checking
            val response = api.getAllDomainRequests()
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val remoteList = response.body() ?: emptyList()
                if (remoteList.isNotEmpty()) {
                    _domains.value = remoteList
                } else if (_domains.value.isEmpty()) {
                    _domains.value = sampleDomains
                }
                _connectionState.value = SupabaseConnectionState.Connected(latency, _domains.value.size)
                updateMetrics(true, latency)
                Result.success(_domains.value)
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                Log.w("DomainRepository", "Supabase error code ${response.code()}: $errorBody")
                if (response.code() == 404 || errorBody.contains("relation \"domain_requests\" does not exist") || response.code() == 400) {
                    _connectionState.value = SupabaseConnectionState.TableNotFound(
                        "Table 'domain_requests' not created yet in Supabase. Run SQL schema in Supabase SQL Editor."
                    )
                } else {
                    _connectionState.value = SupabaseConnectionState.Error("HTTP ${response.code()}: $errorBody")
                }
                updateMetrics(false, latency)
                Result.success(_domains.value) // Keep cached/sample data active so admin can operate
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            Log.e("DomainRepository", "Network or Supabase exception", e)
            _connectionState.value = SupabaseConnectionState.Error(e.localizedMessage ?: "Unknown error")
            updateMetrics(false, latency)
            Result.success(_domains.value)
        }
    }

    suspend fun pingSupabase(): SupabaseConnectionState = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url("${SupabaseConfig.PROJECT_URL}/rest/v1/")
                .header("apikey", SupabaseConfig.ANON_KEY)
                .header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .build()

            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - start
            val state = if (response.isSuccessful || response.code == 200 || response.code == 304) {
                SupabaseConnectionState.Connected(latency, _domains.value.size)
            } else {
                SupabaseConnectionState.Error("HTTP ${response.code}: ${response.message}")
            }
            _connectionState.value = state
            updateMetrics(response.isSuccessful, latency)
            state
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - start
            val state = SupabaseConnectionState.Error(e.message ?: "Connection failed")
            _connectionState.value = state
            updateMetrics(false, latency)
            state
        }
    }

    suspend fun approveRequest(id: String, notes: String? = null): Boolean = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val updatedList = _domains.value.map {
            if (it.id == id) {
                it.copy(
                    status = "approved",
                    sslStatus = "pending",
                    dnsConfigured = true,
                    adminNotes = notes ?: "Approved by Admin. Ready for DNS verification.",
                    updatedAt = now
                )
            } else it
        }
        _domains.value = updatedList
        updateMetrics()

        try {
            api.updateDomainRequest(
                idFilter = "eq.$id",
                payload = UpdateDomainPayload(
                    status = "approved",
                    sslStatus = "pending",
                    dnsConfigured = true,
                    adminNotes = notes ?: "Approved by Admin. Ready for DNS verification.",
                    updatedAt = now
                )
            )
        } catch (e: Exception) {
            Log.w("DomainRepository", "Failed to sync approval to Supabase: ${e.message}")
        }
        true
    }

    suspend fun activateDomain(id: String): Boolean = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val updatedList = _domains.value.map {
            if (it.id == id) {
                it.copy(
                    status = "active",
                    sslStatus = "active",
                    dnsConfigured = true,
                    adminNotes = "Domain hosting is live on GitHub Pages with HTTPS.",
                    updatedAt = now
                )
            } else it
        }
        _domains.value = updatedList
        updateMetrics()

        try {
            api.updateDomainRequest(
                idFilter = "eq.$id",
                payload = UpdateDomainPayload(
                    status = "active",
                    sslStatus = "active",
                    dnsConfigured = true,
                    adminNotes = "Domain hosting is live on GitHub Pages with HTTPS.",
                    updatedAt = now
                )
            )
        } catch (e: Exception) {
            Log.w("DomainRepository", "Failed to sync activation to Supabase: ${e.message}")
        }
        true
    }

    suspend fun rejectRequest(id: String, reason: String): Boolean = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val updatedList = _domains.value.map {
            if (it.id == id) {
                it.copy(
                    status = "rejected",
                    adminNotes = reason,
                    updatedAt = now
                )
            } else it
        }
        _domains.value = updatedList
        updateMetrics()

        try {
            api.updateDomainRequest(
                idFilter = "eq.$id",
                payload = UpdateDomainPayload(
                    status = "rejected",
                    adminNotes = reason,
                    updatedAt = now
                )
            )
        } catch (e: Exception) {
            Log.w("DomainRepository", "Failed to sync rejection to Supabase: ${e.message}")
        }
        true
    }

    suspend fun suspendDomain(id: String, reason: String = "Suspended by admin"): Boolean = withContext(Dispatchers.IO) {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val updatedList = _domains.value.map {
            if (it.id == id) {
                it.copy(
                    status = "suspended",
                    adminNotes = reason,
                    updatedAt = now
                )
            } else it
        }
        _domains.value = updatedList
        updateMetrics()

        try {
            api.updateDomainRequest(
                idFilter = "eq.$id",
                payload = UpdateDomainPayload(
                    status = "suspended",
                    adminNotes = reason,
                    updatedAt = now
                )
            )
        } catch (e: Exception) {
            Log.w("DomainRepository", "Failed to sync suspension: ${e.message}")
        }
        true
    }

    suspend fun deleteRequest(id: String): Boolean = withContext(Dispatchers.IO) {
        _domains.value = _domains.value.filter { it.id != id }
        updateMetrics()

        try {
            api.deleteDomainRequest("eq.$id")
        } catch (e: Exception) {
            Log.w("DomainRepository", "Failed to sync deletion: ${e.message}")
        }
        true
    }

    suspend fun createNewRequest(
        domainName: String,
        githubUsername: String,
        githubRepo: String,
        clientName: String,
        clientEmail: String,
        notes: String?
    ): DomainRequest = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val cleanDomain = domainName.trim().lowercase()
        val cleanUser = githubUsername.trim()
        val cnameTarget = "${cleanUser.lowercase()}.github.io"
        val token = "gh-verify-${id.take(8)}"

        val newRequest = DomainRequest(
            id = id,
            domainName = cleanDomain,
            githubUsername = cleanUser,
            githubRepo = githubRepo.trim(),
            clientName = clientName.trim(),
            clientEmail = clientEmail.trim(),
            requestNotes = notes,
            status = "pending",
            sslStatus = "pending",
            cnameTarget = cnameTarget,
            dnsVerificationToken = token,
            dnsConfigured = false,
            adminNotes = "Submitted via Portal. Pending admin review.",
            createdAt = now,
            updatedAt = now
        )

        _domains.value = listOf(newRequest) + _domains.value
        updateMetrics()

        try {
            val payload = NewDomainRequestPayload(
                id = id,
                domainName = cleanDomain,
                githubUsername = cleanUser,
                githubRepo = githubRepo.trim(),
                clientName = clientName.trim(),
                clientEmail = clientEmail.trim(),
                requestNotes = notes,
                status = "pending",
                sslStatus = "pending",
                cnameTarget = cnameTarget,
                dnsVerificationToken = token,
                dnsConfigured = false,
                createdAt = now
            )
            api.createDomainRequest(payload)
        } catch (e: Exception) {
            Log.w("DomainRepository", "Failed to post to Supabase: ${e.message}")
        }

        newRequest
    }

    suspend fun seedSampleDataToSupabase(): Int = withContext(Dispatchers.IO) {
        var count = 0
        sampleDomains.forEach { sample ->
            try {
                val payload = NewDomainRequestPayload(
                    id = sample.id,
                    domainName = sample.domainName,
                    githubUsername = sample.githubUsername,
                    githubRepo = sample.githubRepo,
                    clientName = sample.clientName,
                    clientEmail = sample.clientEmail,
                    requestNotes = sample.requestNotes,
                    status = sample.status,
                    sslStatus = sample.sslStatus,
                    cnameTarget = sample.resolvedCnameTarget,
                    dnsVerificationToken = sample.verificationToken,
                    dnsConfigured = sample.dnsConfigured,
                    createdAt = sample.createdAt ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                )
                api.createDomainRequest(payload)
                count++
            } catch (e: Exception) {
                Log.w("DomainRepository", "Seed error for ${sample.domainName}: ${e.message}")
            }
        }
        refresh()
        count
    }

    suspend fun verifyDns(domain: DomainRequest): DnsCheckResult = withContext(Dispatchers.IO) {
        try {
            val ips = try {
                val addresses = InetAddress.getAllByName(domain.domainName)
                addresses.map { it.hostAddress }.filterNotNull()
            } catch (e: Exception) {
                emptyList()
            }

            val githubIps = listOf("185.199.108.153", "185.199.109.153", "185.199.110.153", "185.199.111.153")
            val matchesGitHub = ips.any { it in githubIps }

            val isOnline = ips.isNotEmpty()
            val message = when {
                matchesGitHub -> "DNS correctly points to GitHub Pages servers (${ips.joinToString()})"
                ips.isNotEmpty() -> "Domain resolves to ${ips.joinToString()}, awaiting propagation to GitHub Pages"
                else -> "DNS not yet propagated. Please add CNAME/A records in domain registrar."
            }

            DnsCheckResult(
                domain = domain.domainName,
                resolvedIps = ips,
                matchesGitHubIps = matchesGitHub,
                cnameFound = isOnline,
                cnameTarget = domain.resolvedCnameTarget,
                isOnline = isOnline,
                message = message
            )
        } catch (e: Exception) {
            DnsCheckResult(
                domain = domain.domainName,
                resolvedIps = emptyList(),
                matchesGitHubIps = false,
                cnameFound = false,
                cnameTarget = domain.resolvedCnameTarget,
                isOnline = false,
                message = "DNS Lookup test: ${e.localizedMessage ?: "Unresolved"}"
            )
        }
    }

    fun generateDnsRecords(domain: DomainRequest): List<DnsRecordItem> {
        val records = mutableListOf<DnsRecordItem>()

        if (domain.isApex) {
            // Apex domain needs 4 A records
            listOf(
                "185.199.108.153",
                "185.199.109.153",
                "185.199.110.153",
                "185.199.111.153"
            ).forEach { ip ->
                records.add(
                    DnsRecordItem(
                        type = "A",
                        host = "@",
                        value = ip,
                        description = "GitHub Pages Apex Server IP"
                    )
                )
            }
            // And www CNAME
            records.add(
                DnsRecordItem(
                    type = "CNAME",
                    host = "www",
                    value = domain.resolvedCnameTarget,
                    description = "Redirects www.${domain.domainName} to GitHub Pages"
                )
            )
        } else {
            // Subdomain CNAME
            val sub = domain.domainName.substringBefore(".")
            records.add(
                DnsRecordItem(
                    type = "CNAME",
                    host = sub,
                    value = domain.resolvedCnameTarget,
                    description = "Points subdomain directly to GitHub Pages deployment"
                )
            )
        }

        // GitHub Pages Domain Verification TXT record
        records.add(
            DnsRecordItem(
                type = "TXT",
                host = "_github-pages-challenge-${domain.githubUsername}",
                value = domain.verificationToken,
                description = "GitHub Domain Ownership Verification Challenge"
            )
        )

        return records
    }

    private fun updateMetrics(isOnline: Boolean? = null, latency: Long? = null) {
        val current = _domains.value
        val now = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        _metrics.value = SystemMetrics(
            totalRequests = current.size,
            pendingCount = current.count { it.status == "pending" },
            approvedCount = current.count { it.status == "approved" },
            activeCount = current.count { it.status == "active" },
            rejectedCount = current.count { it.status == "rejected" },
            sslActiveCount = current.count { it.sslStatus == "active" },
            supabaseOnline = isOnline ?: _metrics.value.supabaseOnline,
            latencyMs = latency ?: _metrics.value.latencyMs,
            lastSyncTime = now
        )
    }
}
