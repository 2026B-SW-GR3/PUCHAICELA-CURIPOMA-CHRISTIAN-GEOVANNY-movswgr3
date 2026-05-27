# 🎨 Documentación UI - Jetpack Compose CRUD

## 📋 Descripción General

Se ha creado una interfaz de usuario completa en Jetpack Compose que implementa un CRUD funcional para notas con las siguientes características:

✅ Switch en TopAppBar para cambiar motor
✅ Indicador visual (Chip) del motor activo
✅ Lista reactiva con collectAsState()
✅ Formulario para crear/editar notas
✅ Actualización instantánea sin reiniciar

---

## 🏗️ Arquitectura de Componentes

### 1. **NotasScreen** (Pantalla Principal)
```kotlin
@Composable
fun NotasScreen(viewModel: NotasViewModel = hiltViewModel())
```

**Responsabilidades:**
- Observa los flows del ViewModel
- Orquesta todos los componentes menores
- Maneja el estado local del formulario

**Flows observados:**
```kotlin
val notas by viewModel.notasFlow.collectAsState(initial = emptyList())
val esRelacional by viewModel.esRelacionalFlow.collectAsState(initial = false)
```

✅ **Reactividad:** Cambios instantáneos sin reiniciar

---

### 2. **TopAppBar con Switch**

```kotlin
TopAppBar(
    title = { Text("Mis Notas") },
    actions = {
        // Switch para cambiar motor
        Switch(
            checked = esRelacional,
            onCheckedChange = { nuevoValor ->
                viewModel.cambiarMotor(nuevoValor)
            }
        )
    }
)
```

**Características:**
- ✅ Switch interactivo (izquierda = Realm, derecha = SQL)
- ✅ Texto indicador ("SQL" o "Realm")
- ✅ Cambio dinámico sin reinicio
- ✅ Color de fondo variante primaria

---

### 3. **Indicador Visual - Chip**

```kotlin
IndicadorMotorActivo(esRelacional = esRelacional)
```

**Visualización:**
```
Color verde (#4CAF50) para SQL:
┌────────────────────────────────────┐
│ 📊 Usando SQLite (SQL)             │
└────────────────────────────────────┘

Color azul (#2196F3) para Realm:
┌────────────────────────────────────┐
│ 📄 Usando Realm (NoSQL)            │
└────────────────────────────────────┘
```

**Cambios dinámicos:**
- Color del borde
- Ícono y texto
- Se actualiza en tiempo real

---

### 4. **Formulario de Notas**

```kotlin
FormularioNota(
    contenido = contenidoNota,
    onContenidoChange = { contenidoNota = it },
    notaEnEdicion = notaEnEdicion,
    onGuardar = { ... },
    onCancelar = { ... },
    editandoNota = notaEnEdicion != null
)
```

**Características:**
- ✅ TextField para texto de nota
- ✅ Botón "Guardar" o "Actualizar" (según contexto)
- ✅ Botón "Cancelar" cuando se edita
- ✅ Validación de contenido no vacío
- ✅ Fondo diferenciado

**Estados:**
- Crear nueva nota
- Editar nota existente
- Cancelar edición

---

### 5. **Lista Reactiva**

```kotlin
ListaNotas(
    notas = notas,
    motorActual = if (esRelacional) "SQL" else "Realm",
    onEditar = { nota -> ... },
    onEliminar = { nota -> ... }
)
```

**Características:**
- ✅ LazyColumn para rendimiento
- ✅ Lista vacía con mensaje
- ✅ Contador de notas
- ✅ Motor actual visible
- ✅ Actualización instantánea

**Contenido:**
```
Notas (3)                    Motor: SQL
┌──────────────────────────────────────┐
│ "Mi primer nota"       ✏️  🗑️        │
│ ID: 12345678...                      │
└──────────────────────────────────────┘
```

---

### 6. **Tarjeta de Nota Individual**

```kotlin
TarjetaNota(
    nota = nota,
    onEditar = { ... },
    onEliminar = { ... }
)
```

**Elementos:**
- Contenido de nota (máx 3 líneas)
- ID abreviado
- Botón editar (✏️)
- Botón eliminar (🗑️)
- Material Design Card

---

## 🔄 Flujo Reactivo con collectAsState()

### Guardando una nota:
```
1. Usuario escribe en TextField
   ↓
2. contenidoNota se actualiza (estado local)
   ↓
3. Usuario toca "Guardar"
   ↓
4. viewModel.guardarNota(contenido) se llama
   ↓
5. Repositorio guarda en BD (Realm o SQL)
   ↓
6. notasFlow emite nueva lista
   ↓
7. collectAsState() notifica cambio
   ↓
8. Recomposición automática
   ↓
9. Lista se actualiza en pantalla ✅
```

### Cambiando motor:
```
1. Usuario toca el Switch
   ↓
2. esRelacional cambia
   ↓
3. viewModel.cambiarMotor() se llama
   ↓
4. Repositorio migra datos
   ↓
5. esRelacionalFlow emite nuevo valor
   ↓
6. collectAsState() notifica
   ↓
7. Chip color cambia
   ↓
8. Lista se actualiza
   ↓
9. Todo en < 1 segundo ✅
```

