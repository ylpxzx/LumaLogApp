package com.example.lumalogapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.lumalogapp.data.LumaData
import com.example.lumalogapp.data.LumaStore
import com.example.lumalogapp.data.ThemePreference
import com.example.lumalogapp.ui.i18n.LumaStrings
import com.example.lumalogapp.ui.screens.CheckinScreen
import com.example.lumalogapp.ui.screens.DashboardScreen
import com.example.lumalogapp.ui.screens.ItemEditorScreen
import com.example.lumalogapp.ui.screens.MakeupScreen
import com.example.lumalogapp.ui.screens.SettingsScreen
import com.example.lumalogapp.ui.share.saveHabitImage
import com.example.lumalogapp.ui.theme.LumaLogAppTheme
import java.time.LocalDate

private sealed interface Screen {
    data object Dashboard : Screen
    data object Settings : Screen
    data class Checkin(val itemId: Long) : Screen
    data class Makeup(val itemId: Long) : Screen
    data class Editor(val itemId: Long?) : Screen
}

@Composable
fun LumaLogRoot() {
    val context = LocalContext.current.applicationContext
    val store = remember { LumaStore(context) }
    var data by remember { mutableStateOf(store.load()) }
    var screen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var message by remember { mutableStateOf<String?>(null) }
    val strings = remember(data.preferences.language) { LumaStrings(data.preferences.language) }
    val darkTheme = when (data.preferences.theme) {
        ThemePreference.System -> isSystemInDarkTheme()
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }

    fun updateData(next: LumaData) {
        data = next
        store.save(next)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            store.exportTo(uri, data)
            message = strings.t("exported")
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            updateData(store.importFrom(uri))
            screen = Screen.Dashboard
            message = strings.t("imported")
        }
    }

    LumaLogAppTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (val current = screen) {
                Screen.Dashboard -> DashboardScreen(
                    data = data,
                    strings = strings,
                    message = message,
                    onMessageShown = { message = null },
                    onOpenSettings = { screen = Screen.Settings },
                    onCreate = { screen = Screen.Editor(null) },
                    onOpenCheckin = { screen = Screen.Checkin(it) },
                    onOpenEdit = { screen = Screen.Editor(it) },
                )

                Screen.Settings -> SettingsScreen(
                    data = data,
                    strings = strings,
                    onBack = { screen = Screen.Dashboard },
                    onUpdatePreferences = { updateData(data.copy(preferences = it)) },
                    onCreateCategory = { name, colorTheme ->
                        updateData(store.createCategory(data, name, colorTheme))
                    },
                    onToggleCategory = { updateData(store.toggleCategoryHidden(data, it)) },
                    onDeleteCategory = { updateData(store.deleteCategory(data, it)) },
                    onRestoreItem = { updateData(store.restoreItem(data, it)) },
                    onExport = { exportLauncher.launch("lumalog-backup-${LocalDate.now()}.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                )

                is Screen.Checkin -> CheckinScreen(
                    data = data,
                    itemId = current.itemId,
                    strings = strings,
                    onBack = { screen = Screen.Dashboard },
                    onOpenMakeup = { screen = Screen.Makeup(current.itemId) },
                    onSaveShareImage = { entry -> saveHabitImage(context, entry, strings, darkTheme) },
                    onCheckin = {
                        updateData(store.checkIn(data, current.itemId))
                        message = strings.t("checked")
                    },
                )

                is Screen.Makeup -> MakeupScreen(
                    data = data,
                    itemId = current.itemId,
                    strings = strings,
                    onBack = { screen = Screen.Checkin(current.itemId) },
                    onConfirm = { dates ->
                        updateData(store.makeupCheckins(data, current.itemId, dates))
                        message = strings.t("makeupConfirmed", "count" to dates.size.toString())
                        screen = Screen.Checkin(current.itemId)
                    },
                )

                is Screen.Editor -> ItemEditorScreen(
                    data = data,
                    itemId = current.itemId,
                    strings = strings,
                    onBack = { screen = Screen.Dashboard },
                    onSave = { item ->
                        updateData(
                            if (current.itemId == null) store.createItem(data, item)
                            else store.updateItem(data, item)
                        )
                        screen = Screen.Dashboard
                    },
                    onDelete = {
                        if (current.itemId != null) {
                            updateData(store.deleteItem(data, current.itemId))
                        }
                        screen = Screen.Dashboard
                    },
                    onArchive = {
                        if (current.itemId != null) {
                            updateData(store.archiveItem(data, current.itemId))
                        }
                        screen = Screen.Dashboard
                    },
                )
            }
        }
    }
}
