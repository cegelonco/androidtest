package com.studenthousing.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.studenthousing.app.data.network.AuthEvent
import com.studenthousing.app.data.network.AuthEventBus
import com.studenthousing.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.toolbar.setupWithNavController(navController)

        // Disable back arrow on main screens (login handled by hiding toolbar)
        val noBackDestinations = setOf(
            R.id.propertiesFragment
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id in noBackDestinations) {
                binding.toolbar.navigationIcon = null
            }
        }

        // Inflate menu
        binding.toolbar.inflateMenu(R.menu.menu_main)

        // Handle menu clicks
        val app = applicationContext as StudentHousingApp
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                R.id.action_logout -> {
                    lifecycleScope.launch {
                        app.container.tokenStore.clearToken()
                    }
                    navController.navigate(
                        R.id.loginFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.nav_graph, true)
                            .build()
                    )
                    true
                }
                else -> false
            }
        }

        // Hide menu on auth screens
        val authDestinations = setOf(
            R.id.loginFragment,
            R.id.signupFragment,
            R.id.verifyEmailFragment
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuth = destination.id in authDestinations
            binding.toolbar.menu.setGroupVisible(R.id.menu_group_main, !isAuth)
            binding.toolbar.visibility = if (isAuth) View.GONE else View.VISIBLE
        }

        // Listen for 401 unauthorized events
        lifecycleScope.launch {
            AuthEventBus.events.collect { event ->
                when (event) {
                    is AuthEvent.Unauthorized -> {
                        app.container.tokenStore.clearToken()
                        navController.navigate(
                            R.id.loginFragment,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph, true)
                                .build()
                        )
                    }
                }
            }
        }
    }
}