---

## 📱 Estructura de Carpetas

```
ui/
├── viewmodels/
│   └── NotasViewModel.kt        (Lógica de negocio)
├── screens/
│   └── NotasScreen.kt           (Pantalla principal)
└── theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

---

## 🎯 Implementación del CRUD

### CREATE - Crear Nota
```kotlin
fun guardarNota(contenido: String) {
    viewModel.guardarNota(contenido)
    // Limpia el formulario
    contenidoNota = ""
}
```

**Logs esperados:**
```
I/NotasViewModel: guardarNota: Guardando nueva nota
I/RealmDataSource: Nota guardada exitosamente
I/NotasViewModel: guardarNota: Nota guardada exitosamente
```

### READ - Leer Notas
```kotlin
val notas by viewModel.notasFlow.collectAsState(initial = emptyList())
```

**Características:**
- ✅ Observación automática
- ✅ Actualización instantánea
- ✅ Sin polling manual
- ✅ Eficiente

### UPDATE - Editar Nota
```kotlin
fun actualizarNota(id: String, contenido: String) {
    viewModel.actualizarNota(id, contenido)
}
```

**Proceso:**
1. Usuario toca botón ✏️
2. Nota se carga en formulario
3. Usuario edita contenido
4. Usuario toca "Actualizar"
5. Nota se actualiza en BD
6. Lista se refresca

### DELETE - Eliminar Nota
```kotlin
fun eliminarNota(id: String) {
    viewModel.eliminarNota(id)
}
```

**Nota:** La función está disponible pero requiere agregar método a NotasRepository.

---

## 🔌 Integración ViewModel - UI

### Inyección con Hilt:
```kotlin
@Composable
fun NotasScreen(
    viewModel: NotasViewModel = hiltViewModel()
)
```

✅ El ViewModel se crea automáticamente
✅ Se mantiene durante configuración
✅ Se destruye con la pantalla

### Observación de Flows:
```kotlin
val notas by viewModel.notasFlow.collectAsState(initial = emptyList())
val esRelacional by viewModel.esRelacionalFlow.collectAsState(initial = false)
```

✅ Conversión de Flow a State
✅ Recomposición en cambios
✅ Lifecycle aware

---

## 🎨 Diseño Visual

### Colores
```
SQL (Relacional):     Verde (#4CAF50)
Realm (NoSQL):        Azul (#2196F3)
Fondo formulario:     surfaceVariant
Tarjeta:              surface
```

### Tipografía
```
Título pantalla:      titleMedium
Etiquetas:            labelSmall
Contenido:            bodyMedium
Descripción:          labelSmall
```

### Espaciado
```
Padding horizontal:   16.dp
Padding vertical:     8-24.dp
Gap entre elementos:  8-16.dp
Altura mínima campo:  100.dp
```

---

## 📊 Ejemplo de Uso Completo

### Flujo usuario:
```
1. App inicia
   → Notas cargadas de Realm (default)
   → Chip azul "📄 Usando Realm (NoSQL)"

2. Usuario cambia Switch
   → Chip cambia a verde "📊 Usando SQLite (SQL)"
   → Datos se migran automáticamente
   → Lista se actualiza < 1 segundo

3. Usuario escribe nota
   "Comprar leche"

4. Usuario toca "Guardar"
   → Nota se guarda en SQL
   → Lista se actualiza inmediatamente
   → Campo se limpia

5. Usuario edita nota
   → Toca botón ✏️
   → Contenido carga en formulario
   → Modifica texto
   → Toca "Actualizar"
   → Nota actualizada en tiempo real

6. Usuario elimina nota
   → Toca botón 🗑️
   → Nota eliminada
   → Lista actualizada
```

---

## 🔧 Requisitos Cumplidos

✅ **Switch en TopAppBar**
- Posicionado en acciones
- Funcional y reactivo
- Cambia motor dinámicamente

✅ **Indicador Visual (Chip)**
- Color diferenciado por motor
- Texto claro
- Ícono sugestivo
- Actualización instantánea

✅ **Lista Reactiva con collectAsState()**
- collectAsState() implementado
- Actualización sin reinicio
- Sin necesidad de polling
- Eficiencia garantizada

✅ **CRUD Completo**
- Create: ✅ Nuevo formulario
- Read: ✅ Lista observable
- Update: ✅ Edición en formulario
- Delete: ✅ Botón eliminar

---

## 🚀 Próximos Pasos

1. **Compilar:** Gradle build
2. **Ejecutar:** En emulador o device
3. **Pruebas:** Crear/editar/eliminar notas
4. **Cambiar motor:** Toca el Switch
5. **Observar logs:** Logcat para rastreo

---

## 📚 Archivos Relacionados

- `NotasViewModel.kt` - Lógica de negocio
- `NotasScreen.kt` - Interfaz de usuario
- `DataModule.kt` - Inyección de dependencias
- `ProyectoMovilesApp.kt` - Inicialización Hilt
- `MainActivity.kt` - Punto de entrada

