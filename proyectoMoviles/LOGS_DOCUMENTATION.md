# 📊 Documentación de Logs - NotasRepositoryImpl

## 🔍 Sistema de Logs Estructurados

La implementación de `NotasRepositoryImpl` utiliza `android.util.Log` con los siguientes niveles:

### Niveles de Log

| Nivel | Método | Cuándo se usa | Color | Prioridad |
|-------|--------|---------------|-------|-----------|
| **DEBUG** | `Log.d()` | Información de debugging | Azul | 3 |
| **INFO** | `Log.i()` | Eventos importantes | Verde | 4 |
| **WARNING** | `Log.w()` | Advertencias (raras) | Amarillo | 5 |
| **ERROR** | `Log.e()` | Errores en operaciones | Rojo | 6 |

---

## 📝 Mensajes de Log por Operación

### 1. **Guardado de Notas** - `guardarNota(nota: Nota)`

```
[INFO] guardarNota: Iniciando guardado en motor activo: SQL/Realm
[DEBUG] guardarNota: Validaciones pasadas, guardando en SQL/Realm
[DEBUG] guardarNota: Nota a guardar - id=123, contenido=Mi contenido...
[INFO] guardarNota: Nota guardada exitosamente en SQL/Realm - id=123
```

**En caso de error:**
```
[ERROR] guardarNota: Error al guardar en SQL/Realm - Error message
```

**Validaciones que se loguean:**
- ✓ ID vacío rechazado (ERROR)
- ✓ Contenido vacío rechazado (ERROR)
- ✓ Selección de DataSource activa (DEBUG)
- ✓ Guardado exitoso (INFO)

---

### 2. **Cambio de Motor** - `cambiarMotor(esRelacional: Boolean)`

#### Flujo exitoso:
```
[INFO] cambiarMotor: Iniciando cambio de motor de Realm a SQL
[DEBUG] cambiarMotor: Obteniendo todas las notas del motor anterior (Realm)
[INFO] cambiarMotor: 25 notas recuperadas del motor anterior
[INFO] cambiarMotor: Motor cambiado a SQL
[DEBUG] cambiarMotor: Limpiando datos previos en motor nuevo (SQL)
[DEBUG] cambiarMotor: Iniciando migración de 25 notas a SQL
[DEBUG] cambiarMotor: Nota migrada (1/25) - id=nota-001
[DEBUG] cambiarMotor: Nota migrada (2/25) - id=nota-002
...
[DEBUG] cambiarMotor: Nota migrada (25/25) - id=nota-025
[INFO] cambiarMotor: Migración completada exitosamente. 25 notas migradas de Realm a SQL
```

#### En caso de error:
```
[ERROR] cambiarMotor: Error durante la migración, motor revertido a Realm - Error message
```

---

### 3. **Lectura de Notas** - `getAllNotas()`

```
[DEBUG] getAllNotas: Obteniendo todas las notas del motor activo (SQL)
[DEBUG] getAllNotas: 25 notas obtenidas
```

---

### 4. **Búsqueda por ID** - `getNotaById(id: String)`

#### Nota encontrada:
```
[DEBUG] getNotaById: Buscando nota con id=123 en motor activo (SQL)
[DEBUG] getNotaById: Nota encontrada - id=123
```

#### Nota no encontrada:
```
[DEBUG] getNotaById: Buscando nota con id=999 en motor activo (SQL)
[DEBUG] getNotaById: Nota no encontrada - id=999
```

---

### 5. **Eliminación de Nota** - `eliminarNota(id: String)`

#### Exitosa:
```
[INFO] eliminarNota: Eliminando nota con id=123 del motor activo (SQL)
[INFO] eliminarNota: Nota eliminada exitosamente - id=123
```

#### Con error:
```
[ERROR] eliminarNota: Error al eliminar nota id=123 - Error message
```

---

### 6. **Inicialización del Repositorio**

```
[INFO] NotasRepositoryImpl inicializado con motor: SQL
```

---

### 7. **Observación de Flujos Reactivos** - `notasFlow`

```
[DEBUG] Observando notas desde motor activo: SQL/Realm
```

---

## 🔍 Ejemplos Prácticos de Logs

### Ejemplo 1: Guardar una nota en SQL

```kotlin
repository.guardarNota(Nota(id = "1", contenido = "Mi primera nota"))
```

