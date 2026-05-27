# 🔧 Guía de Integración - NotasRepositoryImpl

## 📋 Checklist de Implementación

### ✅ Capa de Datos (Completada)
- [x] Modelo puro `Nota`
- [x] Interfaz `NotasRepository`
- [x] Clase `NotaRealm` con @PrimaryKey
- [x] Clase `NotaSql`
- [x] Archivo `AppDatabase.sq` con queries SQL
- [x] Mappers de conversión
- [x] Interfaz `NotasDataSource`
- [x] Implementación `RealmDataSource`
- [x] Implementación `SqlDataSource`
- [x] Implementación `NotasRepositoryImpl`

### ⏳ Próximos Pasos

---

## 📦 Paso 1: Crear Factory/Inyección de Dependencias

### Opción A: Usando Hilt (Recomendado)

**Crear archivo:** `app/src/main/java/com/example/proyectomoviles/di/DataModule.kt`

```kotlin
package com.example.proyectomoviles.di

import android.app.Application
import androidx.room.Room
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.proyectomoviles.AppDatabase
import com.example.proyectomoviles.data.datasources.RealmDataSource
import com.example.proyectomoviles.data.datasources.SqlDataSource
import com.example.proyectomoviles.data.repositories.NotasRepository
import com.example.proyectomoviles.data.repositories.NotasRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideRealm(): Realm {
        val config = RealmConfiguration.Builder(schema = setOf(...))
            .build()
        return Realm.open(config)
    }
    
    @Provides
    @Singleton
    fun provideSqlDriver(app: Application): SqlDriver {
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = app,
            name = "notas.db"
        )
    }
    
    @Provides
    @Singleton
    fun provideAppDatabase(driver: SqlDriver): AppDatabase {
        return AppDatabase(driver)
    }
    
    @Provides
    @Singleton
    fun provideRealmDataSource(realm: Realm): RealmDataSource {
        return RealmDataSource(realm)
    }
    
    @Provides
    @Singleton
    fun provideSqlDataSource(database: AppDatabase): SqlDataSource {
        return SqlDataSource(database)
    }
    
    @Provides
    @Singleton
    fun provideNotasRepository(
        realmDataSource: RealmDataSource,
        sqlDataSource: SqlDataSource
    ): NotasRepository {
        return NotasRepositoryImpl(
            realmDataSource = realmDataSource,
            sqlDataSource = sqlDataSource,
            esRelacionalInicial = false  // Comienza con Realm
        )
    }
}
```

---

## 🏛️ Paso 2: Crear ViewModel

**Crear archivo:** `app/src/main/java/com/example/proyectomoviles/ui/viewmodels/NotasViewModel.kt`

```kotlin
package com.example.proyectomoviles.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectomoviles.data.models.Nota
import com.example.proyectomoviles.data.repositories.NotasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NotasViewModel"

@HiltViewModel
class NotasViewModel @Inject constructor(
    private val repository: NotasRepository
) : ViewModel() {
    
    // Exponer los flows del repositorio
    val notas = repository.notasFlow
    val esRelacional = repository.esRelacionalFlow
    
    fun guardarNota(titulo: String, contenido: String) {
        if (titulo.isBlank() || contenido.isBlank()) {
            Log.w(TAG, "guardarNota: Título o contenido vacío")
            return
        }
        
        viewModelScope.launch {
            try {
                val nota = Nota(
                    id = System.currentTimeMillis().toString(),
                    contenido = "$titulo\n$contenido"
                )
                repository.guardarNota(nota)
                Log.i(TAG, "guardarNota: Nota guardada desde ViewModel")
            } catch (e: Exception) {
                Log.e(TAG, "guardarNota: Error - ${e.message}", e)
            }
        }
    }
    
    fun cambiarMotor(esRelacional: Boolean) {
        viewModelScope.launch {
            try {
                repository.cambiarMotor(esRelacional)
                Log.i(TAG, "cambiarMotor: Motor cambiado exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "cambiarMotor: Error - ${e.message}", e)
            }
        }
    }
}
```

---

## 🎨 Paso 3: Integrar en UI Compose

**Crear archivo:** `app/src/main/java/com/example/proyectomoviles/ui/screens/NotasScreen.kt`

