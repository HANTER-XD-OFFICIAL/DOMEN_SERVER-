package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SupabaseConnectionState
import com.example.data.supabase.SupabaseConfig
import com.example.ui.DomainViewModel
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun SupabaseConfigScreen(
    viewModel: DomainViewModel,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val connectionState by viewModel.connectionState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val metrics by viewModel.metrics.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Supabase Backend Configuration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "PostgreSQL database & REST API details connecting your domain hosting system.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Live Connection Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberTeal.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().testTag("supabase_status_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
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
                                text = "BACKEND STATUS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTeal
                            )
                        }

                        Button(
                            onClick = { viewModel.testSupabase() },
                            enabled = !isRefreshing,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("test_connection_button")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black)
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ping Test", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val (statusTitle, statusDesc, statusColor) = when (val state = connectionState) {
                        is SupabaseConnectionState.Connected -> Triple(
                            "Supabase Online & Responsive",
                            "Round-trip latency: ${state.latencyMs}ms • Records synced: ${state.recordCount}",
                            SuccessGreen
                        )
                        is SupabaseConnectionState.Checking -> Triple(
                            "Testing Supabase PostgREST Endpoint...",
                            "Contacting https://yejqvregkkyhxxjblnii.supabase.co/rest/v1/",
                            WarningAmber
                        )
                        is SupabaseConnectionState.TableNotFound -> Triple(
                            "Table 'domain_requests' Pending Migration",
                            state.message,
                            WarningAmber
                        )
                        is SupabaseConnectionState.Error -> Triple(
                            "Connection Issue",
                            state.error,
                            DangerRed
                        )
                    }

                    Text(
                        text = statusTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = statusDesc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Credentials Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configured Supabase Credentials",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Project URL
                    Text(text = "Project URL", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SupabaseConfig.PROJECT_URL,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberTeal
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(SupabaseConfig.PROJECT_URL))
                                    onShowToast("Project URL copied")
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = CyberTeal, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Anon Key
                    Text(text = "Anon / Public Key", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SupabaseConfig.ANON_KEY.take(20) + "..." + SupabaseConfig.ANON_KEY.takeLast(10),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(SupabaseConfig.ANON_KEY))
                                    onShowToast("Anon key copied")
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Key", tint = CyberTeal, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Seed Data & Copy SQL
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.seedData() },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Seed Sample Data", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.setShowSqlDialog(true) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View SQL Schema", fontSize = 12.sp)
                }
            }
        }

        // SQL Schema Embedded Block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Supabase SQL Migration Script",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(SupabaseConfig.SQL_SCHEMA))
                                onShowToast("SQL Schema copied to clipboard!")
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy SQL", tint = CyberTeal, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFF090D16),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = SupabaseConfig.SQL_SCHEMA,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
