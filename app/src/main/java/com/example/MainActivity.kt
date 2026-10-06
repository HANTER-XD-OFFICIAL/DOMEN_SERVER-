package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.SupabaseConnectionState
import com.example.ui.DomainViewModel
import com.example.ui.components.DomainDetailDialog
import com.example.ui.components.NewDomainDialog
import com.example.ui.components.SqlSchemaDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DnsManagerScreen
import com.example.ui.screens.RequestsScreen
import com.example.ui.screens.SupabaseConfigScreen
import com.example.ui.screens.WebsitePortalScreen
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val domainViewModel: DomainViewModel = viewModel()
                DomainAdminApp(viewModel = domainViewModel)
            }
        }
    }
}

sealed class Screen(val index: Int, val title: String, val icon: ImageVector, val tag: String) {
    object Dashboard : Screen(0, "Dashboard", Icons.Default.Dashboard, "nav_dashboard")
    object Requests : Screen(1, "Requests", Icons.Default.ListAlt, "nav_requests")
    object DnsManager : Screen(2, "DNS", Icons.Default.Dns, "nav_dns")
    object PagesPortal : Screen(3, "Pages Site", Icons.Default.Web, "nav_portal")
    object Supabase : Screen(4, "Supabase", Icons.Default.Storage, "nav_supabase")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainAdminApp(
    viewModel: DomainViewModel
) {
    var selectedScreenIndex by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val metrics by viewModel.metrics.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val selectedDomain by viewModel.selectedDomain.collectAsState()
    val dnsResult by viewModel.dnsCheckResult.collectAsState()
    val isDnsChecking by viewModel.dnsChecking.collectAsState()
    val showNewRequestDialog by viewModel.showNewRequestDialog.collectAsState()
    val showSqlDialog by viewModel.showSqlDialog.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    // Handle user action messages
    LaunchedEffect(userMessage) {
        userMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.dismissMessage()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = CyberTeal.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "APEX",
                                color = CyberTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "DomainHost Admin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Supabase live indicator
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
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
                                text = "Supabase",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.setShowNewRequestDialog(true) },
                        modifier = Modifier.testTag("appbar_add_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Domain Request",
                            tint = CyberTeal
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.testTag("appbar_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh data",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val screens = listOf(
                    Screen.Dashboard,
                    Screen.Requests,
                    Screen.DnsManager,
                    Screen.PagesPortal,
                    Screen.Supabase
                )

                screens.forEach { screen ->
                    val isSelected = selectedScreenIndex == screen.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedScreenIndex = screen.index },
                        modifier = Modifier.testTag(screen.tag),
                        icon = {
                            if (screen == Screen.Requests && metrics.pendingCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = WarningAmber) {
                                            Text(
                                                text = "${metrics.pendingCount}",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(imageVector = screen.icon, contentDescription = screen.title)
                                }
                            } else {
                                Icon(imageVector = screen.icon, contentDescription = screen.title)
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = CyberTeal,
                            indicatorColor = CyberTeal,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedScreenIndex, label = "screen_crossfade") { screenIndex ->
                when (screenIndex) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToRequests = { selectedScreenIndex = 1 },
                        onNavigateToDns = { selectedScreenIndex = 2 },
                        onNavigateToWebsite = { selectedScreenIndex = 3 },
                        onNavigateToConfig = { selectedScreenIndex = 4 }
                    )
                    1 -> RequestsScreen(
                        viewModel = viewModel
                    )
                    2 -> DnsManagerScreen(
                        viewModel = viewModel,
                        onShowToast = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                    3 -> WebsitePortalScreen(
                        viewModel = viewModel,
                        onShowToast = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                    4 -> SupabaseConfigScreen(
                        viewModel = viewModel,
                        onShowToast = { msg ->
                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }
            }

            // Domain detail / action dialog
            selectedDomain?.let { domain ->
                DomainDetailDialog(
                    domain = domain,
                    dnsResult = dnsResult,
                    isDnsChecking = isDnsChecking,
                    onDismiss = { viewModel.selectDomain(null) },
                    onApprove = { id -> viewModel.approveRequest(id) },
                    onActivate = { id -> viewModel.activateDomain(id) },
                    onReject = { id, reason -> viewModel.rejectRequest(id, reason) },
                    onSuspend = { id -> viewModel.suspendDomain(id) },
                    onDelete = { id -> viewModel.deleteDomain(id) },
                    onVerifyDns = { viewModel.verifyDns(domain) },
                    onShowToast = { msg ->
                        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                    },
                    repository = viewModel.repository
                )
            }

            // New domain submission dialog
            if (showNewRequestDialog) {
                NewDomainDialog(
                    onDismiss = { viewModel.setShowNewRequestDialog(false) },
                    onSubmit = { domain, user, repo, client, email, notes ->
                        viewModel.submitNewRequest(domain, user, repo, client, email, notes)
                    }
                )
            }

            // SQL Schema setup dialog
            if (showSqlDialog) {
                SqlSchemaDialog(
                    onDismiss = { viewModel.setShowSqlDialog(false) },
                    onShowToast = { msg ->
                        coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
            }
        }
    }
}
