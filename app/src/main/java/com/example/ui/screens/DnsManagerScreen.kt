package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DomainRequest
import com.example.ui.DomainViewModel
import com.example.ui.components.DnsRecordRow
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsManagerScreen(
    viewModel: DomainViewModel,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allDomains by viewModel.filteredDomains.collectAsState()
    val activeAndApproved = remember(allDomains) {
        allDomains.filter { it.status in listOf("active", "approved", "pending") }
    }

    var selectedDomainId by remember {
        mutableStateOf(activeAndApproved.firstOrNull()?.id.orEmpty())
    }

    // Keep selected domain in sync
    val currentDomain = activeAndApproved.find { it.id == selectedDomainId } ?: activeAndApproved.firstOrNull()
    val dnsRecords = remember(currentDomain) {
        currentDomain?.let { viewModel.repository.generateDnsRecords(it) } ?: emptyList()
    }

    val clipboardManager = LocalClipboardManager.current
    val dnsResult by viewModel.dnsCheckResult.collectAsState()
    val isDnsChecking by viewModel.dnsChecking.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "DNS & GitHub Pages Manager",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage CNAME, A records, and GitHub Pages apex domain routing.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (activeAndApproved.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active or approved domains yet", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Approve domain requests in the Requests tab to manage DNS here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Domain selector horizontal chips
            item {
                Text(
                    text = "Select Domain:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyberTeal
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(activeAndApproved) { domain ->
                        val isSelected = domain.id == currentDomain?.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedDomainId = domain.id },
                            label = { Text(domain.domainName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberTeal.copy(alpha = 0.2f),
                                selectedLabelColor = CyberTeal
                            )
                        )
                    }
                }
            }

            if (currentDomain != null) {
                // Active domain info card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentDomain.domainName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = CyberTeal
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Target: ${currentDomain.resolvedCnameTarget}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusBadge(status = currentDomain.status)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // GitHub Pages CNAME file block
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Repository CNAME File",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = currentDomain.cnameFileContent,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(currentDomain.cnameFileContent))
                                        onShowToast("Copied CNAME content")
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberTeal, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Live DNS test & Copy all
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.verifyDns(currentDomain) },
                                    enabled = !isDnsChecking,
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isDnsChecking) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test DNS Propagation", fontSize = 12.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        val zoneFile = buildString {
                                            dnsRecords.forEach {
                                                append("${it.host}\tIN\t${it.type}\t${it.value}\n")
                                            }
                                        }
                                        clipboardManager.setText(AnnotatedString(zoneFile))
                                        onShowToast("Zone file copied to clipboard")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy All Records", fontSize = 12.sp)
                                }
                            }

                            if (dnsResult != null && dnsResult?.domain == currentDomain.domainName) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = if (dnsResult!!.matchesGitHubIps) SuccessGreen.copy(alpha = 0.12f) else WarningAmber.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = dnsResult!!.message,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (dnsResult!!.matchesGitHubIps) SuccessGreen else WarningAmber
                                        )
                                        if (dnsResult!!.resolvedIps.isNotEmpty()) {
                                            Text(
                                                text = "IPs: ${dnsResult!!.resolvedIps.joinToString()}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // DNS records list
                item {
                    Text(
                        text = "Configured DNS Records (${dnsRecords.size})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(dnsRecords) { record ->
                    DnsRecordRow(record = record, onCopied = onShowToast)
                }

                // Registrar Instructions Guide
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CyberTeal)
                                Text(
                                    text = "GitHub Pages Custom Domain Checklist",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. In GitHub repository: Settings > Pages > Custom domain > Enter '${currentDomain.domainName}'.\n" +
                                       "2. Add a file named 'CNAME' containing '${currentDomain.cnameFileContent}' in repo root.\n" +
                                       "3. In your DNS provider (Cloudflare, Namecheap, GoDaddy): Add the records shown above.\n" +
                                       "4. Enable 'Enforce HTTPS' in GitHub Pages settings after DNS verifies.",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
