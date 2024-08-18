package com.example.spor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VerificationActivity : AppCompatActivity() {

    private lateinit var etVerificationCode: EditText
    private lateinit var btnVerify: Button
    private lateinit var btnCancel: Button
    private lateinit var dbConnection: DatabaseConnection
    private var generatedCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        etVerificationCode = findViewById(R.id.etVerificationCode)
        btnVerify = findViewById(R.id.btnVerify)
        btnCancel = findViewById(R.id.btnCancel)
        supportActionBar?.hide()

        generatedCode = intent.getStringExtra("verification_code") ?: ""
        val extras = intent.extras

        // Extras'tan gelen verileri al
        val username = extras?.getString("username")
        val password = extras?.getString("password")
        val age = extras?.getString("age")
        val height = extras?.getString("height")
        val weight = extras?.getString("weight")
        val email = extras?.getString("email")
        val phoneNumber = extras?.getString("phoneNumber")

        // Veritabanı bağlantısını oluştur
        dbConnection = DatabaseConnection()

        btnVerify.setOnClickListener {
            val enteredCode = etVerificationCode.text.toString()
            if (enteredCode == generatedCode) {
                showToast("Kayıt Başarılı!")
                // Veritabanına kayıt işlemleri
                dbConnection.registerUser(
                    username!!, password!!, age!!, height!!, weight!!, email!!, phoneNumber!!
                ) { success ->
                    if (success) {
                        showToast("Kayıt başarılı!")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Doğrulama ekranını kapat
                    } else {
                        showToast("Kayıt sırasında bir hata oluştu!")
                    }
                }
            } else {
                showToast("Doğrulama kodu yanlış!")
            }
        }

        btnCancel.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish() // Doğrulama ekranını kapat
        }
    }


    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@VerificationActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
