package com.davinder.subguard.ui.screens.backupexport

import com.davinder.subguard.ui.components.PremiumSurface
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.viewmodel.BackupExportViewModel
import com.davinder.subguard.viewmodel.CloudSyncResult
import com.davinder.subguard.viewmodel.RestoreResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupExportScreen(
    onBack: () -> Unit,
    viewModel: BackupExportViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser
    var isSyncingCloud by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }

    fun toast(message: String) =
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    // Restore: pick a .json backup and really import it
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isBusy = true
                when (val result = viewModel.restore(uri)) {
                    is RestoreResult.Success -> toast(
                        when {
                            result.added == 0 && result.skipped > 0 ->
                                "Nothing new to restore - all ${result.skipped} subscriptions already exist"
                            result.skipped > 0 ->
                                "Restored ${result.added} subscriptions (${result.skipped} already existed)"
                            else -> "Restored ${result.added} subscriptions"
                        }
                    )
                    is RestoreResult.Failure -> toast(result.message)
                }
                isBusy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & export", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AzurePrimary)
            }

            PremiumSurface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    // 1. Create JSON backup (real data, shared as a real file)
                    BackupItemRow(
                        icon = Icons.Outlined.CloudUpload,
                        title = "Create backup",
                        subtitle = "Save all your subscriptions as a JSON file",
                        onClick = {
                            if (isBusy) return@BackupItemRow
                            scope.launch {
                                isBusy = true
                                try {
                                    val file = viewModel.createBackupFile()
                                    if (file == null) {
                                        toast("No subscriptions to back up yet")
                                    } else {
                                        shareFile(context, file, "application/json", "Save backup")
                                    }
                                } catch (e: Exception) {
                                    toast("Couldn't create backup: ${e.message ?: "unknown error"}")
                                }
                                isBusy = false
                            }
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // 2. Restore backup
                    BackupItemRow(
                        icon = Icons.Outlined.Restore,
                        title = "Restore backup",
                        subtitle = "Import subscriptions from a previously saved JSON file",
                        onClick = { if (!isBusy) filePickerLauncher.launch("*/*") }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // 3. Cloud backup (real Firestore pending-writes check)
                    BackupItemRow(
                        icon = Icons.Outlined.Sync,
                        title = "Cloud backup",
                        subtitle = if (user != null) "Auto-synced to ${user.email}" else "Sign in to enable continuous cloud replication",
                        badge = if (user != null) (if (isSyncingCloud) "SYNCING..." else "ACTIVE") else "OFFLINE",
                        onClick = {
                            if (isSyncingCloud) return@BackupItemRow
                            scope.launch {
                                isSyncingCloud = true
                                when (val result = viewModel.syncCloud()) {
                                    is CloudSyncResult.Synced ->
                                        toast("All ${result.count} subscriptions are safely stored in the cloud")
                                    is CloudSyncResult.Pending ->
                                        toast("No internet right now. ${result.count} subscriptions will upload automatically when you're online")
                                    CloudSyncResult.NotSignedIn ->
                                        toast("Please sign in from Settings to activate Cloud backup")
                                    is CloudSyncResult.Failure -> toast(result.message)
                                }
                                isSyncingCloud = false
                            }
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // 4. Export CSV (real file, opens as a spreadsheet)
                    BackupItemRow(
                        icon = Icons.Outlined.Description,
                        title = "Export as CSV",
                        subtitle = "Generate spreadsheet format for Excel or Google Sheets",
                        onClick = {
                            if (isBusy) return@BackupItemRow
                            scope.launch {
                                isBusy = true
                                try {
                                    val file = viewModel.createCsvFile()
                                    if (file == null) {
                                        toast("No subscriptions to export yet")
                                    } else {
                                        shareFile(context, file, "text/csv", "Export CSV")
                                    }
                                } catch (e: Exception) {
                                    toast("Couldn't export CSV: ${e.message ?: "unknown error"}")
                                }
                                isBusy = false
                            }
                        }
                    )
                }
            }
        }
    }
}

/** Shares a real file through FileProvider (this is what WhatsApp/Drive/Gmail expect). */
private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            clipData = ClipData.newRawUri(file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't share the file: ${e.message ?: "unknown error"}", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun BackupItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = AzurePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (badge == "ACTIVE") AzurePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = badge,
                    fontWeight = FontWeight.Bold,
                    color = if (badge == "ACTIVE") AzurePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 11.sp
                )
            }
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}