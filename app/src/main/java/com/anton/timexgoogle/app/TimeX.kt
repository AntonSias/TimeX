package com.anton.timexgoogle.app

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.anton.timexgoogle.R
import com.anton.timexgoogle.room.AppDatabase
import com.anton.timexgoogle.room.JornadaDao
import com.anton.timexgoogle.room.JornadaEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.widget.RelativeLayout
import com.bumptech.glide.Glide

class TimeX : AppCompatActivity() {

    private lateinit var totalTimeTextView: TextView
    private lateinit var restTimeTextView: TextView
    private lateinit var playPauseButton: FloatingActionButton
    private lateinit var restButton: FloatingActionButton
    private lateinit var historialButton: FloatingActionButton
    private lateinit var tvDate: TextView
    private lateinit var inicioTimeTextView: TextView
    private lateinit var finTimeTextView: TextView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var db: AppDatabase
    private lateinit var jornadaDao: JornadaDao

    private var isRunning = false
    private var isResting = false
    private var timeInSeconds = 0L
    private var restTimeInSeconds = 0L
    private var restStartTime: Long = 0L
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var isFirstRun = true

    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.time_x)
        // Inicializar la base de datos Room en una corrutina suspendida
        GlobalScope.launch {
            try {
                db = createDatabase(applicationContext)
                jornadaDao = db.jornadaDao()

                // Aquí puedes realizar otras operaciones después de la inicialización de la base de datos
                withContext(Dispatchers.Main) {
                    setupUI()
                }
            } catch (e: Exception) {
                Log.e("TimeX", "Error al inicializar la base de datos", e)
            }
        }
    }

    private fun setupUI() {
        // Inicializa los elementos de la interfaz de usuario
        totalTimeTextView = findViewById(R.id.principal_Total)
        restTimeTextView = findViewById(R.id.principal_Descanso)
        playPauseButton = findViewById(R.id.principal_btnPlayPause)
        restButton = findViewById(R.id.principal_btnRest)
        historialButton = findViewById(R.id.principal_btnHistorial)
        tvDate = findViewById(R.id.principal_Date)
        inicioTimeTextView = findViewById(R.id.principal_Inicio)
        finTimeTextView = findViewById(R.id.principal_Fin)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        restButton.isEnabled = false

        // Después de obtener los datos del usuario en TimeX
        val displayName = intent.getStringExtra("displayName")
        val email = intent.getStringExtra("email")
        val photoUrl = intent.getStringExtra("photoUrl")
        val loginMethod = intent.getStringExtra("loginMethod")

        val deviceModel = intent.getStringExtra("deviceModel")
        val androidVersion = intent.getStringExtra("androidVersion")
        val hardwareModel = intent.getStringExtra("hardwareModel")
        val sdkVersion = intent.getStringExtra("sdkVersion")

        // Agregar un Log para imprimir la URL de la foto de perfil
        Log.i("anton", "URL de la foto de perfil: $photoUrl")



        // Fecha
        val zoneId = ZoneId.of("Europe/Madrid")
        Log.i("anton", "Zona horaria del sistema: $zoneId")// Define la zona horaria del sistema
        val currentDate = LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy"))
        tvDate.text = currentDate

        playPauseButton.setOnClickListener {
            if (isRunning) {
                // Detiene el temporizador
                stopTotalTimer()
                // Deshabilita btnRest
                restButton.isEnabled = false

                // Cuando se detiene el temporizador principal, actualiza `finTimeTextView` con la hora actual
                val now = LocalDateTime.now()
                val formattedTime = now.format(timeFormatter)
                finTimeTextView.text = formattedTime
            } else {
                // Restablece los contadores a cero
                timeInSeconds = 0
                restTimeInSeconds = 0
                totalTimeTextView.text = "00:00:00"
                restTimeTextView.text = "00:00:00"

                // Inicia el temporizador
                startTotalTimer()
                // Habilita btnRest porque el temporizador principal está en ejecución
                restButton.isEnabled = true

                // Limpia `finTimeTextView` cuando comienza una nueva jornada
                finTimeTextView.text = "--:--"
            }
        }

        // Configurar el botón rest
        restButton.setOnClickListener {
            if (isResting) {
                stopRestTimer()
                startTotalTimer()
            } else {
                if (isRunning) {
                    stopTotalTimer()
                }
                startRestTimer()
            }
        }

        historialButton.setOnClickListener{
            val intent = Intent(this, Historial::class.java)
            startActivity(intent)
        }


        // Verificar la conexión a internet y sincronizar los datos cuando se recupere la conexión
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d("TimeX", "Conexión a Internet establecida")
                lifecycleScope.launch {
                    // Sincroniza los datos cuando se recupera la conexión
                    synchronizeData()
                }
                showToast("Conexión a Internet establecida")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                showToast("Conexión a Internet perdida")
                Log.d("TimeX", "Conexión a Internet perdida")
            }
        })

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
                    // Ya estamos en TimeX, no hacemos nada
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

        bottomNavigationView.selectedItemId = R.id.nav_home
    }

    private suspend fun synchronizeData() {
        // Obtener las jornadas no sincronizadas
        val jornadasNoSincronizadas = withContext(Dispatchers.IO) {
            jornadaDao.getJornadasNoSincronizadas()
        }
        Log.d("TimeX", "Jornadas no sincronizadas: $jornadasNoSincronizadas")
        for (jornada in jornadasNoSincronizadas) {
            // Aquí implementa la lógica para sincronizar los datos con el servidor
            // Por ejemplo, puedes enviar los datos al servidor y actualizar el estado de sincronización en la base de datos
            showToast("Datos sincronizados correctamente")
            // Actualizar el estado de sincronización de la jornada
            withContext(Dispatchers.IO) {
                jornadaDao.updateJornadaSyncStatus(jornada.id, 1)
            }
        }
    }

    //TODO: implementar el cambio de colores en la pulsación de los botones para mayor feedback

    private fun startTotalTimer() {
        isRunning = true

        // Comprobar si estamos comenzando una nueva jornada (timeInSeconds == 0)
        if (timeInSeconds == 0L) {
            // Establecer la hora de inicio solo si es la primera vez que se inicia el temporizador de trabajo
            if (isFirstRun || timeInSeconds == 0L) {
                // Obtener la hora actual en el formato especificado
                val now = LocalDateTime.now()
                val formattedTime = now.format(timeFormatter)
                // Configurar principal_Inicio con la hora actual
                inicioTimeTextView.text = formattedTime

                // Establecer isFirstRun a false una vez que hemos configurado la hora de inicio
                isFirstRun = false

                // Limpiar principal_Fin ya que estamos comenzando una nueva jornada
                finTimeTextView.text = ""
            }
        }
        handler.post(updateTotalTimeRunnable)
        playPauseButton.setImageResource(R.drawable.icono_pausa) // Cambia el ícono del botón a pausa
        showToast("Jornada iniciada")

        // Inserta una nueva jornada en la base de datos
        GlobalScope.launch(Dispatchers.IO) {
            insertJornada(System.currentTimeMillis(), restTimeInSeconds)
        }
    }

    // Actualiza `finTimeTextView` solo cuando se detiene el temporizador principal
    private fun stopTotalTimer() {
        isRunning = false
        handler.removeCallbacks(updateTotalTimeRunnable)
        playPauseButton.setImageResource(R.drawable.icono_play) // Cambia el ícono del botón a play
        showToast("Jornada finalizada")

    }

    // No actualiza `finTimeTextView` en `startRestTimer()`
    private fun startRestTimer() {
        isResting = true
        restStartTime = System.currentTimeMillis() // Guardar el tiempo de inicio del descanso
        handler.post(updateRestTimeRunnable)
        restButton.setImageResource(R.drawable.icono_pausa) // Cambia el ícono del botón a pausa
        showToast("Descanso iniciado")
    }

    private fun stopRestTimer() {
        isResting = false
        restTimeInSeconds += calculateRestTime() // Calcular el tiempo transcurrido durante el descanso
        handler.removeCallbacks(updateRestTimeRunnable)
        restButton.setImageResource(R.drawable.icono_play) // Cambia el ícono del botón a play
        showToast("Descanso finalizado")
    }

    private fun calculateRestTime(): Long {
        // Calcular la diferencia entre el tiempo actual y el tiempo de inicio del descanso
        val currentTime = System.currentTimeMillis()
        return (currentTime - restStartTime) / 1000 // Convertir a segundos
    }

    // Función que actualiza el TextView de total con el tiempo transcurrido
    private val updateTotalTimeRunnable = object : Runnable {
        override fun run() {
            // Aumenta el tiempo en 1 segundo
            timeInSeconds++

            // Convierte el tiempo en segundos a formato 00:00:00
            val hours = timeInSeconds / 3600
            val minutes = (timeInSeconds % 3600) / 60
            val seconds = timeInSeconds % 60

            val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            totalTimeTextView.text = timeString

            // Ejecuta esta función nuevamente después de 1 segundo solo si el temporizador está en marcha
            if (isRunning) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    // Función que actualiza el TextView de descanso con el tiempo transcurrido
    private val updateRestTimeRunnable = object : Runnable {
        override fun run() {
            // Aumenta el tiempo en 1 segundo
            restTimeInSeconds++

            // Convierte el tiempo en segundos a formato 00:00:00
            val hours = restTimeInSeconds / 3600
            val minutes = (restTimeInSeconds % 3600) / 60
            val seconds = restTimeInSeconds % 60

            val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            restTimeTextView.text = timeString

            // Ejecuta esta función nuevamente después de 1 segundo solo si el temporizador está en marcha
            if (isResting) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    // Función para mostrar un Toast
    private fun showToast(message: String) {
        // Implementa la lógica para mostrar un Toast aquí
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Función para crear la base de datos dentro de una corrutina suspendida
    private suspend fun createDatabase(context: Context): AppDatabase {
        return withContext(Dispatchers.IO) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "jornada-database"
            ).build()
        }
    }
    // Función para agregar una nueva jornada a la base de datos
    private suspend fun insertJornada(startTime: Long, restTime: Long) {
        val jornada = JornadaEntity(startTime = startTime, endTime = null, restTimeInSeconds = restTime, syncStatus = 0)
        jornadaDao.insertJornada(jornada)
    }
}