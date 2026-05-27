# 📋 Documentación SQL - AppDatabase.sq

## 📌 Estructura de la Base de Datos

### CREATE TABLE - Notas

```sql
CREATE TABLE Nota (
    id TEXT PRIMARY KEY NOT NULL,
    contenido TEXT NOT NULL
);
```

**Explicación:**
- `Nota`: Nombre de la tabla que almacenará las notas
- `id TEXT PRIMARY KEY NOT NULL`: 
  - Columna ID única para cada nota
  - Tipo TEXT (string/texto)
  - PRIMARY KEY: Solo puede haber un ID único por nota
  - NOT NULL: El campo es obligatorio
- `contenido TEXT NOT NULL`:
  - Columna que almacena el contenido de la nota
  - Tipo TEXT (string/texto)
  - NOT NULL: El contenido es obligatorio

---

## 🔍 Operaciones CRUD

### INSERT - insertNota
```sql
insertNota:
INSERT INTO Nota (id, contenido)
VALUES (?, ?);
```

**Uso en Kotlin:**
```kotlin
database.insertNota(id = "1", contenido = "Mi nota")
```

**Explicación:**
- Inserta una nueva nota en la tabla
- `?` son placeholders que se reemplazan con los valores en tiempo de ejecución
- Parámetros: id (String), contenido (String)

---

### UPDATE - updateNota
```sql
updateNota:
UPDATE Nota
SET contenido = ?
WHERE id = ?;
```

**Uso en Kotlin:**
```kotlin
database.updateNota(contenido = "Contenido actualizado", id = "1")
```

**Explicación:**
- Actualiza el contenido de una nota existente
- `WHERE id = ?` especifica qué nota actualizar por su ID
- Parámetros: contenido (String), id (String)

---

### SELECT - getAllNotas
```sql
getAllNotas:
SELECT * FROM Nota;
```

**Uso en Kotlin:**
```kotlin
val todasLasNotas: Flow<List<Nota>> = database.getAllNotas()
```

**Explicación:**
- Selecciona TODAS las notas de la tabla
- `*` significa "todas las columnas"
- Devuelve un Flow<List<Nota>> que puede ser observado

---

### SELECT - getNotaById
```sql
getNotaById:
SELECT * FROM Nota
WHERE id = ?;
```

**Uso en Kotlin:**
```kotlin
val nota: Flow<Nota?> = database.getNotaById(id = "1")
```

**Explicación:**
- Selecciona UNA nota específica por su ID
- `WHERE id = ?` filtra por el ID proporcionado
- Parámetro: id (String)
- Devuelve un Flow<Nota?> (puede ser nulo si no existe)

---

### DELETE - deleteNota
```sql
deleteNota:
DELETE FROM Nota
WHERE id = ?;
```

**Uso en Kotlin:**
```kotlin
database.deleteNota(id = "1")
```

**Explicación:**
- Elimina una nota específica por su ID
- `WHERE id = ?` especifica qué nota eliminar
- Parámetro: id (String)

---

### DELETE - deleteAllNotas
```sql
deleteAllNotas:
DELETE FROM Nota;
```

**Uso en Kotlin:**
```kotlin
database.deleteAllNotas()
```

**Explicación:**
- Elimina TODAS las notas de la tabla
- Sin WHERE clause (cuidado: operación irreversible)
- Sin parámetros

---

### COUNT - countNotas
```sql
countNotas:
SELECT COUNT(*) FROM Nota;
```

**Uso en Kotlin:**
```kotlin
val totalNotas: Flow<Long> = database.countNotas()
```

**Explicación:**
- Cuenta el número total de notas en la tabla
- `COUNT(*)` devuelve un valor numérico
- Devuelve un Flow<Long>

---

## 🔗 Integración con SQLDelight

**Ubicación del archivo:**
```
app/src/main/sqldelight/com/example/proyectomoviles/AppDatabase.sq
```

**Pasos para generar el código:**
1. SQLDelight analiza el archivo `.sq`
2. Genera la clase `AppDatabase` automáticamente
3. Crea métodos extension para cada query
4. Los métodos son type-safe y reactivos (devuelven Flow)

**Acceso en el código:**
```kotlin
// Después de crear la instancia de AppDatabase
val database = AppDatabase(driver = AndroidSqliteDriver(...))

// Usar las queries
database.getAllNotas().collect { notas ->
    println("Notas: $notas")
}
```

---

## 🛡️ Características de SQLDelight

✅ **Type-safe**: El compilador verifica que los tipos sean correctos
✅ **Reactivo**: Todas las queries devuelven Flow para observación
✅ **Suspendidas**: Compatible con corrutinas
✅ **Generación automática**: Genera la implementación del código SQL
✅ **Sin boilerplate**: No necesitas escribir adapters manualmente

---

## 📝 Notas Importantes

- Los nombres de las queries (insertNota, getAllNotas, etc.) se convierten en métodos en la clase generada
- Los `?` son placeholders seguros contra SQL injection
- SQLDelight genera code during build time
- Los Flows permiten observación reactiva de los datos

