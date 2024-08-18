// MainActivity.kt
package com.example.spor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvPasswordLabel: TextView
    private lateinit var tvFitMate: TextView

    private lateinit var dbConnection: DatabaseConnection
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvPasswordLabel = findViewById(R.id.textView2)

        dbConnection = DatabaseConnection()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                dbConnection.authenticateUser(email, password) { success ->
                    runOnUiThread {
                        if (success) {
                            successfulLogin(email)

                            val intent = Intent(this, BlankActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            showToast("E-posta adresi veya şifre yanlış!")
                        }
                    }
                }
            } else {
                showToast("Lütfen e-posta adresi ve şifre girin!")
            }
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvPasswordLabel.setOnClickListener {
            val intent = Intent(this, Password_Reset_Activity::class.java)
            startActivity(intent)
        }

        // Google SignIn Button Click Listener
        val btnSignInWithGoogle = findViewById<Button>(R.id.btnSignInWithGoogle)
        btnSignInWithGoogle.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        // Önce mevcut oturumu kapatın
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // Ardından hesap seçme ekranını başlatın
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account)

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            // You can show an error message to the user here.
            showToast("Google ile oturum açma başarısız: ${e.message}")
            Log.e("GoogleSignIn", "Google ile oturum açma başarısız", e)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            // Kullanıcının adı ve e-postasıyla UI güncelleniyor
            val userName = account.displayName ?: ""
            val userEmail = account.email ?: ""

            // Kullanıcının veritabanında daha önce kayıtlı olup olmadığını kontrol et
            dbConnection.checkUserExists(userEmail) { userExists ->
                if (userExists) {
                    // Kullanıcı veritabanında zaten var, mevcut bilgileri al
                    dbConnection.getUserDetails(userEmail) { userDetails ->
                        runOnUiThread {
                            if (userDetails != null) {
                                // Kullanıcı bilgilerini başarıyla aldık, giriş yap
                                successfulLogin(userEmail)

                                showToast("Hoş geldiniz, ${userDetails.name}")

                                // BlankActivity'e geçiş yap
                                val intent = Intent(this, BlankActivity::class.java)
                                startActivity(intent)
                                finish() // Eğer bu aktiviteden geri dönülmeyecekse bu aktiviteyi sonlandırabilirsiniz
                            } else {
                                showToast("Kullanıcı bilgileri alınamadı.")
                            }
                        }
                    }
                } else {
                    // Kullanıcı veritabanında yok, yeni kayıt yap
                    dbConnection.registerUser(
                        username = userName,
                        password = "", // Google hesabı için bir şifre gerekmiyor
                        age = "0", // Varsayılan olarak 0
                        height = "0", // Varsayılan olarak 0
                        weight = "0", // Varsayılan olarak 0
                        email = userEmail,
                        phoneNumber = "" // Varsayılan olarak boş
                    ) { success ->
                        runOnUiThread {
                            if (success) {
                                successfulLogin(userEmail)

                                showToast("Hoş geldiniz, $userName")

                                // BlankActivity'e geçiş yap
                                val intent = Intent(this, BlankActivity::class.java)
                                startActivity(intent)
                                finish() // Eğer bu aktiviteden geri dönülmeyecekse bu aktiviteyi sonlandırabilirsiniz
                            } else {
                                showToast("Veritabanına kaydedilirken bir hata oluştu.")
                            }
                        }
                    }
                }
            }
        }
    }




    private fun successfulLogin(email: String) {
        val sharedPreferences = getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("email", email)
            apply()
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideLoginViews() {
        etEmail.visibility = View.GONE
        etPassword.visibility = View.GONE
        btnLogin.visibility = View.GONE
        tvRegister.visibility = View.GONE
        tvPasswordLabel.visibility = View.GONE
    }

    private fun navigateToLogin() {
        etEmail.visibility = View.VISIBLE
        etPassword.visibility = View.VISIBLE
        btnLogin.visibility = View.VISIBLE
        tvRegister.visibility = View.VISIBLE
        tvPasswordLabel.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // Show the login views
        navigateToLogin()

        // Clear EditText fields
        etEmail.text.clear()
        etPassword.text.clear()
    }
}
