package com.example.proyectomoviles.data.repositories

import com.example.proyectomoviles.data.datasources.RealmDataSource
import com.example.proyectomoviles.data.datasources.SqlDataSource
import com.example.proyectomoviles.data.models.Nota
// Asegúrate de importar tu modelo Realm (ajusta la ruta si es diferente en tu proyecto)
import com.example.proyectomoviles.data.models.NotaRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

/**
 * Ejemplos de uso de NotasRepositoryImpl
 *
 * NOTA: Este archivo es solo documentación.
 * En una aplicación real, la inicialización se haría en una clase
 * Factory o un contenedor de inyección de dependencias (Hilt, Koin, etc.).
 */

/**
 * Ejemplo 1: Inicializar el repositorio comenzando con Realm
 */
suspend fun ejemplo1_inicializarConRealm() {
    // 1. Crear una configuración de Realm pasándole tu modelo de base de datos
    val config = RealmConfiguration.Builder(schema = setOf(NotaRealm::class))
        .name("ejemplo_db.realm")
        .build()

    // 2. Abrir Realm con esa configuración
    val realm = Realm.open(config)
    val realmDataSource = RealmDataSource(realm)

    // NOTA: Comentado porque 'database' requiere el contexto de Android para existir.
    // val sqlDataSource = SqlDataSource(database)

    // Crear el repositorio iniciando con Realm (esRelacionalInicial = false)
    /*
    val repository = NotasRepositoryImpl(
        realmDataSource = realmDataSource,
        sqlDataSource = sqlDataSource,
        esRelacionalInicial = false
    )
    */

    // Logs esperados:
    // I/NotasRepositoryImpl: NotasRepositoryImpl inicializado con motor: Realm
}

/**
 * Ejemplo 2: Guardar una nota en el motor activo
 */
suspend fun ejemplo2_guardarNota(repository: NotasRepository) {
    val nota = Nota(
        id = "nota-001",
        contenido = "Este es mi contenido de prueba"
    )

    // Guardar la nota
    repository.guardarNota(nota)

    // Logs esperados:
    // I/NotasRepositoryImpl: guardarNota: Iniciando guardado en motor activo: Realm
    // D/NotasRepositoryImpl: guardarNota: Validaciones pasadas, guardando en Realm
    // I/RealmDataSource: Guardando nota en Realm: id=nota-001, contenido=Este es mi contenido...
    // D/RealmDataSource: Insertando nueva nota: id=nota-001
    // I/RealmDataSource: Nota guardada exitosamente en Realm: id=nota-001
    // I/NotasRepositoryImpl: guardarNota: Nota guardada exitosamente en Realm - id=nota-001
}

/**
 * Ejemplo 3: Guardar múltiples notas
 */
suspend fun ejemplo3_guardarMultiplesNotas(repository: NotasRepository) {
    val notas = listOf(
        Nota(id = "nota-001", contenido = "Primera nota"),
        Nota(id = "nota-002", contenido = "Segunda nota"),
        Nota(id = "nota-003", contenido = "Tercera nota")
    )

    for (nota in notas) {
        repository.guardarNota(nota)
    }

    // Cada guardarNota genera logs de INFO
}

/**
 * Ejemplo 4: Cambiar de motor (Realm a SQL)
 */
suspend fun ejemplo4_cambiarDeMotor(repository: NotasRepositoryImpl) {
    // El repositorio actualmente usa Realm
    println(repository.getEstadoActual())
    // Salida: Motor activo: Realm, esRelacional: false

    // Cambiar a SQL
    repository.cambiarMotor(esRelacional = true)

    println(repository.getEstadoActual())
    // Salida: Motor activo: SQL, esRelacional: true

    // Logs esperados:
    // I/NotasRepositoryImpl: cambiarMotor: Iniciando cambio de motor de Realm a SQL
    // D/NotasRepositoryImpl: cambiarMotor: Obteniendo todas las notas del motor anterior (Realm)
    // D/RealmDataSource: Obteniendo todas las notas de Realm de forma directa
    // I/NotasRepositoryImpl: cambiarMotor: X notas recuperadas del motor anterior
    // ... (migración)
    // I/NotasRepositoryImpl: cambiarMotor: Migración completada exitosamente. X notas migradas de Realm a SQL
}

/**
 * Ejemplo 5: Cambiar de SQL a Realm
 */
suspend fun ejemplo5_cambiarDeMotorInverso(repository: NotasRepositoryImpl) {
    // El repositorio actualmente usa SQL
    println(repository.getEstadoActual())
    // Salida: Motor activo: SQL, esRelacional: true

    // Cambiar a Realm
    repository.cambiarMotor(esRelacional = false)

    println(repository.getEstadoActual())
    // Salida: Motor activo: Realm, esRelacional: false

    // Las notas se migran automáticamente
}

/**
 * Ejemplo 6: Guardar sin afectar el otro motor
 */
