package opsecurity.oph.passwordmanager.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import opsecurity.oph.passwordmanager.R
import opsecurity.oph.passwordmanager.data.PasswordRepository

class RegisterActivity : AppCompatActivity() {
    private lateinit var repository: PasswordRepository
    
    // Views
    private lateinit var editTextUsername: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonLogin: Button
    private lateinit var buttonBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonBack = findViewById(R.id.buttonBack)

        repository = PasswordRepository(this)

        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()

            if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                Toast.makeText(this, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (repository.registerUser(username, password)) {
                    Toast.makeText(this@RegisterActivity, R.string.registration_successful, Toast.LENGTH_SHORT).show()
                    startLoginActivity()
                } else {
                    Toast.makeText(this@RegisterActivity, R.string.username_exists, Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonLogin.setOnClickListener {
            startLoginActivity()
        }
        
        buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun startLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
} 