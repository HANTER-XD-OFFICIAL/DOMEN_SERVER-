package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberTeal

@Composable
fun NewDomainDialog(
    onDismiss: () -> Unit,
    onSubmit: (domain: String, user: String, repo: String, clientName: String, clientEmail: String, notes: String?) -> Unit
) {
    var domainName by remember { mutableStateOf("") }
    var githubUsername by remember { mutableStateOf("") }
    var githubRepo by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var domainError by remember { mutableStateOf<String?>(null) }
    var userError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        if (domainName.isBlank() || !domainName.contains(".")) {
            domainError = "Enter a valid domain name (e.g. portfolio.alex.dev)"
            isValid = false
        } else {
            domainError = null
        }

        if (githubUsername.isBlank()) {
            userError = "GitHub username is required"
            isValid = false
        } else {
            userError = null
        }

        if (clientEmail.isBlank() || !clientEmail.contains("@")) {
            emailError = "Enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }

        return isValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .testTag("new_domain_dialog"),
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
                    Icon(Icons.Default.Language, contentDescription = null, tint = CyberTeal)
                    Text(
                        text = "New Domain Request",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Provision a custom domain connected to a GitHub Pages repository. Records will sync directly to Supabase.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = domainName,
                    onValueChange = {
                        domainName = it.trim().lowercase()
                        domainError = null
                    },
                    label = { Text("Custom Domain Name *") },
                    placeholder = { Text("e.g. app.mycloud.io or myblog.com") },
                    isError = domainError != null,
                    supportingText = domainError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("domain_name_input")
                )

                OutlinedTextField(
                    value = githubUsername,
                    onValueChange = {
                        githubUsername = it.trim()
                        userError = null
                        if (githubRepo.isBlank()) {
                            githubRepo = "${it.trim().lowercase()}.github.io"
                        }
                    },
                    label = { Text("GitHub Username / Org *") },
                    placeholder = { Text("e.g. alexraselchodhury") },
                    isError = userError != null,
                    supportingText = userError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("github_username_input")
                )

                OutlinedTextField(
                    value = githubRepo,
                    onValueChange = { githubRepo = it.trim() },
                    label = { Text("Target GitHub Repository *") },
                    placeholder = { Text("e.g. username.github.io or my-docs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("github_repo_input")
                )

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client / Owner Name") },
                    placeholder = { Text("e.g. Alex Rasel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clientEmail,
                    onValueChange = {
                        clientEmail = it.trim()
                        emailError = null
                    },
                    label = { Text("Client Email *") },
                    placeholder = { Text("e.g. alex@example.com") },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Request Notes / Project Description") },
                    placeholder = { Text("e.g. Production site, requires apex A records and automatic HTTPS") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        val repoFinal = if (githubRepo.isNotBlank()) githubRepo else "${githubUsername.lowercase()}.github.io"
                        val nameFinal = if (clientName.isNotBlank()) clientName else githubUsername
                        onSubmit(
                            domainName,
                            githubUsername,
                            repoFinal,
                            nameFinal,
                            clientEmail,
                            notes.ifBlank { null }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                modifier = Modifier.testTag("submit_request_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
