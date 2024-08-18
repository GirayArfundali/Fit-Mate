package com.example.spor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VerificationActivity2 : AppCompatActivity() {

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


        // Veritabanı bağlantısını oluştur
        dbConnection = DatabaseConnection()
        btnVerify.setOnClickListener {
            val enteredCode = etVerificationCode.text.toString()
            if (enteredCode == generatedCode) { // Girilen kod ile oluşturulan kod eşleşiyorsa
                // Doğrulama başarılı, şifre değiştirme ekranına geç
                val email = intent.getStringExtra("email")
                val intent = Intent(this@VerificationActivity2, ChangePasswordActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
                finish() // Doğrulama ekranını kapat
            } else {
                showToast("Girilen doğrulama kodu yanlış.")
            }
        }



        btnCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Doğrulama ekranını kapat
        }
    }


    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@VerificationActivity2, message, Toast.LENGTH_SHORT).show()
        }
    }
}
