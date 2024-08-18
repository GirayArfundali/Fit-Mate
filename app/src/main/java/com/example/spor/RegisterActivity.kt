package com.example.spor

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etAge: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnRegister: Button
    private lateinit var dbConnection: DatabaseConnection
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_register_activity)
        supportActionBar?.hide()
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etAge = findViewById(R.id.etAge)
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        btnRegister = findViewById(R.id.btnRegister)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)

        dbConnection = DatabaseConnection()

        // Telefon numarası formatlayıcıyı ekle
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) return

                // Kullanıcının girdiği telefon numarasını al
                val phoneNumber = s.toString().replace("-", "")

                // Eğer kullanıcının girdiği telefon numarası 13 karakterden fazla ise işlem yapma
                if (phoneNumber.length > 13) {
                    etPhoneNumber.removeTextChangedListener(this)
                    etPhoneNumber.setText(phoneNumber.substring(0, 13))
                    etPhoneNumber.setSelection(etPhoneNumber.text.length)
                    etPhoneNumber.addTextChangedListener(this)
                    return
                }

                // Telefon numarasını 3-3-2-2 formatına dönüştür
                val formattedPhoneNumber = StringBuilder()
                for (i in phoneNumber.indices) {
                    if (formattedPhoneNumber.length >= 13) {
                        break // 13 karakter sınırını aştığımızda döngüyü sonlandır
                    }
                    if ((i == 3 || i == 6 || i == 8 || i == 10) && formattedPhoneNumber.isNotEmpty() && formattedPhoneNumber.last() != '-') {
                        formattedPhoneNumber.append("-")
                    }
                    formattedPhoneNumber.append(phoneNumber[i])
                }

                // Telefon numarasını EditText'e yaz
                etPhoneNumber.removeTextChangedListener(this)
                etPhoneNumber.setText(formattedPhoneNumber)
                etPhoneNumber.setSelection(etPhoneNumber.text.length)
                etPhoneNumber.addTextChangedListener(this)
            }
        })

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val age = etAge.text.toString()
            val height = etHeight.text.toString()
            val weight = etWeight.text.toString()
            val email = etEmail.text.toString()
            val phoneNumber = etPhoneNumber.text.toString()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                showToast("Lütfen tüm alanları doldurun!")
            } else if (password != confirmPassword) {
                showToast("Şifreler eşleşmiyor!")
            } else {
                sendVerificationCode(username, password, age, height, weight, phoneNumber, email)
            }
        }
    }

    private fun sendVerificationCode(username: String, password: String, age: String, height: String, weight: String, phoneNumber: String, email: String) {
        val code = generateVerificationCode()
        val subject = "Doğrulama Kodu"
        val messageText = "Hesabınızı doğrulamak için kullanmanız gereken kod: $code"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val properties = Properties()
                properties["mail.smtp.host"] = "smtp.gmail.com"
                properties["mail.smtp.port"] = "587"
                properties["mail.smtp.auth"] = "true"
                properties["mail.smtp.starttls.enable"] = "true"

                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication("girayarfundal63@gmail.com", "ltja yrvo ggpx sblv")
                    }
                })

                val message = MimeMessage(session)
                message.setFrom(InternetAddress("girayarfundal63@gmail.com"))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                message.subject = subject
                message.setText(messageText)

                Transport.send(message)

                runOnUiThread {
                    showToast("Doğrulama kodu e-postası gönderildi")
                    val intent = Intent(this@RegisterActivity, VerificationActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("password", password)
                    intent.putExtra("age", age)
                    intent.putExtra("height", height)
                    intent.putExtra("weight", weight)
                    intent.putExtra("email", email)
                    intent.putExtra("phoneNumber", phoneNumber)
                    intent.putExtra("verification_code", code)
                    startActivity(intent)
                }
            } catch (e: MessagingException) {
                runOnUiThread {
                    showToast("E-posta gönderme hatası: ${e.message}")
                }
            }
        }
    }

    private fun generateVerificationCode(): String {
        val random = Random()
        val code = StringBuilder()
        for (i in 0 until 6) {
            code.append(random.nextInt(10))
        }
        return code.toString()
    }

    fun goToMainActivity(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
