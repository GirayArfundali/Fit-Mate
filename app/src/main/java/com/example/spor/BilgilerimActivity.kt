package com.example.spor

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BilgilerimActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etAge: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText

    private lateinit var dbConnection: DatabaseConnection
    private lateinit var btnBack: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnUpdate2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bilgilerim)
        supportActionBar?.hide()

        etUsername = findViewById(R.id.etUsername)
        etAge = findViewById(R.id.etAge)
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)

        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnUpdate2 = findViewById(R.id.btnUpdate2)

        btnBack.setOnClickListener {
            finish()
        }

        dbConnection = DatabaseConnection()

        val sharedPreferences = getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")

        if (!email.isNullOrEmpty()) {
            dbConnection.getUserDetails(email!!) { userDetails ->
                runOnUiThread {
                    if (userDetails != null) {
                        etUsername.setText(userDetails.name)
                        etAge.setText(userDetails.age.toString())
                        etHeight.setText(userDetails.height.toString())
                        etWeight.setText(userDetails.weight.toString())
                        etEmail.setText(userDetails.email)
                        etPhoneNumber.setText(userDetails.phoneNumber)
                    } else {
                        showToast("Kullanıcı bilgileri alınamadı!")
                    }
                }
            }
        } else {
            showToast("Kullanıcı bilgileri bulunamadı!")
        }

        btnUpdate.setOnClickListener {
            updateUserDetails()
        }

        btnUpdate2.setOnClickListener {
            updateUserDetails()
        }
    }

    private fun updateUserDetails() {
        val userDetails = UserDetails(
            etUsername.text.toString(),
            etAge.text.toString().toInt(),
            etHeight.text.toString().toFloat(),
            etWeight.text.toString().toFloat(),
            etEmail.text.toString(),
            etPhoneNumber.text.toString()
        )

        dbConnection.updateUserDetails(userDetails) { success ->
            runOnUiThread {
                if (success) {
                    showToast("Bilgiler başarıyla güncellendi!")
                } else {
                    showToast("Bilgiler güncellenirken hata oluştu!")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