```kotlin
package com.example.proyectomoviles.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyectomoviles.ui.viewmodels.NotasViewModel

@Composable
fun NotasScreen(
    viewModel: NotasViewModel = hiltViewModel()
) {
    val notas by viewModel.notas.collectAsState(initial = emptyList())
    val esRelacional by viewModel.esRelacional.collectAsState(initial = false)
    
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Indicador del motor
        Text(
            text = "Motor activo: ${if (esRelacional) "SQL" else "Realm"}",
            style = MaterialTheme.typography.bodySmall
        )
        
        // Selector de motor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.cambiarMotor(false) },
                enabled = esRelacional,
                modifier = Modifier.weight(1f)
            ) {
                Text("Realm")
            }
            
            Button(
                onClick = { viewModel.cambiarMotor(true) },
                enabled = !esRelacional,
                modifier = Modifier.weight(1f)
            ) {
                Text("SQL")
            }
        }
        
        // Formulario para agregar nota
        TextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        TextField(
            value = contenido,
            onValueChange = { contenido = it },
            label = { Text("Contenido") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .heightIn(min = 100.dp)
        )
        
        Button(
            onClick = {
                viewModel.guardarNota(titulo, contenido)
                titulo = ""
                contenido = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Nota")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de notas
        Text("Notas (${notas.size}):", style = MaterialTheme.typography.titleMedium)
        
        LazyColumn {
            items(notas.size) { index ->
                val nota = notas[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = nota.contenido,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
```

---

## 🔧 Paso 4: Actualizar Dependencias en build.gradle.kts

Si usas Hilt, agrega:

```gradle
// Hilt
implementation("com.google.dagger:hilt-android:2.48")
kapt("com.google.dagger:hilt-compiler:2.48")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
```

---

## 🚀 Paso 5: Actualizar MainActivity

```kotlin
package com.example.proyectomoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyectomoviles.ui.screens.NotasScreen
import com.example.proyectomoviles.ui.theme.ProyectoMovilesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProyectoMovilesTheme {
                NotasScreen()
            }
        }
    }
}
```

---

## 📱 Paso 6: Agregar @HiltAndroidApp en Application

**Crear archivo:** `app/src/main/java/com/example/proyectomoviles/NotasApplication.kt`

```kotlin
package com.example.proyectomoviles

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotasApplication : Application()
```

**Actualizar AndroidManifest.xml:**

```xml
<application
    android:name=".NotasApplication"
    ...>
</application>
```

---

## 🧪 Paso 7: Testing

**Crear archivo:** `app/src/test/java/com/example/proyectomoviles/NotasRepositoryImplTest.kt`

```kotlin
package com.example.proyectomoviles

import com.example.proyectomoviles.data.datasources.NotasDataSource
import com.example.proyectomoviles.data.models.Nota
import com.example.proyectomoviles.data.repositories.NotasRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotasRepositoryImplTest {
    
    private val realmDataSource = mockk<NotasDataSource>()
    private val sqlDataSource = mockk<NotasDataSource>()
    
    private val repository = NotasRepositoryImpl(
        realmDataSource = realmDataSource,
        sqlDataSource = sqlDataSource,
        esRelacionalInicial = false
    )
    
    @Test
    fun guardarNota_debeGuardarEnMotorActivo() = runTest {
        val nota = Nota(id = "1", contenido = "Test")
        coEvery { realmDataSource.guardarNota(any()) } returns Unit
        
        repository.guardarNota(nota)
        
        coVerify { realmDataSource.guardarNota(nota) }
        coVerify(exactly = 0) { sqlDataSource.guardarNota(any()) }
    }
    
    @Test
    fun cambiarMotor_debeRealizarMigracion() = runTest {
        val notas = listOf(
            Nota(id = "1", contenido = "Nota 1"),
            Nota(id = "2", contenido = "Nota 2")
        )
        
        coEvery { realmDataSource.getAllNotasDirect() } returns notas
        coEvery { sqlDataSource.limpiarTodas() } returns Unit
        coEvery { sqlDataSource.guardarNota(any()) } returns Unit
        
        repository.cambiarMotor(esRelacional = true)
        
        coVerify { realmDataSource.getAllNotasDirect() }
        coVerify { sqlDataSource.limpiarTodas() }
        coVerify(exactly = 2) { sqlDataSource.guardarNota(any()) }
    }
}
```

---

## 🎯 Flujo Completo

```
1. Usuario abre la app
   ↓
2. Hilt inyecta NotasRepositoryImpl en ViewModel
   ↓
3. ViewModel expone flows del repositorio
   ↓
4. UI observa los flows
   ↓
5. Usuario guarda nota
   ↓
6. ViewModel llama repository.guardarNota()
   ↓
7. NotasRepositoryImpl evalúa motor activo (Realm)
   ↓
8. RealmDataSource guarda la nota
   ↓
9. UI se actualiza automáticamente
   ↓
10. Logs rastrean toda la operación
```

---

## ✅ Verificación Final

Antes de usar, asegúrate de:

- [ ] SQLDelight generó la clase AppDatabase
- [ ] Todas las dependencias están en build.gradle.kts
- [ ] El AndroidManifest.xml tiene los permisos necesarios
- [ ] Compiló sin errores
- [ ] Los logs aparecen en Logcat

---

## 📞 Soporte

Revisa la documentación:
- `LOGS_DOCUMENTATION.md` - Para debugging
- `SQL_DOCUMENTATION.md` - Para queries
- `ExemplosUsoRepository.kt` - Para ejemplos
- `IMPLEMENTACION_REPOSITORIO.md` - Para arquitectura