**Logs esperados:**
```
I/NotasRepositoryImpl: guardarNota: Iniciando guardado en motor activo: SQL
D/NotasRepositoryImpl: guardarNota: Validaciones pasadas, guardando en SQL
D/NotasRepositoryImpl: guardarNota: Nota a guardar - id=1, contenido=Mi primera nota
I/SqlDataSource: Guardando nota en SQLDelight: id=1, contenido=Mi primera nota...
D/SqlDataSource: Insertando nueva nota: id=1
I/SqlDataSource: Nota guardada exitosamente en SQLDelight: id=1
I/NotasRepositoryImpl: guardarNota: Nota guardada exitosamente en SQL - id=1
```

---

### Ejemplo 2: Cambiar de Realm a SQL

```kotlin
repository.cambiarMotor(esRelacional = true)
```

**Logs esperados:**
```
I/NotasRepositoryImpl: cambiarMotor: Iniciando cambio de motor de Realm a SQL
D/NotasRepositoryImpl: cambiarMotor: Obteniendo todas las notas del motor anterior (Realm)
D/RealmDataSource: Obteniendo todas las notas de Realm de forma directa
D/RealmDataSource: getAllNotasDirect: Se recuperaron 5 notas de Realm
I/NotasRepositoryImpl: cambiarMotor: 5 notas recuperadas del motor anterior
I/NotasRepositoryImpl: cambiarMotor: Motor cambiado a SQL
D/NotasRepositoryImpl: cambiarMotor: Limpiando datos previos en motor nuevo (SQL)
I/SqlDataSource: Limpiando TODAS las notas de SQLDelight
I/SqlDataSource: Todas las notas han sido eliminadas de SQLDelight
D/NotasRepositoryImpl: cambiarMotor: Iniciando migración de 5 notas a SQL
I/SqlDataSource: Guardando nota en SQLDelight: id=nota-1, contenido=...
D/SqlDataSource: Insertando nueva nota: id=nota-1
I/SqlDataSource: Nota guardada exitosamente en SQLDelight: id=nota-1
D/NotasRepositoryImpl: cambiarMotor: Nota migrada (1/5) - id=nota-1
... (repetir para notas 2-5)
I/NotasRepositoryImpl: cambiarMotor: Migración completada exitosamente. 5 notas migradas de Realm a SQL
```

---

## 🛡️ Manejo de Errores

### Validaciones con Log

1. **ID vacío:**
   ```
   E/NotasRepositoryImpl: guardarNota: ID de nota vacío, operación rechazada
   ```

2. **Contenido vacío:**
   ```
   E/NotasRepositoryImpl: guardarNota: Contenido de nota vacío, operación rechazada
   ```

3. **Error en base de datos:**
   ```
   E/SqlDataSource: Error al guardar nota en SQLDelight: id=1, error=database is locked
   E/NotasRepositoryImpl: guardarNota: Error al guardar en SQL - database is locked
   ```

4. **Error en migración:**
   ```
   E/NotasRepositoryImpl: cambiarMotor: Error migrando nota id=nota-3, error=IO exception
   E/NotasRepositoryImpl: cambiarMotor: Error durante la migración, motor revertido a Realm - IO exception
   ```

---

## 📱 Ver Logs en Android Studio

### Logcat
1. Abre **Logcat** en Android Studio (abajo del editor)
2. Filtra por tag: `NotasRepositoryImpl`, `SqlDataSource`, o `RealmDataSource`
3. Selecciona el nivel de log: `Debug`, `Info`, `Warn`, `Error`

### Filtrar en Logcat:
```
tag:NotasRepositoryImpl OR tag:SqlDataSource OR tag:RealmDataSource
```

### Ver solo errores:
```
level:E
```

### Ver cambios de motor:
```
tag:NotasRepositoryImpl cambiarMotor
```

---

## 🔗 Correlación de Logs

Los logs están estructurados para seguir el flujo completo:

```
NotasRepository (Interface)
    ↓ (implementa)
NotasRepositoryImpl (Orquesta la lógica)
    ↓ (delega a)
SqlDataSource / RealmDataSource (Ejecutan operaciones)
    ↓ (interactúan con)
SQLDelight / Realm (Bases de datos)
```

Cada nivel proporciona logs que permiten rastrear la ejecución completa.

---

## 💡 Tips para Debugging

1. **Seguir una operación completa:**
   - Busca el ID de la nota en los logs
   - Sigue su flujo desde repositorio hasta BD

2. **Detectar cambios de motor:**
   - Busca "cambiarMotor" en Logcat
   - Revisa cuántas notas se migraron

3. **Encontrar errores:**
   - Filtra por "ERROR" o "Exception"
   - Lee el stack trace completo

4. **Auditar acceso a BD:**
   - Cada operación (INSERT, UPDATE, SELECT, DELETE) genera logs
   - Puedes contar cuántas veces se accede a cada motor

