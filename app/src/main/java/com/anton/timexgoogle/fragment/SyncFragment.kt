package com.anton.fragmentpractice.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.anton.timexgoogle.R
import com.anton.timexgoogle.room.AppDatabase
import com.anton.timexgoogle.room.JornadaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SyncFragment : Fragment() {
    private lateinit var jornadaDao: JornadaDao
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.list_view)

        // Initialize the database and DAO
        val db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java, "jornada-database"
        ).build()
        jornadaDao = db.jornadaDao()

        // Fetch the data from Room and update the ListView
        lifecycleScope.launch {
            val jornadasNoSincronizadas = withContext(Dispatchers.IO) {
                jornadaDao.getJornadasNoSincronizadas()
            }
            val dates = jornadasNoSincronizadas.map { it.startTime.toDate() }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, dates)
            listView.adapter = adapter
        }
    }

    // Extension function to convert Long to a readable date format
    private fun Long.toDate(): String {
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy")
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
        return dateTime.format(formatter)
    }
}