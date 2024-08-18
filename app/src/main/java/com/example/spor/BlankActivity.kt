package com.example.spor

import AdminFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import android.view.View
import android.view.ViewTreeObserver

class BlankActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var dbConnection: DatabaseConnection
    private val SHOW_NAV_DELAY = 0L // Gösterme gecikme süresi (ms)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
        dbConnection = DatabaseConnection()
        supportActionBar?.hide()

        drawerLayout = findViewById(R.id.drawer_layout)
        bottomNav = findViewById(R.id.bottom_navigation)

        // Bottom navigation item tıklamalarını dinle
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_home -> {
                    val homeFragment = HomeFragment()
                    openFragment(homeFragment)
                    true
                }

                R.id.action_antrenman -> {
                    val antrenmanFragment = AntrenmanFragment()
                    openFragment(antrenmanFragment)
                    true
                }

                R.id.action_BMI -> {
                    val BmıFragment = BmıFragment()
                    openFragment(BmıFragment)
                    true
                }

                R.id.action_İstatistik -> {
                    val IstatistikFragment = IstatistikFragment()
                    openFragment(IstatistikFragment)
                    true
                }

                R.id.action_admin -> {
                    // action_admin tıklandığında AdminFragment'ı başlat
                    val sharedPreferences = getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
                    val username = sharedPreferences.getString("email", "") ?: ""
                    if (username.isNotEmpty()) {
                        dbConnection.getUserDetails(username) { userDetails ->
                            runOnUiThread {
                                if (userDetails != null) {
                                    val fragment = AdminFragment().apply {
                                        arguments = Bundle().apply {
                                            putParcelable("userDetails", userDetails)
                                        }
                                    }
                                    supportFragmentManager.beginTransaction().apply {
                                        replace(R.id.fragment_container, fragment)
                                        addToBackStack(null)
                                        commit()
                                    }
                                } else {
                                    // Kullanıcı detayları alınamadıysa yapılacak işlemler
                                }
                            }
                        }
                    } else {
                        // Kullanıcı adı boşsa yapılacak işlemler
                    }
                    true
                }
                else -> false
            }
        }

        // Open HomeFragment initially
        val homeFragment = HomeFragment()
        openFragment(homeFragment)
        bottomNav.selectedItemId = R.id.action_home

        // Ana layoutun konumunu izlemek için bir ViewTreeObserver ekleyin
        findViewById<View>(android.R.id.content).viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = findViewById<View>(android.R.id.content).rootView.height - findViewById<View>(android.R.id.content).height
            if (heightDiff > dpToPx(this@BlankActivity, 200)) { // Klavye açıldıysa
                // Klavye açıkken BottomNavigationView'ı gizle
                bottomNav.visibility = View.GONE
            } else {
                // Klavye kapalıysa veya açılma durumu belirsizse BottomNavigationView'ı gizle
                Handler().postDelayed({
                    bottomNav.visibility = View.VISIBLE
                }, SHOW_NAV_DELAY)
            }
        }
    }

    // dp'yi piksele çevirmek için yardımcı fonksiyon
    fun dpToPx(activity: AppCompatActivity, dp: Int): Int {
        val density = activity.resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

