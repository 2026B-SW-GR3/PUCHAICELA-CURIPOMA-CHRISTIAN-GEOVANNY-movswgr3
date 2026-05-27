package com.example.proyectomoviles

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application que inicializa Hilt.
 * Necesaria para inyección de dependencias con Hilt.
 */
@HiltAndroidApp
class ProyectoMovilesApp : Application()

