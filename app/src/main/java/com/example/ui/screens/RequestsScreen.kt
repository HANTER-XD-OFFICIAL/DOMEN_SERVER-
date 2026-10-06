package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DomainRequest
import com.example.ui.DomainViewModel
import com.example.ui.components.SslBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    viewModel: DomainViewModel,
    modifier: Modifier = Modifier
) {
    val filteredDomains by viewModel.filteredDomains.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filterOptions = listOf(
        "all" to "All",
        "pending" to "Pending",
        "approved" to "Approved",
        "active" to "Active",
        "rejected" to "Rejected"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by domain, user, or client...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyberTeal)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("requests_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { (key, label) ->
                    val isSelected = filterStatus == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(key) },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberTeal.copy(alpha = 0.2f),
                            selectedLabelColor = CyberTeal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Results count header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredDomains.size} Domain Request${if (filteredDomains.size != 1) "s" else ""}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Request List
            if (filteredDomains.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = CyberTeal,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No domain requests matching filter",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try switching tabs or creating a new domain request.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDomains, key = { it.id }) { domain ->
                        RequestCard(
                            domain = domain,
                            onInspect = { viewModel.selectDomain(domain) },
                            onApprove = { viewModel.approveRequest(domain.id) },
                            onActivate = { viewModel.activateDomain(domain.id) },
                            onReject = { viewModel.rejectRequest(domain.id, "Rejected by admin from queue") }
                        )
                    }
                }
            }
        }

        // Floating Action Button to submit new domain
        FloatingActionButton(
            onClick = { viewModel.setShowNewRequestDialog(true) },
            containerColor = CyberTeal,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_new_domain")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Domain")
                Text("New Domain", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RequestCard(
    domain: DomainRequest,
    onInspect: () -> Unit,
    onApprove: () -> Unit,
    onActivate: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("domain_card_${domain.id.take(6)}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Domain + Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = CyberTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = domain.domainName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(status = domain.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details
            Text(
                text = "Target: ${domain.githubUsername}/${domain.githubRepo}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Client: ${domain.clientName} (${domain.clientEmail})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!domain.requestNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${domain.requestNotes}\"",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SslBadge(sslStatus = domain.sslStatus)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (domain.status.lowercase()) {
                        "pending" -> {
                            OutlinedButton(
                                onClick = onReject,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Reject", fontSize = 11.sp)
                            }
                            Button(
                                onClick = onApprove,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve", fontSize = 11.sp, color = Color.White)
                            }
                        }
                        "approved" -> {
                            Button(
                                onClick = onActivate,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Activate", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            OutlinedButton(
                                onClick = onInspect,
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("DNS & Details", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
