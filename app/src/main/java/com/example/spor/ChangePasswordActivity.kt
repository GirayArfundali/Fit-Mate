package com.example.spor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Toast

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var resetButton: Button
    private lateinit var cancelButton: Button // Add this line for cancel button
    private val databaseConnection = DatabaseConnection()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        supportActionBar?.hide()
        newPasswordEditText = findViewById(R.id.editTextNewPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)
        resetButton = findViewById(R.id.btnReset)
        cancelButton = findViewById(R.id.btnCancel2) // Initialize cancel button

        resetButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (newPassword == confirmPassword) {
                updatePasswordInDatabase(newPassword)
                finish()
                showToast("Şifre başarıyla sıfırlandı")
            } else {
                // Handle password mismatch error
            }
        }

        cancelButton.setOnClickListener {
            // Navigate back to Password_Reset_Activity
            val intent = Intent(this, Password_Reset_Activity::class.java)
            startActivity(intent)
            finish() // Finish ChangePasswordActivity to prevent going back to it when pressing back button
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updatePasswordInDatabase(newPassword: String) {
        val userEmail = intent.getStringExtra("email") ?: "" // Intent'ten e-posta adresini al
        databaseConnection.changePassword(userEmail, newPassword) { success ->
            if (success) {
                // Password updated successfully
            } else {
                // Password update failed
            }
        }
    }
}
