package com.example

import com.example.data.model.DomainRequest
import com.example.data.repository.DomainRepository
import com.example.data.supabase.SupabaseConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainHostingUnitTest {

    @Test
    fun testSupabaseConfig_hasCorrectCredentials() {
        assertEquals("https://yejqvregkkyhxxjblnii.supabase.co", SupabaseConfig.PROJECT_URL)
        assertEquals("sb_publishable_BSvIvMTDe0rgsZjpnWUxjw_wkWv6I7C", SupabaseConfig.ANON_KEY)
        assertTrue(SupabaseConfig.SQL_SCHEMA.contains("CREATE TABLE IF NOT EXISTS domain_requests"))
        assertTrue(SupabaseConfig.SQL_SCHEMA.contains("ENABLE ROW LEVEL SECURITY"))
    }

    @Test
    fun testDomainRequest_apexDomainDetection() {
        val apexDomain = DomainRequest(domainName = "alexdev.me", githubUsername = "alexraselchodhury")
        assertTrue(apexDomain.isApex)

        val subDomain = DomainRequest(domainName = "docs.alexdev.me", githubUsername = "alexraselchodhury")
        assertFalse(subDomain.isApex)
    }

    @Test
    fun testDnsRecords_apexDomainGeneratesGitHubIpsAndCname() {
        val repository = DomainRepository()
        val apexDomain = DomainRequest(
            id = "test-uuid-1",
            domainName = "alexdev.me",
            githubUsername = "alexraselchodhury",
            githubRepo = "alexraselchodhury.github.io"
        )

        val records = repository.generateDnsRecords(apexDomain)
        val aRecords = records.filter { it.type == "A" }
        assertEquals(4, aRecords.size)

        val expectedIps = setOf(
            "185.199.108.153",
            "185.199.109.153",
            "185.199.110.153",
            "185.199.111.153"
        )
        val generatedIps = aRecords.map { it.value }.toSet()
        assertEquals(expectedIps, generatedIps)

        val cnameRecord = records.find { it.type == "CNAME" }
        assertNotNull(cnameRecord)
        assertEquals("alexraselchodhury.github.io", cnameRecord?.value)
        assertEquals("www", cnameRecord?.host)

        val txtRecord = records.find { it.type == "TXT" }
        assertNotNull(txtRecord)
        assertTrue(txtRecord?.host?.contains("alexraselchodhury") == true)
    }

    @Test
    fun testDnsRecords_subdomainGeneratesDirectCname() {
        val repository = DomainRepository()
        val subDomain = DomainRequest(
            id = "test-uuid-2",
            domainName = "api.alexdev.me",
            githubUsername = "alexraselchodhury",
            githubRepo = "my-api"
        )

        val records = repository.generateDnsRecords(subDomain)
        val aRecords = records.filter { it.type == "A" }
        assertEquals(0, aRecords.size)

        val cnameRecord = records.find { it.type == "CNAME" }
        assertNotNull(cnameRecord)
        assertEquals("api", cnameRecord?.host)
        assertEquals("alexraselchodhury.github.io", cnameRecord?.value)
    }

    @Test
    fun testStatusTransitions() = runBlocking {
        val repository = DomainRepository()
        val initialList = repository.domains.value
        val pendingItem = initialList.find { it.status == "pending" } ?: initialList.first()

        // Test Approve
        repository.approveRequest(pendingItem.id, "Approved in test")
        val approvedItem = repository.domains.value.find { it.id == pendingItem.id }
        assertEquals("approved", approvedItem?.status)
        assertTrue(approvedItem?.dnsConfigured == true)

        // Test Activate
        repository.activateDomain(pendingItem.id)
        val activeItem = repository.domains.value.find { it.id == pendingItem.id }
        assertEquals("active", activeItem?.status)
        assertEquals("active", activeItem?.sslStatus)

        // Test Suspend
        repository.suspendDomain(pendingItem.id, "Testing suspension")
        val suspendedItem = repository.domains.value.find { it.id == pendingItem.id }
        assertEquals("suspended", suspendedItem?.status)
    }

    @Test
    fun testCreateNewRequest() = runBlocking {
        val repository = DomainRepository()
        val created = repository.createNewRequest(
            domainName = "newclient.dev",
            githubUsername = "clientdev",
            githubRepo = "clientdev.github.io",
            clientName = "Client Person",
            clientEmail = "client@domain.com",
            notes = "Test notes"
        )

        assertEquals("newclient.dev", created.domainName)
        assertEquals("pending", created.status)
        assertEquals("clientdev.github.io", created.resolvedCnameTarget)
        assertTrue(repository.domains.value.any { it.id == created.id })
    }
}
