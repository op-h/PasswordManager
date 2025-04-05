package opsecurity.oph.passwordmanager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import opsecurity.oph.passwordmanager.data.PasswordRepository
import opsecurity.oph.passwordmanager.data.entities.Password
import opsecurity.oph.passwordmanager.ui.LoginActivity
import opsecurity.oph.passwordmanager.ui.PasswordAdapter
import android.widget.TextView
import kotlinx.coroutines.flow.firstOrNull
import androidx.appcompat.app.ActionBarDrawerToggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewPasswords: RecyclerView
    private lateinit var textViewEmpty: TextView
    private lateinit var textViewTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var repository: PasswordRepository
    private lateinit var adapter: PasswordAdapter
    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Set content view first to avoid crashes
            setContentView(R.layout.activity_main)
            
            // Initialize repository
            repository = PasswordRepository(this)
            
            // Initialize views first
            initializeViews()
            
            // Set up back press handling after views are initialized
            setupBackPressHandling()
            
            // Check authentication and proceed with setup
            checkAuthenticationAndSetup()
            
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Error initializing app")
            redirectToLogin()
        }
    }

    private fun setupBackPressHandling() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        backPressedCallback?.let {
            onBackPressedDispatcher.addCallback(this, it)
        }
    }

    private fun checkAuthenticationAndSetup() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val username = withContext(Dispatchers.IO) {
                    repository.getLoggedInUser().firstOrNull()
                }
                
                if (username == null) {
                    redirectToLogin()
                    return@launch
                }
                
                // Complete UI setup with username
                completeUISetup(username)
                
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Error checking login status")
                redirectToLogin()
            }
        }
    }

    private fun initializeViews() {
        try {
            drawerLayout = findViewById(R.id.drawer_layout)
            recyclerViewPasswords = findViewById(R.id.recyclerViewPasswords)
            textViewEmpty = findViewById(R.id.textViewEmpty)
            textViewTitle = findViewById(R.id.textViewTitle)
            navView = findViewById(R.id.nav_view)

            // Set up FAB
            findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab)?.setOnClickListener {
                showAddPasswordDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to initialize views: ${e.message}")
        }
    }

    private fun completeUISetup(username: String) {
        try {
            // Setup toolbar and navigation
            setupToolbarAndNavigation(username)
            
            // Setup RecyclerView
            setupRecyclerView()
            
            // Load passwords
            loadPasswords()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Error setting up UI")
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupToolbarAndNavigation(username: String) {
        // Set up toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up navigation drawer
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up navigation
        navView.setNavigationItemSelectedListener(this)

        // Update navigation header with username
        val navHeaderView = navView.getHeaderView(0)
        navHeaderView?.findViewById<TextView>(R.id.textViewLoggedInUser)?.text = username
    }

    private fun setupRecyclerView() {
        adapter = PasswordAdapter { password ->
            showPasswordDialog(password)
        }
        recyclerViewPasswords.layoutManager = LinearLayoutManager(this)
        recyclerViewPasswords.adapter = adapter
    }

    private fun loadPasswords() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val passwords = withContext(Dispatchers.IO) {
                    repository.getAllPasswords()
                }
                adapter.submitList(passwords)
                updateEmptyState(passwords.isEmpty())
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Error loading passwords")
                updateEmptyState(true)
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        recyclerViewPasswords.visibility = if (isEmpty) View.GONE else View.VISIBLE
        textViewEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                loadPasswords()
            }
            R.id.nav_signout -> {
                showSignOutConfirmationDialog()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showSignOutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sign_out)
            .setMessage(R.string.sign_out_confirmation)
            .setPositiveButton(R.string.sign_out) { _, _ ->
                signOut()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun signOut() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    repository.clearLoggedInUser()
                }
                redirectToLogin()
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Error signing out")
            }
        }
    }

    private fun showAddPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_password, null)
        val editTextTitle = dialogView.findViewById<TextInputEditText>(R.id.editTextTitle)
        val editTextUsername = dialogView.findViewById<TextInputEditText>(R.id.editTextUsername)
        val editTextPassword = dialogView.findViewById<TextInputEditText>(R.id.editTextPassword)
        val buttonGeneratePassword = dialogView.findViewById<MaterialButton>(R.id.buttonGeneratePassword)
        val buttonAdd = dialogView.findViewById<MaterialButton>(R.id.buttonAdd)
        val buttonCancel = dialogView.findViewById<MaterialButton>(R.id.buttonCancel)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Set up password generator
        buttonGeneratePassword.setOnClickListener {
            val generatedPassword = generateStrongPassword()
            editTextPassword.setText(generatedPassword)
        }

        // Set up add button
        buttonAdd.setOnClickListener {
            val title = editTextTitle.text?.toString()?.trim() ?: ""
            val username = editTextUsername.text?.toString()?.trim() ?: ""
            val password = editTextPassword.text?.toString()?.trim() ?: ""

            when {
                title.isEmpty() -> {
                    editTextTitle.error = "Title is required"
                }
                password.isEmpty() -> {
                    editTextPassword.error = "Password is required"
                }
                else -> {
                    addPassword(title, username, password)
                    dialog.dismiss()
                }
            }
        }

        // Set up cancel button
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun generateStrongPassword(): String {
        val length = 16
        val upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerCase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        
        val allChars = upperCase + lowerCase + numbers + specialChars
        val random = java.security.SecureRandom()
        
        // Ensure at least one of each type
        val password = StringBuilder()
        password.append(upperCase[random.nextInt(upperCase.length)])
        password.append(lowerCase[random.nextInt(lowerCase.length)])
        password.append(numbers[random.nextInt(numbers.length)])
        password.append(specialChars[random.nextInt(specialChars.length)])
        
        // Fill the rest randomly
        for (i in 0 until length - 4) {
            password.append(allChars[random.nextInt(allChars.length)])
        }
        
        // Shuffle the password
        return password.toString().toCharArray().apply { 
            for (i in indices.reversed()) {
                val j = random.nextInt(i + 1)
                val temp = this[i]
                this[i] = this[j]
                this[j] = temp
            }
        }.joinToString("")
    }

    private fun addPassword(title: String, username: String, password: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    repository.addPassword(title, username, password)
                }
                loadPasswords()
                showError("Password added successfully")
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Failed to add password")
            }
        }
    }

    private fun showPasswordDialog(password: Password) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val decryptedPassword = withContext(Dispatchers.IO) {
                    repository.decryptPassword(password)
                }
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(password.title)
                    .setMessage("Password: $decryptedPassword")
                    .setPositiveButton(R.string.ok, null)
                    .setNeutralButton(R.string.copy) { _, _ ->
                        copyToClipboard(decryptedPassword)
                    }
                    .setNegativeButton(R.string.delete) { _, _ ->
                        deletePassword(password)
                    }
                    .show()
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Error decrypting password")
            }
        }
    }

    private fun copyToClipboard(text: String) {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("password", text)
            clipboard.setPrimaryClip(clip)
            showError("Copied to clipboard")
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Error copying to clipboard")
        }
    }

    private fun deletePassword(password: Password) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                withContext(Dispatchers.IO) {
                    repository.deletePassword(password)
                }
                loadPasswords()
                Snackbar.make(
                    findViewById(R.id.fab),
                    R.string.password_deleted,
                    Snackbar.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Error deleting password")
            }
        }
    }
}