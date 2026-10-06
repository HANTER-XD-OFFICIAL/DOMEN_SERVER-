package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DomainRequest
import com.example.data.model.SystemMetrics
import com.example.data.repository.DnsCheckResult
import com.example.data.repository.DomainRepository
import com.example.data.repository.SupabaseConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DomainViewModel(
    val repository: DomainRepository = DomainRepository()
) : ViewModel() {

    val metrics: StateFlow<SystemMetrics> = repository.metrics
    val connectionState: StateFlow<SupabaseConnectionState> = repository.connectionState

    private val _filterStatus = MutableStateFlow("all")
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedDomain = MutableStateFlow<DomainRequest?>(null)
    val selectedDomain: StateFlow<DomainRequest?> = _selectedDomain.asStateFlow()

    private val _dnsChecking = MutableStateFlow(false)
    val dnsChecking: StateFlow<Boolean> = _dnsChecking.asStateFlow()

    private val _dnsCheckResult = MutableStateFlow<DnsCheckResult?>(null)
    val dnsCheckResult: StateFlow<DnsCheckResult?> = _dnsCheckResult.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _showNewRequestDialog = MutableStateFlow(false)
    val showNewRequestDialog: StateFlow<Boolean> = _showNewRequestDialog.asStateFlow()

    private val _showSqlDialog = MutableStateFlow(false)
    val showSqlDialog: StateFlow<Boolean> = _showSqlDialog.asStateFlow()

    // Filtered domain list
    val filteredDomains: StateFlow<List<DomainRequest>> = combine(
        repository.domains,
        _filterStatus,
        _searchQuery
    ) { allDomains, filter, query ->
        allDomains.filter { item ->
            val matchesFilter = when (filter) {
                "all" -> true
                else -> item.status.equals(filter, ignoreCase = true)
            }
            val matchesQuery = if (query.isBlank()) true else {
                item.domainName.contains(query, ignoreCase = true) ||
                item.githubUsername.contains(query, ignoreCase = true) ||
                item.clientName.contains(query, ignoreCase = true) ||
                item.clientEmail.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refresh()
            _isRefreshing.value = false
        }
    }

    fun testSupabase() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val res = repository.pingSupabase()
            _isRefreshing.value = false
            when (res) {
                is SupabaseConnectionState.Connected -> {
                    _userMessage.value = "Supabase API Connected (${res.latencyMs}ms)"
                }
                is SupabaseConnectionState.Error -> {
                    _userMessage.value = "Supabase Test: ${res.error}"
                }
                is SupabaseConnectionState.TableNotFound -> {
                    _userMessage.value = res.message
                }
                else -> {}
            }
        }
    }

    fun setFilter(status: String) {
        _filterStatus.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectDomain(domain: DomainRequest?) {
        _selectedDomain.value = domain
        _dnsCheckResult.value = null
    }

    fun approveRequest(domainId: String, notes: String? = null) {
        viewModelScope.launch {
            repository.approveRequest(domainId, notes)
            _userMessage.value = "Request approved! DNS configuration generated."
            // Update selected domain if open
            _selectedDomain.value = _selectedDomain.value?.let {
                if (it.id == domainId) it.copy(status = "approved", dnsConfigured = true) else it
            }
        }
    }

    fun activateDomain(domainId: String) {
        viewModelScope.launch {
            repository.activateDomain(domainId)
            _userMessage.value = "Domain activated! Site is live on GitHub Pages."
            _selectedDomain.value = _selectedDomain.value?.let {
                if (it.id == domainId) it.copy(status = "active", sslStatus = "active") else it
            }
        }
    }

    fun rejectRequest(domainId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectRequest(domainId, reason)
            _userMessage.value = "Request rejected."
            _selectedDomain.value = _selectedDomain.value?.let {
                if (it.id == domainId) it.copy(status = "rejected", adminNotes = reason) else it
            }
        }
    }

    fun suspendDomain(domainId: String, reason: String = "Suspended by admin") {
        viewModelScope.launch {
            repository.suspendDomain(domainId, reason)
            _userMessage.value = "Domain suspended."
            _selectedDomain.value = _selectedDomain.value?.let {
                if (it.id == domainId) it.copy(status = "suspended") else it
            }
        }
    }

    fun deleteDomain(domainId: String) {
        viewModelScope.launch {
            repository.deleteRequest(domainId)
            _userMessage.value = "Domain request deleted."
            if (_selectedDomain.value?.id == domainId) {
                _selectedDomain.value = null
            }
        }
    }

    fun submitNewRequest(
        domainName: String,
        githubUsername: String,
        githubRepo: String,
        clientName: String,
        clientEmail: String,
        notes: String?
    ) {
        viewModelScope.launch {
            val req = repository.createNewRequest(
                domainName = domainName,
                githubUsername = githubUsername,
                githubRepo = githubRepo,
                clientName = clientName,
                clientEmail = clientEmail,
                notes = notes
            )
            _userMessage.value = "Domain request for '${req.domainName}' submitted!"
            _showNewRequestDialog.value = false
        }
    }

    fun verifyDns(domain: DomainRequest) {
        viewModelScope.launch {
            _dnsChecking.value = true
            val result = repository.verifyDns(domain)
            _dnsCheckResult.value = result
            _dnsChecking.value = false
        }
    }

    fun seedData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val count = repository.seedSampleDataToSupabase()
            _isRefreshing.value = false
            _userMessage.value = "Synced $count sample domain records with Supabase."
        }
    }

    fun setShowNewRequestDialog(show: Boolean) {
        _showNewRequestDialog.value = show
    }

    fun setShowSqlDialog(show: Boolean) {
        _showSqlDialog.value = show
    }

    fun dismissMessage() {
        _userMessage.value = null
    }
}
