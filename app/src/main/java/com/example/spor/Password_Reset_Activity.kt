package com.example.spor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.Random
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Password_Reset_Activity : AppCompatActivity() {

    private lateinit var emailEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)
        supportActionBar?.hide()
        emailEditText = findViewById(R.id.etEmail)
        val sendCodeButton: Button = findViewById(R.id.send_code_button)

        sendCodeButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {
                // Kullanıcının e-posta adresini kontrol et
                checkUserExists(email)
            } else {
                showToast("Lütfen e-posta adresinizi girin.")
            }
        }


        // Geri butonunu bul
        val backButton: Button = findViewById(R.id.back_button)

        // Geri butonuna OnClickListener ekle
        backButton.setOnClickListener {
            // Geri butonuna basıldığında main aktivitesine dön
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Eğer Password_Reset_Activity'yi geri döndükten sonra kapatmak istiyorsanız bu satırı ekleyebilirsiniz
        }
    }

    private fun checkUserExists(email: String) {
        val databaseConnection = DatabaseConnection()
        databaseConnection.checkUserExists(email) { userExists ->
            if (userExists) {
                runOnUiThread {
                    sendVerificationCode(email)
                }
            } else {
                runOnUiThread {
                    showToast("Böyle bir e-posta bulunamadı.")
                }
            }
        }
    }


    private fun sendVerificationCode(email: String) {
        val code = generateVerificationCode()
        val subject = "Doğrulama Kodu"
        val messageText = "Hesabınızı doğrulamak için kullanmanız gereken kod: $code"

        // E-posta gönderme işlemini gerçekleştir
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

                // Şifre sıfırlama sayfasından doğrulama ekranına geçildiğinde
                // Şifre sıfırlama sayfasından doğrulama ekranına geçildiğinde
                runOnUiThread {
                    val intent = Intent(this@Password_Reset_Activity, VerificationActivity2::class.java)
                    intent.putExtra("verification_code", code)
                    intent.putExtra("email", email) // Doğrulama ekranına e-posta adresini geçir
                    startActivity(intent)
                    finish() // Şifre sıfırlama ekranını kapat
                    showToast("Doğrulama Kodunuz E-postanıza  gönderildi.")
                }


// Doğrulama başarılı olduğunda şifre değiştirme sayfasına geç





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

    // showToast fonksiyonunu dışarıda tanımlayarak tekrar kullanılabilirlik sağladık.
    private fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
