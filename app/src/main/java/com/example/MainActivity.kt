package com.example

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.InactivityCountdownDialog
import com.example.ui.components.NotificationSheet
import com.example.ui.components.OtaUpdateDialog
import com.example.ui.components.PinDialog
import com.example.ui.components.ScreenshotWatermarkBanner
import com.example.ui.components.SpreadsheetIntegrationModal
import com.example.ui.screens.ActivityLogScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LaporanBulananScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PendudukDetailScreen
import com.example.ui.screens.PendudukFormScreen
import com.example.ui.screens.PendudukListScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PusatAnalisisScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var screenCaptureCallback: Activity.ScreenCaptureCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        viewModel.onUserActivity()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val callback = Activity.ScreenCaptureCallback {
                    val accountName = viewModel.userProfile.value.namaPetugas
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
                    val timeStr = sdf.format(Date())
                    Toast.makeText(
                        this@MainActivity,
                        "📸 Screenshot: $accountName • $timeStr WIB",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.logScreenshot(
                        screenName = viewModel.currentScreen.value.javaClass.simpleName,
                        accountName = accountName,
                        timeFormatted = timeStr
                    )
                }
                screenCaptureCallback = callback
                registerScreenCaptureCallback(mainExecutor, callback)
            } catch (e: Exception) {
                // Ignore or log if screen capture detection permission is restricted by runtime environment
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                screenCaptureCallback?.let {
                    unregisterScreenCaptureCallback(it)
                }
            } catch (e: Exception) {
                // Ignore safely
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()
    val isNotificationOpen by viewModel.isNotificationSheetOpen.collectAsState()
    val showPinDialog by viewModel.showPinDialog.collectAsState()
    val showSpreadsheetModal by viewModel.showSpreadsheetModal.collectAsState()
    val showInactivityWarning by viewModel.showInactivityWarning.collectAsState()
    val inactivityCountdown by viewModel.inactivityCountdown.collectAsState()
    val screenshotWatermarkNotice by viewModel.screenshotWatermarkNotice.collectAsState()
    val showOtaDialog by viewModel.showOtaDialog.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val isDownloadingUpdate by viewModel.isDownloadingUpdate.collectAsState()
    val updateProgress by viewModel.updateProgress.collectAsState()
    val updateDownloadedBytes by viewModel.updateDownloadedBytes.collectAsState()
    val updateTotalBytes by viewModel.updateTotalBytes.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val updateError by viewModel.updateError.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    var showSyncDropdown by remember { mutableStateOf(false) }

    // Handle system back navigation (prevent exiting app unexpectedly)
    BackHandler(enabled = isAuthenticated) {
        if (showInactivityWarning) {
            viewModel.stayLoggedIn()
        } else if (showPinDialog) {
            viewModel.dismissPinDialog()
        } else if (showSpreadsheetModal) {
            viewModel.closeSpreadsheetModal()
        } else if (isNotificationOpen) {
            viewModel.closeNotificationSheet()
        } else {
            val handled = viewModel.navigateBack()
            if (!handled) {
                (context as? android.app.Activity)?.finish()
            }
        }
    }

    // If user is not yet logged in, show Login Screen
    if (!isAuthenticated) {
        LoginScreen(viewModel = viewModel)
        return
    }

    val showBottomBar = currentScreen is Screen.Dashboard ||
            currentScreen is Screen.PendudukList ||
            currentScreen is Screen.ActivityLogs ||
            currentScreen is Screen.Profile ||
            currentScreen is Screen.Settings

    val topBarTitle = when (currentScreen) {
        is Screen.Dashboard -> "SIMDes Kependudukan"
        is Screen.PendudukList -> "Tabel Data Penduduk"
        is Screen.ActivityLogs -> "Riwayat Log Aktivitas"
        is Screen.LaporanBulanan -> "Laporan Bulanan"
        is Screen.PusatAnalisis -> "Pusat Analisis & Statistik"
        is Screen.Profile -> "Profil Petugas"
        is Screen.Settings -> "Pengaturan Aplikasi"
        is Screen.PendudukDetail -> "Detail Penduduk"
        is Screen.PendudukForm -> "Formulir Penduduk"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (currentScreen !is Screen.PendudukDetail && 
                    currentScreen !is Screen.PendudukForm && 
                    currentScreen !is Screen.LaporanBulanan && 
                    currentScreen !is Screen.PusatAnalisis) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (currentScreen) {
                                            is Screen.Settings -> Icons.Default.Settings
                                            is Screen.Profile -> Icons.Default.AccountCircle
                                            is Screen.ActivityLogs -> Icons.Default.History
                                            is Screen.PendudukList -> Icons.Default.People
                                            else -> Icons.Default.Groups
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = topBarTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        },
                        actions = {
                            // Spreadsheet Sync Action Menu (Unduh Data & Kirim Pembaruan)
                            Box {
                                IconButton(
                                    onClick = {
                                        if (isSyncing) return@IconButton
                                        showSyncDropdown = true
                                    }
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(22.dp),
                                            strokeWidth = 2.5.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudSync,
                                            contentDescription = "Sinkronisasi Spreadsheet",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showSyncDropdown,
                                    onDismissRequest = { showSyncDropdown = false },
                                    modifier = Modifier.width(300.dp)
                                ) {
                                    // Option 1: Unduh Data (Tarik Spreadsheet -> Aplikasi)
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = "Unduh Data",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "Tarik data dari spreadsheet ke aplikasi",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showSyncDropdown = false
                                            if (profile.appsScriptUrl.isBlank()) {
                                                viewModel.openSpreadsheetModal()
                                            } else {
                                                viewModel.pullDataFromSpreadsheet()
                                            }
                                        }
                                    )

                                    HorizontalDivider()

                                    // Option 2: Kirim Pembaruan (Hasil Edit & Baru -> Spreadsheet)
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = "Kirim Pembaruan",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "Kirim data edit & penduduk baru ke spreadsheet",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Upload,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        },
                                        onClick = {
                                            showSyncDropdown = false
                                            if (profile.appsScriptUrl.isBlank()) {
                                                viewModel.openSpreadsheetModal()
                                            } else {
                                                viewModel.syncWithSpreadsheet()
                                            }
                                        }
                                    )
                                }
                            }

                            // Notification Bell with Unread Badge
                            IconButton(onClick = { viewModel.openNotificationSheet() }) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            ) {
                                                Text("$unreadCount", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifikasi Real-time"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Dashboard,
                            onClick = { viewModel.navigateTo(Screen.Dashboard) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Dashboard) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                    contentDescription = "Dashboard"
                                )
                            },
                            label = { Text("Dashboard") }
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.PendudukList,
                            onClick = { viewModel.navigateTo(Screen.PendudukList) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.PendudukList) Icons.Default.People else Icons.Outlined.People,
                                    contentDescription = "Penduduk"
                                )
                            },
                            label = { Text("Penduduk") }
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.ActivityLogs,
                            onClick = { viewModel.navigateTo(Screen.ActivityLogs) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.ActivityLogs) Icons.Default.History else Icons.Outlined.History,
                                    contentDescription = "Log"
                                )
                            },
                            label = { Text("Log") }
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Profile || currentScreen is Screen.Settings,
                            onClick = { viewModel.navigateTo(Screen.Profile) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Profile || currentScreen is Screen.Settings) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle,
                                    contentDescription = "Profil"
                                )
                            },
                            label = { Text("Profil") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val screen = currentScreen) {
                    is Screen.Dashboard -> DashboardScreen(viewModel = viewModel)
                    is Screen.PendudukList -> PendudukListScreen(viewModel = viewModel)
                    is Screen.ActivityLogs -> ActivityLogScreen(viewModel = viewModel)
                    is Screen.LaporanBulanan -> LaporanBulananScreen(viewModel = viewModel)
                    is Screen.PusatAnalisis -> PusatAnalisisScreen(viewModel = viewModel)
                    is Screen.Profile -> ProfileScreen(viewModel = viewModel)
                    is Screen.Settings -> SettingsScreen(viewModel = viewModel)
                    is Screen.PendudukDetail -> PendudukDetailScreen(nik = screen.nik, viewModel = viewModel)
                    is Screen.PendudukForm -> PendudukFormScreen(nikToEdit = screen.nik, initialNoKk = screen.initialNoKk, viewModel = viewModel)
                }
            }
        }

        // Watermark Banner on top (only shown when screenshot occurs)
        AnimatedVisibility(
            visible = screenshotWatermarkNotice != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            screenshotWatermarkNotice?.let { notice ->
                ScreenshotWatermarkBanner(
                    watermarkText = notice,
                    onDismiss = { viewModel.dismissScreenshotWatermark() }
                )
            }
        }
    }

    // Inactivity 10-Second Countdown Warning Dialog
    if (showInactivityWarning) {
        InactivityCountdownDialog(
            remainingSeconds = inactivityCountdown,
            onStayLoggedIn = { viewModel.stayLoggedIn() },
            onLockNow = { viewModel.lockNow() }
        )
    }

    // PIN Authentication Dialog
    if (showPinDialog) {
        PinDialog(
            onDismiss = { viewModel.dismissPinDialog() },
            onPinSubmit = { enteredPin ->
                viewModel.verifyEnteredPin(enteredPin)
            },
            errorMessage = pinError
        )
    }

    // Real-time Notification Sheet
    if (isNotificationOpen) {
        NotificationSheet(
            notifications = allNotifications,
            onDismiss = { viewModel.closeNotificationSheet() },
            onMarkAsRead = { id -> viewModel.markNotificationAsRead(id) },
            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
            onClearAll = { viewModel.clearAllNotifications() }
        )
    }

    // Google Spreadsheet & index.html Integration Sheet Modal
    if (showSpreadsheetModal) {
        SpreadsheetIntegrationModal(
            viewModel = viewModel,
            onDismiss = { viewModel.closeSpreadsheetModal() }
        )
    }

    // OTA GitHub Update Dialog
    if (showOtaDialog) {
        OtaUpdateDialog(
            updateInfo = updateInfo,
            isChecking = isCheckingUpdate,
            isDownloading = isDownloadingUpdate,
            downloadProgress = updateProgress,
            downloadedBytes = updateDownloadedBytes,
            totalBytes = updateTotalBytes,
            currentVersion = "v${viewModel.getCurrentAppVersionName()}",
            errorMessage = updateError,
            onCheckUpdate = { viewModel.checkForOtaUpdate(manual = true) },
            onStartDownloadAndInstall = { viewModel.startDownloadAndInstallUpdate() },
            onDismiss = { viewModel.dismissOtaDialog() }
        )
    }
}
