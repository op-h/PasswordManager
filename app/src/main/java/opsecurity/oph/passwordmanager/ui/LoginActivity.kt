package opsecurity.oph.passwordmanager.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import opsecurity.oph.passwordmanager.MainActivity
import opsecurity.oph.passwordmanager.R
import opsecurity.oph.passwordmanager.data.PasswordRepository

class LoginActivity : AppCompatActivity() {
    private lateinit var repository: PasswordRepository
    
    // Views
    private lateinit var editTextUsername: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegister = findViewById(R.id.buttonRegister)

        repository = PasswordRepository(this)

        // Check if user is already logged in
        lifecycleScope.launch {
            val username = repository.getLoggedInUser().firstOrNull()
            if (username != null) {
                startMainActivity()
            }
        }

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (repository.loginUser(username, password)) {
                    repository.saveLoggedInUser(username)
                    startMainActivity()
                } else {
                    Toast.makeText(this@LoginActivity, R.string.invalid_credentials, Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonRegister.setOnClickListener {
            startRegisterActivity()
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private fun startRegisterActivity() {
        startActivity(Intent(this, RegisterActivity::class.java))
        // Don't finish this activity so we can return to it
    }
} 