package com.anton.timexgoogle.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class ConnectivityReceiver : BroadcastReceiver() {

    private var connectivityListener: ConnectivityListener? = null

    interface ConnectivityListener {
        fun onConnectivityChanged(isConnected: Boolean)
    }

    fun setListener(listener: ConnectivityListener) {
        this.connectivityListener = listener
    }

    // Modificar isConnected para aceptar un Context
    fun isConnected(context: Context): Boolean {
        // Usar el contexto para obtener el ConnectivityManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        // Verifica si tiene la capacidad de acceder a la red
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // Asegúrate de que el contexto no sea nulo
        context?.let {
            // Llama a isConnected con el contexto proporcionado
            val isConnected = isConnected(context)
            connectivityListener?.onConnectivityChanged(isConnected)
        }
    }

    companion object {
        fun register(context: Context, receiver: ConnectivityReceiver) {
            val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(receiver, intentFilter)
        }

        fun unregister(context: Context, receiver: ConnectivityReceiver) {
            context.unregisterReceiver(receiver)
        }
    }
}