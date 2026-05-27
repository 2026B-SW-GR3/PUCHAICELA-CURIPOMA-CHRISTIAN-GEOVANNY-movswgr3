# 🏗️ Arquitectura de la Capa de Datos

## Estructura de Carpetas

```
app/src/main/
├── java/com/example/proyectomoviles/
│   └── data/
│       ├── models/
│       │   ├── Nota.kt                 ← Modelo de dominio (puro)
│       │   ├── NotaRealm.kt            ← Entidad Realm (NoSQL)
│       │   └── NotaSql.kt              ← Entidad SQL
│       ├── repositories/
│       │   └── NotasRepository.kt      ← Interfaz del repositorio
│       └── mappers/
│           └── NotaMappers.kt          ← Convertidores entre modelos
│
└── sqldelight/com/example/proyectomoviles/
    └── AppDatabase.sq                  ← Queries SQL
```

---

## 🔄 Flujo de Datos

### Opción 1: Usando Realm (NoSQL)

```
┌─────────────────────┐
│   UI (Composable)   │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────────────────┐
│   NotasRepository (Interface)   │
│  - notasFlow: StateFlow         │
│  - esRelacionalFlow: StateFlow  │
│  - guardarNota()                │
│  - cambiarMotor()               │
└──────────┬──────────────────────┘
           │
           ↓
┌──────────────────────────────────┐
│  NotasRepositoryImpl (Realm)      │
│  - Maneja Realm como BD          │
└──────────┬───────────────────────┘
           │
           ↓ (NotaMappers)
┌──────────────────────────────────┐
│      NotaRealm (Entity)          │
│  - @PrimaryKey id: String        │
│  - contenido: String             │
└──────────┬───────────────────────┘
           │
           ↓
┌──────────────────────────────────┐
│    Realm Database (NoSQL)        │
└──────────────────────────────────┘
```

### Opción 2: Usando SQLDelight (SQL)

```
┌─────────────────────┐
│   UI (Composable)   │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────────────────┐
│   NotasRepository (Interface)   │
│  - notasFlow: StateFlow         │
│  - esRelacionalFlow: StateFlow  │
│  - guardarNota()                │
│  - cambiarMotor()               │
└──────────┬──────────────────────┘
           │
           ↓
┌──────────────────────────────────┐
│  NotasRepositoryImpl (SQL)        │
│  - Maneja SQLDelight como BD     │
└──────────┬───────────────────────┘
           │
           ↓ (AppDatabase.sq)
┌──────────────────────────────────┐
│       AppDatabase (Generated)    │
│  - getAllNotas(): Flow<List>     │
│  - insertNota(): Unit            │
│  - updateNota(): Unit            │
│  - deleteNota(): Unit            │
└──────────┬───────────────────────┘
           │
           ↓
┌──────────────────────────────────┐
│    SQLite Database (SQL)         │
└──────────────────────────────────┘
```

---

## 📊 Comparativa: Realm vs SQLDelight

| Aspecto | Realm (NoSQL) | SQLDelight (SQL) |
|---------|---------------|-----------------|
| **Tipo** | Documento/Objeto | Relacional |
| **Entidad** | `NotaRealm : RealmObject` | `AppDatabase.sq` |
| **Clave Primaria** | `@PrimaryKey` | `PRIMARY KEY` |
| **Mapeo** | `NotaMappers.notaRealm.toDomainModel()` | Generado automáticamente |
| **Reactivo** | A través de `observeAsFlow()` | Nativo con `Flow` |
| **Queries** | Kotlin DSL | SQL puro en `.sq` |
| **Tipo-Safe** | En tiempo de compilación | En tiempo de compilación |

---

## 🔀 Cambio de Motor (SQL ↔ NoSQL)

La función `cambiarMotor()` permitirá:

1. Detectar el motor actual
2. Migrar datos desde el origen al destino
3. Cambiar el StateFlow `esRelacionalFlow`
4. Redirigir todas las operaciones futuras

```kotlin
// Cambiar de Realm a SQL
repositorio.cambiarMotor(esRelacional = true)

// Cambiar de SQL a Realm
repositorio.cambiarMotor(esRelacional = false)
```

---

## 🎯 Ventajas de esta Arquitectura

✅ **Aislamiento**: La UI no conoce los detalles de la BD
✅ **Testeable**: Puedes mockear la interfaz del repositorio
✅ **Flexible**: Cambiar entre Realm y SQL sin tocar la UI
✅ **Reactivo**: StateFlow para observación automática
✅ **Type-Safe**: El compilador verifica tipos
✅ **Escalable**: Fácil de extender con nuevas operaciones

---

## 📦 Dependencias Necesarias

```gradle
// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Realm
implementation("io.realm.kotlin:library-base:1.13.0")

// SQLDelight
implementation("app.cash.sqldelight:android-driver:2.0.1")
implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
```

---

## 🚀 Próximos Pasos

1. **Implementar NotasRepositoryImpl** con soporte para ambos motores
2. **Crear un DataSource** para Realm y SQLDelight
3. **Implementar la lógica de migración** en `cambiarMotor()`
4. **Crear un ViewModel** que use el repositorio
5. **Integrar en la UI** con Compose

