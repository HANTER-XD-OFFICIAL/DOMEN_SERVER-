package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DomainRequest
import com.example.data.repository.DnsCheckResult
import com.example.data.repository.DomainRepository
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun DomainDetailDialog(
    domain: DomainRequest,
    dnsResult: DnsCheckResult?,
    isDnsChecking: Boolean,
    onDismiss: () -> Unit,
    onApprove: (String) -> Unit,
    onActivate: (String) -> Unit,
    onReject: (String, String) -> Unit,
    onSuspend: (String) -> Unit,
    onDelete: (String) -> Unit,
    onVerifyDns: () -> Unit,
    onShowToast: (String) -> Unit,
    repository: DomainRepository
) {
    val clipboardManager = LocalClipboardManager.current
    var showRejectPrompt by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    val dnsRecords = remember(domain) { repository.generateDnsRecords(domain) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .testTag("domain_detail_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = CyberTeal
                    )
                    Text(
                        text = domain.domainName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Status badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = domain.status)
                    SslBadge(sslStatus = domain.sslStatus)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Client & Repo Metadata Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${domain.clientName} (${domain.clientEmail})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            Text(
                                text = "GitHub Target: ${domain.githubUsername}/${domain.githubRepo}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Target CNAME: ${domain.resolvedCnameTarget}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberTeal
                        )
                        if (!domain.requestNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notes: ${domain.requestNotes}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CNAME File for GitHub Repository
                Text(
                    text = "GitHub Pages Repository CNAME File",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberTeal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "File: CNAME (in root of ${domain.githubRepo})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = domain.cnameFileContent,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(domain.cnameFileContent))
                            onShowToast("Copied CNAME content to clipboard")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy CNAME", tint = CyberTeal, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DNS Records to configure
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Required DNS Zone Records",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal
                    )
                    TextButton(onClick = {
                        val zoneFile = buildString {
                            dnsRecords.forEach {
                                append("${it.host}\tIN\t${it.type}\t${it.value}\n")
                            }
                        }
                        clipboardManager.setText(AnnotatedString(zoneFile))
                        onShowToast("Zone records copied to clipboard")
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Zone File", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dnsRecords.forEach { record ->
                        DnsRecordRow(record = record, onCopied = onShowToast)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DNS Propagation Checker
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live DNS Status & Propagation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = onVerifyDns,
                                enabled = !isDnsChecking,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                modifier = Modifier.height(36.dp)
                            ) {
                                if (isDnsChecking) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test DNS", fontSize = 12.sp)
                                }
                            }
                        }

                        if (dnsResult != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val isOk = dnsResult.matchesGitHubIps || dnsResult.cnameFound
                            Text(
                                text = dnsResult.message,
                                fontSize = 12.sp,
                                color = if (isOk) SuccessGreen else WarningAmber,
                                fontWeight = FontWeight.Medium
                            )
                            if (dnsResult.resolvedIps.isNotEmpty()) {
                                Text(
                                    text = "Resolved IPs: ${dnsResult.resolvedIps.joinToString(", ")}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Rejection prompt
                if (showRejectPrompt) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection reason") },
                        placeholder = { Text("e.g., Target GitHub repo not accessible or invalid domain") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRejectPrompt = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onReject(domain.id, rejectReason.ifBlank { "Request rejected by administrator" })
                                showRejectPrompt = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                        ) {
                            Text("Confirm Rejection")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (domain.status.lowercase()) {
                    "pending" -> {
                        OutlinedButton(
                            onClick = { showRejectPrompt = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject")
                        }
                        Button(
                            onClick = { onApprove(domain.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.testTag("approve_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve & Provision")
                        }
                    }
                    "approved" -> {
                        Button(
                            onClick = { onActivate(domain.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                            modifier = Modifier.testTag("activate_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activate Live Site")
                        }
                    }
                    "active" -> {
                        OutlinedButton(
                            onClick = { onSuspend(domain.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Suspend")
                        }
                    }
                    "suspended" -> {
                        Button(
                            onClick = { onActivate(domain.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal)
                        ) {
                            Text("Re-activate")
                        }
                    }
                }
            }
        },
        dismissButton = {
            IconButton(
                onClick = { onDelete(domain.id) },
                modifier = Modifier.testTag("delete_domain_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete domain request",
                    tint = DangerRed
                )
            }
        }
    )
}
