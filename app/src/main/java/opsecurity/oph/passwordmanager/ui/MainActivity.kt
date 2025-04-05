package opsecurity.oph.passwordmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import opsecurity.oph.passwordmanager.R
import opsecurity.oph.passwordmanager.data.PasswordRepository

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewPasswords: RecyclerView
    private lateinit var textViewEmpty: TextView
    private lateinit var textViewTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var repository: PasswordRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Set up drawer layout
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Initialize repository
        repository = PasswordRepository(this)

        // Initialize views
        recyclerViewPasswords = findViewById(R.id.recyclerViewPasswords)
        textViewEmpty = findViewById(R.id.textViewEmpty)
        textViewTitle = findViewById(R.id.textViewTitle)
        navView = findViewById(R.id.nav_view)
        
        // Get logged-in username
        lifecycleScope.launch {
            val username = repository.getLoggedInUser().firstOrNull()
            val navHeaderView = navView.getHeaderView(0)
            val textViewLoggedInUser = navHeaderView.findViewById<TextView>(R.id.textViewLoggedInUser)
            textViewLoggedInUser.text = username
        }
        
        // Set navigation item selected listener
        navView.setNavigationItemSelectedListener(this)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Add functionality to update title based on navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_home -> textViewTitle.text = getString(R.string.menu_home)
                R.id.nav_gallery -> textViewTitle.text = getString(R.string.menu_gallery)
                R.id.nav_slideshow -> textViewTitle.text = getString(R.string.menu_slideshow)
                R.id.nav_signout -> textViewTitle.text = getString(R.string.sign_out)
                else -> textViewTitle.text = ""
            }
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_signout
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onBackPressed() {
        // Close drawer on back press if it's open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(item.itemId)
                textViewTitle.text = getString(R.string.menu_home)
            }
            R.id.nav_signout -> {
                showSignOutConfirmationDialog()
                textViewTitle.text = getString(R.string.sign_out)
            }
        }
        
        // Close the drawer after selection
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    private fun showSignOutConfirmationDialog() {
        MaterialAlertDialogBuilder(this, R.style.Theme_PasswordManager_Dialog)
            .setTitle(R.string.sign_out)
            .setMessage(R.string.sign_out_confirmation)
            .setPositiveButton(R.string.sign_out) { _, _ ->
                signOut()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun signOut() {
        lifecycleScope.launch {
            repository.clearLoggedInUser()
            
            // Show toast message
            Toast.makeText(this@MainActivity, R.string.sign_out_success, Toast.LENGTH_SHORT).show()
            
            // Navigate to login screen
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
} 