suspend fun ejemplo6_aislamientoDeBD(repository: NotasRepositoryImpl) {
    // Supongamos que tenemos datos en ambos motores
    // Realm: nota-001, nota-002
    // SQL: nota-101, nota-102

    // El repositorio está usando Realm
    repository.cambiarMotor(esRelacional = false)

    // Guardar en Realm
    repository.guardarNota(Nota(id = "nota-003", contenido = "Nueva en Realm"))

    println("Notas en Realm actual:")
    repository.getAllNotas().forEach { println("  - ${it.id}: ${it.contenido}") }
    // Salida:
    // Notas en Realm actual:
    //   - nota-001: ...
    //   - nota-002: ...
    //   - nota-003: Nueva en Realm

    // Cambiar a SQL
    repository.cambiarMotor(esRelacional = true)

    println("Notas en SQL actual:")
    repository.getAllNotas().forEach { println("  - ${it.id}: ${it.contenido}") }
    // Salida:
    // Notas en SQL actual:
    //   - nota-101: ...
    //   - nota-102: ...
    //   - nota-103: Nueva en SQL

    // ✓ Las notas guardadas en Realm NO aparecen en SQL
    // ✓ Las notas de SQL NO fueron borradas al cambiar
}

/**
 * Ejemplo 7: Manejo de errores
 */
suspend fun ejemplo7_manejoDeErrores(repository: NotasRepository) {
    try {
        // Intenta guardar nota con ID vacío
        repository.guardarNota(Nota(id = "", contenido = "Contenido"))
    } catch (e: IllegalArgumentException) {
        // E/NotasRepositoryImpl: guardarNota: ID de nota vacío, operación rechazada
        println("Error capturado: ${e.message}")
    }

    try {
        // Intenta guardar nota con contenido vacío
        repository.guardarNota(Nota(id = "nota-1", contenido = ""))
    } catch (e: IllegalArgumentException) {
        // E/NotasRepositoryImpl: guardarNota: Contenido de nota vacío, operación rechazada
        println("Error capturado: ${e.message}")
    }
}

/**
 * Ejemplo 8: Observar cambios reactivos
 */
fun ejemplo8_observarCambios(repository: NotasRepository) {
    // El repositorio expone observables reactivos
    repository.notasFlow  // Flow<List<Nota>> - emite cuando cambian las notas
    repository.esRelacionalFlow  // Flow<Boolean> - emite cuando cambia el motor

    // Uso en Compose:
    // val notas by repository.notasFlow.collectAsState(initial = emptyList())
    // val esRelacional by repository.esRelacionalFlow.collectAsState(initial = false)
}

/**
 * Ejemplo 9: Obtener información de debugging
 */
suspend fun ejemplo9_debugging(repository: NotasRepositoryImpl) {
    // Obtener el estado actual
    println(repository.getEstadoActual())
    // Salida: Motor activo: SQL, esRelacional: true

    // Obtener todas las notas
    val notas = repository.getAllNotas()
    println("Total de notas: ${notas.size}")

    // Obtener una nota específica
    val nota = repository.getNotaById("nota-001")
    if (nota != null) {
        println("Nota encontrada: ${nota.id} - ${nota.contenido}")
    } else {
        println("Nota no encontrada")
    }

    // Eliminar una nota
    repository.eliminarNota("nota-001")
}

/**
 * Ejemplo 10: Escenario completo
 */
suspend fun ejemplo10_escenarioCompleto(repository: NotasRepositoryImpl) {
    println("=== ESCENARIO COMPLETO ===")

    // 1. Iniciar con Realm
    println("\n1. Estado inicial:")
    println(repository.getEstadoActual())

    // 2. Guardar notas en Realm
    println("\n2. Guardando 3 notas en Realm...")
    for (i in 1..3) {
        repository.guardarNota(Nota(id = "realm-$i", contenido = "Nota Realm $i"))
    }
    println("Notas guardadas: ${repository.getAllNotas().size}")

    // 3. Cambiar a SQL
    println("\n3. Cambiando a SQL...")
    repository.cambiarMotor(esRelacional = true)
    println("Notas migraron a SQL: ${repository.getAllNotas().size}")

    // 4. Agregar más notas en SQL
    println("\n4. Agregando 2 notas más en SQL...")
    repository.guardarNota(Nota(id = "sql-1", contenido = "Nota SQL 1"))
    repository.guardarNota(Nota(id = "sql-2", contenido = "Nota SQL 2"))
    println("Total en SQL: ${repository.getAllNotas().size}")

    // 5. Cambiar de nuevo a Realm
    println("\n5. Volviendo a Realm...")
    repository.cambiarMotor(esRelacional = false)
    println("Notas en Realm ahora: ${repository.getAllNotas().size}")

    // 6. Las notas SQL se migraron, pero las nuevas SQL no están aquí
    println("\n6. Observación final:")
    repository.getAllNotas().forEach {
        println("  - ${it.id}: ${it.contenido}")
    }
}