package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DomainRequest
import com.example.data.repository.SupabaseConnectionState
import com.example.data.supabase.SupabaseConfig
import com.example.ui.DomainViewModel
import com.example.ui.components.MetricCard
import com.example.ui.components.SslBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    viewModel: DomainViewModel,
    onNavigateToRequests: () -> Unit,
    onNavigateToDns: () -> Unit,
    onNavigateToWebsite: () -> Unit,
    onNavigateToConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.metrics.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val filteredDomains by viewModel.filteredDomains.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // App Branding & Supabase Status Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberTeal.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_header_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (connectionState) {
                                                is SupabaseConnectionState.Connected -> SuccessGreen
                                                is SupabaseConnectionState.Checking -> WarningAmber
                                                else -> DangerRed
                                            }
                                        )
                                )
                                Text(
                                    text = "SUPABASE BACKEND",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CyberTeal
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "yejqvregkkyhxxjblnii.supabase.co",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.refresh() },
                                modifier = Modifier.testTag("refresh_dashboard_button")
                            ) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CyberTeal)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = CyberTeal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusText = when (val state = connectionState) {
                            is SupabaseConnectionState.Connected -> "Connected (${state.latencyMs}ms) • ${state.recordCount} records"
                            is SupabaseConnectionState.Checking -> "Connecting to Supabase PostgREST..."
                            is SupabaseConnectionState.TableNotFound -> "Table setup needed (Click to copy SQL)"
                            is SupabaseConnectionState.Error -> "Endpoint ping: ${state.error.take(35)}"
                        }
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Anon Key Configured",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }

        // Action needed banner (if pending requests exist)
        if (metrics.pendingCount > 0) {
            item {
                Surface(
                    color = WarningAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRequests() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "${metrics.pendingCount} Domain Request${if (metrics.pendingCount > 1) "s" else ""} Pending",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Review and provision GitHub Pages DNS records",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToRequests,
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Review", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2x2 Metric Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "TOTAL REQUESTS",
                        value = metrics.totalRequests.toString(),
                        icon = Icons.Default.Language,
                        accentColor = ElectricBlue,
                        modifier = Modifier.weight(1f),
                        subtitle = "Supabase DB synced"
                    )
                    MetricCard(
                        title = "PENDING REVIEW",
                        value = metrics.pendingCount.toString(),
                        icon = Icons.Default.HourglassTop,
                        accentColor = WarningAmber,
                        modifier = Modifier.weight(1f),
                        subtitle = "Needs admin action",
                        onClick = onNavigateToRequests
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "ACTIVE HOSTED",
                        value = metrics.activeCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        subtitle = "Live on GitHub Pages",
                        onClick = onNavigateToDns
                    )
                    MetricCard(
                        title = "SSL ENFORCED",
                        value = metrics.sslActiveCount.toString(),
                        icon = Icons.Default.Lock,
                        accentColor = CyberTeal,
                        modifier = Modifier.weight(1f),
                        subtitle = "HTTPS certificates"
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.setShowNewRequestDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("quick_add_domain_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Domain", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToWebsite,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Web, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pages Portal", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.setShowSqlDialog(true) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SQL Schema", fontSize = 12.sp)
                }
            }
        }

        // Recent Requests Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Domain Submissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All (${filteredDomains.size})",
                    color = CyberTeal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToRequests() }
                )
            }
        }

        if (filteredDomains.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No domain requests found", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Submit a request or seed test data to test the workflow",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.seedData() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text("Populate Sample Domains", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(filteredDomains.take(4)) { domain ->
                DashboardDomainItem(
                    domain = domain,
                    onClick = { viewModel.selectDomain(domain) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DashboardDomainItem(
    domain: DomainRequest,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = domain.domainName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${domain.githubUsername}/${domain.githubRepo}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${domain.clientName} • ${domain.formattedDate}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = domain.status)
                Spacer(modifier = Modifier.height(6.dp))
                SslBadge(sslStatus = domain.sslStatus)
            }
        }
    }
}
