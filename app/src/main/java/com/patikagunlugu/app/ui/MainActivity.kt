package com.patikagunlugu.app.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.patikagunlugu.app.manager.SettingsManager
import com.patikagunlugu.app.utils.LocaleHelper
import androidx.lifecycle.lifecycleScope
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.ActivityMainBinding
import com.patikagunlugu.app.manager.SyncManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        // Apply saved locale BEFORE any view inflation
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        // Start Data Migration and Sync
        lifecycleScope.launch {
            val syncManager = SyncManager(this@MainActivity)
            syncManager.migrateInitialData()
            syncManager.deltaSync()
        }
    }
}
