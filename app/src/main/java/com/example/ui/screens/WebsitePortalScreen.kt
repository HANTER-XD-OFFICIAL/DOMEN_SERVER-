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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.example.data.supabase.SupabaseConfig
import com.example.ui.DomainViewModel
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SuccessGreen

@Composable
fun WebsitePortalScreen(
    viewModel: DomainViewModel,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Live Simulator, 1: HTML/JS Code, 2: Deployment Guide

    // Simulator form state
    var clientDomain by remember { mutableStateOf("myportfolio.dev") }
    var clientGithub by remember { mutableStateOf("alexraselchodhury") }
    var clientEmail by remember { mutableStateOf("alex@domainhost.io") }
    var clientName by remember { mutableStateOf("Alex Rasel") }
    var clientSubmitted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GitHub Pages Client Website",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Custom front-end website hosted on GitHub Pages that connects to your Supabase database.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = CyberTeal
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Portal Simulator", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Website Source Code", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Deploy Guide", fontSize = 12.sp) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Interactive Portal Simulator
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Web, contentDescription = null, tint = CyberTeal)
                                Text(
                                    text = "Client Portal Form (Supabase Connected)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This simulates the exact user experience of the custom website hosted on GitHub Pages. Submissions save into Supabase in real-time.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = clientDomain,
                                onValueChange = { clientDomain = it },
                                label = { Text("Your Custom Domain") },
                                placeholder = { Text("e.g. docs.mybrand.org") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("sim_domain_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = clientGithub,
                                onValueChange = { clientGithub = it },
                                label = { Text("GitHub Username") },
                                placeholder = { Text("e.g. octocat") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("sim_github_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it },
                                label = { Text("Your Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = clientEmail,
                                onValueChange = { clientEmail = it },
                                label = { Text("Contact Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (clientDomain.isNotBlank() && clientGithub.isNotBlank()) {
                                        viewModel.submitNewRequest(
                                            domainName = clientDomain,
                                            githubUsername = clientGithub,
                                            githubRepo = "${clientGithub.lowercase()}.github.io",
                                            clientName = clientName,
                                            clientEmail = clientEmail,
                                            notes = "Submitted via GitHub Pages Web Portal"
                                        )
                                        clientSubmitted = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("sim_submit_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit Domain to Supabase", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            if (clientSubmitted) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = SuccessGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                        Text(
                                            text = "Success! Request dispatched to Supabase 'domain_requests' table and visible in Admin Queue.",
                                            fontSize = 12.sp,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Source code view
                item {
                    Text(
                        text = "Static Website Bundle for GitHub Pages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The complete HTML/CSS/JavaScript files configured with your Supabase credentials.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(GITHUB_PAGES_INDEX_HTML))
                                onShowToast("index.html copied to clipboard!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy index.html", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(GITHUB_PAGES_APP_JS))
                                onShowToast("app.js copied to clipboard!")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy app.js", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFF090D16),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "index.html preview:",
                                fontSize = 11.sp,
                                color = CyberTeal,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = GITHUB_PAGES_INDEX_HTML.take(600) + "\n... [Full code available via Copy button]",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            2 -> {
                // Deployment Guide
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "3-Step GitHub Pages Deployment",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CyberTeal
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Step 1: Create a GitHub Repository\n" +
                                       "• Create a new repository (e.g. 'domain-hosting-portal' or 'username.github.io').\n\n" +
                                       "Step 2: Upload Portal Files\n" +
                                       "• Upload 'index.html', 'styles.css', and 'app.js' (pre-configured with Supabase URL & Anon Key).\n\n" +
                                       "Step 3: Enable GitHub Pages\n" +
                                       "• Go to repo Settings > Pages.\n" +
                                       "• Under 'Branch', select 'main' and '/ (root)', then click Save.\n" +
                                       "• Your website is immediately live at https://<username>.github.io/<repo>/!\n\n" +
                                       "Step 4: Custom Domain\n" +
                                       "• In the Custom Domain field, enter your desired domain.\n" +
                                       "• GitHub will automatically issue a Let's Encrypt SSL certificate within minutes.",
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
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

val GITHUB_PAGES_INDEX_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>ApexHost - Custom Domain Hosting on GitHub Pages</title>
  <link rel="stylesheet" href="styles.css">
  <script src="https://cdn.jsdelivr.net/npm/@supabase/supabase-js@2"></script>
</head>
<body>
  <header class="navbar">
    <div class="logo">🌐 ApexHost Cloud</div>
    <div class="nav-links">
      <a href="#request">Request Domain</a>
      <a href="#status">Check Status</a>
      <a href="#docs">DNS Guide</a>
    </div>
  </header>

  <main class="hero">
    <h1>Supercharge Your GitHub Pages with a Custom Domain</h1>
    <p>Seamless automated DNS provisioning, free SSL certificates, and zero downtime.</p>
    
    <section id="request" class="card form-card">
      <h2>Submit Custom Domain Request</h2>
      <form id="domainForm">
        <label>Custom Domain Name</label>
        <input type="text" id="domainName" placeholder="e.g. myportfolio.dev" required>
        
        <label>GitHub Username</label>
        <input type="text" id="githubUser" placeholder="e.g. alexraselchodhury" required>
        
        <label>GitHub Repository</label>
        <input type="text" id="githubRepo" placeholder="e.g. portfolio-site" required>
        
        <label>Full Name</label>
        <input type="text" id="clientName" placeholder="e.g. Alex Rasel" required>
        
        <label>Email Address</label>
        <input type="email" id="clientEmail" placeholder="e.g. alex@example.com" required>
        
        <button type="submit" id="submitBtn">Submit Domain Request</button>
      </form>
      <div id="formAlert" class="alert hidden"></div>
    </section>

    <section id="status" class="card status-card">
      <h2>Check Existing Domain Status</h2>
      <div class="search-box">
        <input type="text" id="statusDomain" placeholder="Enter your domain name...">
        <button id="checkStatusBtn">Lookup</button>
      </div>
      <div id="statusResult"></div>
    </section>
  </main>

  <script src="app.js"></script>
</body>
</html>
""".trimIndent()

val GITHUB_PAGES_APP_JS = """
// Supabase Configuration
const SUPABASE_URL = "https://yejqvregkkyhxxjblnii.supabase.co";
const SUPABASE_ANON_KEY = "sb_publishable_BSvIvMTDe0rgsZjpnWUxjw_wkWv6I7C";

const supabase = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

// Domain submission form handler
document.getElementById('domainForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const alertBox = document.getElementById('formAlert');
  const submitBtn = document.getElementById('submitBtn');
  submitBtn.disabled = true;
  submitBtn.innerText = "Submitting to Supabase...";

  const domain = document.getElementById('domainName').value.trim().toLowerCase();
  const githubUser = document.getElementById('githubUser').value.trim();
  const githubRepo = document.getElementById('githubRepo').value.trim();
  const clientName = document.getElementById('clientName').value.trim();
  const clientEmail = document.getElementById('clientEmail').value.trim();

  try {
    const { data, error } = await supabase
      .from('domain_requests')
      .insert([{
        domain_name: domain,
        github_username: githubUser,
        github_repo: githubRepo,
        client_name: clientName,
        client_email: clientEmail,
        status: 'pending',
        ssl_status: 'pending',
        cname_target: `${'$'}{githubUser.toLowerCase()}.github.io`,
        dns_verification_token: 'gh-verify-' + Math.random().toString(36).substring(2, 9),
        request_notes: 'Submitted via GitHub Pages website'
      }]);

    if (error) throw error;
    alertBox.className = "alert success";
    alertBox.innerText = `Request for ${'$'}{domain} submitted successfully! Your administrator will approve it shortly.`;
    document.getElementById('domainForm').reset();
  } catch (err) {
    alertBox.className = "alert error";
    alertBox.innerText = "Submission failed: " + err.message;
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerText = "Submit Domain Request";
  }
});
""".trimIndent()
