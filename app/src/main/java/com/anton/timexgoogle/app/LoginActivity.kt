package com.anton.timexgoogle.app

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.anton.timexgoogle.R
import com.anton.timexgoogle.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val GOOGLE_SIGN_IN = 100
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Activity created")

        val googleButton = findViewById<Button>(R.id.login_btnGoogle)
        googleButton.setOnClickListener {
            Log.d(TAG, "onCreate: Google Sign-In button clicked")
            //Configuracion
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.your_web_client_id))
                .requestEmail()
                .build()

            val googleClient: GoogleSignInClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        val loginButton = findViewById<Button>(R.id.login_btnEntrar)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithEmail:success")
                            val user = FirebaseAuth.getInstance().currentUser
                            updateUI(user, "email")
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode = $requestCode, resultCode = $resultCode")
        if (requestCode == GOOGLE_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if (account != null) {
                    Log.d(TAG, "onActivityResult: Google Sign-In successful, account: ${account.email}")
                    // Obtener información adicional de la cuenta
                    val displayName = account.displayName
                    val email = account.email
                    val photoUrl = account.photoUrl

                    Log.i(TAG, "URL de la foto de perfil: $photoUrl")

                    val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "onActivityResult: Firebase authentication successful")
                            val user = FirebaseAuth.getInstance().currentUser
                            updateUI(user, "google")
                        } else {
                            Log.e(TAG, "onActivityResult: Firebase authentication failed", it.exception)
                            Toast.makeText(this, "Error: Algo salió mal", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Log.e(TAG, "onActivityResult: Google Sign-In account is null")
                }
            } catch (e: ApiException) {
                Log.e(TAG, "onActivityResult: Google Sign-In failed", e)
                Toast.makeText(this, "Error: Algo salió mal", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?, loginMethod: String) {
        if (user != null) {
            // Obtener información del dispositivo
            val deviceModel = Build.MODEL
            val androidVersion = Build.VERSION.RELEASE
            val hardwareModel = Build.HARDWARE
            val product = Build.PRODUCT
            val sdkVersion = Build.VERSION.SDK_INT
            val board = Build.BOARD

            val intent = Intent(this, TimeX::class.java).apply {
                putExtra("displayName", user.displayName)
                putExtra("email", user.email)
                putExtra("photoUrl", user.photoUrl.toString())
                putExtra("deviceModel", deviceModel)
                putExtra("androidVersion", androidVersion)
                putExtra("hardwareModel", hardwareModel)
                putExtra("sdkVersion", sdkVersion.toString())
                putExtra("loginMethod", loginMethod) // Añadir método de inicio de sesión
            }
            startActivity(intent)
        }
    }
}
