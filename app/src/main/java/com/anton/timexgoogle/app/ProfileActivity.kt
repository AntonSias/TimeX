package com.anton.timexgoogle.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.anton.timexgoogle.R
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        // Recuperar la URL de la foto de perfil del Intent
        val displayName = intent.getStringExtra("displayName")
        val email = intent.getStringExtra("email")
        val photoUrl = intent.getStringExtra("photoUrl")
        val loginMethod = intent.getStringExtra("loginMethod")

        val deviceModel = intent.getStringExtra("deviceModel")
        val androidVersion = intent.getStringExtra("androidVersion")
        val hardwareModel = intent.getStringExtra("hardwareModel")
        val sdkVersion = intent.getStringExtra("sdkVersion")
        val principal_btnLogOut = findViewById<FloatingActionButton>(R.id.principal_btnLogOut)

        Log.i("anton", "URL de la foto de perfil: $photoUrl")

        // Mostrar displayName, email y loginMethod en los TextView correspondientes
        findViewById<TextView>(R.id.userProfile_UserName).text = displayName
        findViewById<TextView>(R.id.userProfile_Email).text = email
        findViewById<TextView>(R.id.userProfile_LogIn).text = loginMethod

        // Cargar la imagen en CircleImageView usando Glide
        val userImage = findViewById<CircleImageView>(R.id.userImage)
        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.mipmap.ic_launcher_round) // Imagen de marcador de posición mientras se carga
            .error(R.mipmap.ic_launcher_round) // Imagen de error si no se puede cargar la imagen
            .into(userImage)

        principal_btnLogOut.setOnClickListener {
            signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_LONG).show()
        }

        // Configurar el BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    // Ya estamos en ProfileActivity, no hacemos nada
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
                    val intent = Intent(this, InfoActivity::class.java).apply {
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
                else -> false
            }
        }

        // Set the selected item in the BottomNavigationView to the current activity
        bottomNavigationView.selectedItemId = R.id.nav_profile
    }

    private fun signOut() {
        auth.signOut()
        // Redirect to login activity or any other desired activity after logout
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
