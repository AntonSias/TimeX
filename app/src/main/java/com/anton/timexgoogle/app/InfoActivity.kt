package com.anton.timexgoogle.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.anton.timexgoogle.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        // Recuperar la URL de la foto de perfil del Intent
        val displayName = intent.getStringExtra("displayName")
        val email = intent.getStringExtra("email")
        val photoUrl = intent.getStringExtra("photoUrl")
        val loginMethod = intent.getStringExtra("loginMethod")

        //Info Dispositivo

        val deviceModel = intent.getStringExtra("deviceModel")
        val androidVersion = intent.getStringExtra("androidVersion")
        val hardwareModel = intent.getStringExtra("hardwareModel")
        val sdkVersion = intent.getStringExtra("sdkVersion")

        findViewById<TextView>(R.id.info_textmodelo).text = deviceModel
        findViewById<TextView>(R.id.info_textandroidModelo).text = androidVersion
        findViewById<TextView>(R.id.info_textHardwareModel).text = hardwareModel
        findViewById<TextView>(R.id.info_textSdkVersion).text = sdkVersion



        // Configurar el BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java).apply {
                        putExtra("displayName", displayName)
                        putExtra("email", email)
                        putExtra("photoUrl", photoUrl)
                        putExtra("deviceModel", deviceModel)
                        putExtra("androidVersion", androidVersion)
                        putExtra("hardwareModel", hardwareModel)
                        putExtra("sdkVersion", sdkVersion)
                        putExtra("loginMethod", loginMethod)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    true
                }
                R.id.nav_home -> {
                    val intent = Intent(this, TimeX::class.java).apply {
                        putExtra("displayName", displayName)
                        putExtra("email", email)
                        putExtra("photoUrl", photoUrl)
                        putExtra("deviceModel", deviceModel)
                        putExtra("androidVersion", androidVersion)
                        putExtra("hardwareModel", hardwareModel)
                        putExtra("sdkVersion", sdkVersion)
                        putExtra("loginMethod", loginMethod)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    true
                }
                R.id.nav_info -> {
                    // Ya estamos en InfoActivity, no hacemos nada
                    true
                }
                else -> false
            }
        }

        // Set the selected item in the BottomNavigationView to the current activity
        bottomNavigationView.selectedItemId = R.id.nav_info


    }
}