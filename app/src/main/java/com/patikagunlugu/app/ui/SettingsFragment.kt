package com.patikagunlugu.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.FragmentSettingsBinding
import com.patikagunlugu.app.manager.ExportManager
import com.patikagunlugu.app.model.SettingsItem
import com.patikagunlugu.app.ui.adapter.SettingsAdapter
import com.patikagunlugu.app.utils.LocaleHelper
import com.patikagunlugu.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettingsList()
        setupClickListeners()
        observeSettings()
        
        binding.tvVersion.text = getString(R.string.app_version_format, "2.0")
    }

    private fun setupSettingsList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                kotlinx.coroutines.flow.combine(
                    viewModel.languageFlow,
                    viewModel.themeFlow,
                    viewModel.unsyncedCountFlow
                ) { lang, theme, unsyncedCount ->
                    Triple(lang, theme, unsyncedCount)
                }.collect { (lang, theme, unsyncedCount) ->
                    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val syncSubtitle = if (currentUser == null) {
                        getString(R.string.sync_status_offline)
                    } else if (unsyncedCount == 0) {
                        getString(R.string.sync_status_synced)
                    } else {
                        getString(R.string.sync_status_unsynced, unsyncedCount)
                    }

                    val items = listOf(
                        SettingsItem(6, getString(R.string.sync_status_title), syncSubtitle, R.drawable.ic_settings_cloud, SettingsItem.Type.REGULAR),
                        SettingsItem(7, getString(R.string.save_to_gallery_title), getString(R.string.save_to_gallery_subtitle), R.drawable.ic_settings_gallery, SettingsItem.Type.SWITCH),
                        SettingsItem(1, getString(R.string.language), if (lang == "tr") getString(R.string.lang_tr) else getString(R.string.lang_en), R.drawable.ic_settings_language, SettingsItem.Type.LANGUAGE),
                        SettingsItem(2, getString(R.string.theme), when(theme) {
                            "light" -> getString(R.string.theme_light)
                            "dark" -> getString(R.string.theme_dark)
                            else -> getString(R.string.theme_system)
                        }, R.drawable.ic_settings_theme, SettingsItem.Type.THEME),
                        SettingsItem(4, getString(R.string.about), getString(R.string.about_app), R.drawable.ic_settings_about, SettingsItem.Type.REGULAR),
                        SettingsItem(5, getString(R.string.logout), "", R.drawable.ic_settings_logout, SettingsItem.Type.REGULAR)
                    )
                    
                    val switchStates = mapOf(
                        7 to viewModel.getSaveToGalleryState()
                    )

                    binding.rvSettings.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvSettings.adapter = SettingsAdapter(
                        items = items,
                        switchStates = switchStates,
                        onSwitchChange = { item, isChecked ->
                            if (item.id == 7) {
                                viewModel.setSaveToGalleryState(isChecked)
                                val msg = if (isChecked) {
                                    if (lang == "tr") "Çekilen fotoğraflar galeriye de kaydedilecek." else "Captured photos will be saved to gallery too."
                                } else {
                                    if (lang == "tr") "Galeriye kaydetme özelliği kapatıldı." else "Save to gallery feature disabled."
                                }
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onItemClick = { item ->
                            when(item.type) {
                                SettingsItem.Type.LANGUAGE -> showLanguageDialog()
                                SettingsItem.Type.THEME -> showThemeDialog()
                                SettingsItem.Type.REGULAR -> {
                                    when(item.id) {
                                        4 -> showAboutDialog()
                                        5 -> showLogoutConfirmation()
                                        6 -> showSyncStatusDialog(unsyncedCount)
                                    }
                                }
                                else -> {}
                            }
                        }
                    )
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnDeleteAll.setOnClickListener {
            showDeleteAllConfirmation()
        }
    }

    private fun showDeleteAllConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_all_data)
            .setMessage(R.string.delete_all_confirm_1)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                showDeleteAllFinalConfirmation()
            }
            .show()
    }

    private fun showDeleteAllFinalConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.final_warning)
            .setMessage(R.string.delete_all_confirm_2)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_permanently) { _, _ ->
                viewModel.clearAllData()
                Toast.makeText(requireContext(), R.string.all_data_deleted, Toast.LENGTH_SHORT).show()
                requireActivity().recreate()
            }
            .show()
    }

    private fun observeSettings() {
        // We are already observing in setupSettingsList
    }

    private fun showLanguageDialog() {
        val languages = arrayOf(getString(R.string.lang_tr), getString(R.string.lang_en))
        val codes = arrayOf("tr", "en")
        
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language)
            .setItems(languages) { _, which ->
                val selectedLang = codes[which]
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.setLanguage(selectedLang)
                    LocaleHelper.applyLocale(requireContext(), selectedLang)
                    requireActivity().recreate()
                }
            }
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf(getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system))
        val keys = arrayOf("light", "dark", "system")
        
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.theme)
            .setItems(themes) { _, which ->
                val selectedTheme = keys[which]
                viewModel.setTheme(selectedTheme)
                applyTheme(selectedTheme)
            }
            .show()
    }

    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun exportData() {
        Toast.makeText(requireContext(), getString(R.string.preparing_data), Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            val zipFile = ExportManager.exportDataToZip(requireContext())
            withContext(Dispatchers.Main) {
                if (zipFile != null && zipFile.exists()) {
                    val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", zipFile)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/zip"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, getString(R.string.export_data)))
                } else {
                    Toast.makeText(requireContext(), getString(R.string.export_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSyncStatusDialog(unsyncedCount: Int) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val title = getString(R.string.sync_warning_title)
        val message = if (currentUser == null) {
            getString(R.string.sync_warning_offline_message)
        } else if (unsyncedCount > 0) {
            getString(R.string.sync_warning_message, unsyncedCount)
        } else {
            getString(R.string.sync_status_synced)
        }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showAboutDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.about)
            .setMessage(getString(R.string.made_with_love))
            .setPositiveButton(R.string.cancel, null)
            .show()
    }

    private fun showLogoutConfirmation() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.logout) { _, _ ->
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), com.patikagunlugu.app.ui.auth.AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
