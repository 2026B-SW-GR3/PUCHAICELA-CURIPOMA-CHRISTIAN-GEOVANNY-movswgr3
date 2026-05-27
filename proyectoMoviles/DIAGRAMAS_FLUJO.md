# 📊 Diagrama Visual - Flujo de NotasRepositoryImpl

## 🔄 Flujo 1: Guardar Nota en Motor Activo

```
┌─────────────────────┐
│   Usuario/ViewModel │
│  .guardarNota()     │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────────────────────────┐
│   NotasRepositoryImpl.guardarNota()      │
│   1. Log: INFO - Iniciando guardado     │
│   2. Validar ID y contenido             │
│   3. Obtener DataSource activa          │
└──────────┬────────────────────┬─────────┘
           │                    │
    ¿Motor activo?              │
    /         \                 │
   /           \                │
FALSE         TRUE              │
 /               \              │
│                 │             │
↓                 ↓             ↓
RealmDataSource   SqlDataSource
│                 │
├─ Log.i: Guardando
├─ Log.d: Validar
├─ Log.d: Insert/Update
├─ Log.i: Éxito
└─ Log.e: Error (si aplica)
```

---

## 🔀 Flujo 2: Cambiar de Motor (Realm → SQL)

```
┌────────────────────────────────────────────┐
│  NotasRepositoryImpl.cambiarMotor(true)     │
└────────────────┬───────────────────────────┘
                 │
                 ↓
    ┌────────────────────────────────┐
    │ 1. Log.i: Cambio iniciado      │
    │    (Realm → SQL)               │
    └────────────┬───────────────────┘
                 │
                 ↓
    ┌────────────────────────────────┐
    │ 2. Log.d: Obtener notas        │
    │    del motor anterior (Realm)  │
    │                                │
    │    RealmDataSource             │
    │    .getAllNotasDirect()        │
    │    → Retorna 5 notas           │
    └────────────┬───────────────────┘
                 │
                 ↓ Log.i: 5 notas recuperadas
    ┌────────────────────────────────┐
    │ 3. Cambiar _esRelacionalFlow   │
    │    a TRUE (SQL activo)         │
    │                                │
    │    Log.i: Motor cambiado a SQL │
    └────────────┬───────────────────┘
                 │
                 ↓
    ┌────────────────────────────────┐
    │ 4. Log.d: Limpiar SQL          │
    │                                │
    │    SqlDataSource               │
    │    .limpiarTodas()             │
    │    Log.i: Limpieza completada  │
    └────────────┬───────────────────┘
                 │
                 ↓ Log.d: Iniciar migración
    ┌────────────────────────────────────┐
    │ 5. Para cada nota (1-5):           │
    │                                    │
    │    Para nota-1:                    │
    │    ├─ Log.i: Guardando             │
    │    ├─ Log.d: Insert                │
    │    ├─ Log.i: Éxito                 │
    │    └─ Log.d: Migrada (1/5)         │
    │                                    │
    │    Para nota-2:                    │
    │    ├─ Log.i: Guardando             │
    │    ├─ Log.d: Insert                │
    │    ├─ Log.i: Éxito                 │
    │    └─ Log.d: Migrada (2/5)         │
    │                                    │
    │    ... (repite para 3, 4, 5)      │
    └────────────┬───────────────────────┘
                 │
                 ↓
    ┌────────────────────────────────┐
    │ 6. Log.i: MIGRACIÓN COMPLETADA │
    │    5 notas migradas de         │
    │    Realm a SQL                 │
    └────────────────────────────────┘

En caso de ERROR en cualquier paso:
└─ Log.e: Error al migrar
   Revierte: _esRelacionalFlow = false
   Lanza excepción
```

---

## 🎯 Decisión: ¿Qué DataSource Usar?

```
┌─────────────────────────────────────┐
│ getDataSourceActiva()               │
└────────────┬────────────────────────┘
             │
             ↓
     ┌───────────────────┐
     │ ¿_esRelacionalFlow? │
     └──┬─────────┬───────┘
        │         │
      TRUE      FALSE
        │         │
        │         └──────────────┐
        │                        │
        ↓                        ↓
   ┌─────────┐          ┌─────────────┐
   │SqlSource│          │RealmDataSource│
   └─────────┘          └─────────────┘
        │                        │
        └──────────┬─────────────┘
                   │
                   ↓
           ┌──────────────┐
           │Guardar aquí  │
           │Solo aquí ✓   │
           │Otro no ✗     │
           └──────────────┘
```

---

## 📈 Ciclo Completo de Operación

```
TIEMPO →

T0: Inicialización
├─ Log.i: NotasRepositoryImpl inicializado
├─ _esRelacionalFlow = false (Realm)
└─ Listo para operaciones

T1: Guardar nota-001
├─ Log.i: guardarNota iniciada
├─ Log.d: Obtener DataSource activa
├─ Log.d: Motor = Realm
├─ RealmDataSource.guardarNota(nota-001)
│  └─ Log.i: Guardada en Realm
└─ Resultado: nota-001 en Realm ✓

T2: Guardar nota-002
├─ Log.i: guardarNota iniciada
├─ Log.d: Motor = Realm
├─ RealmDataSource.guardarNota(nota-002)
│  └─ Log.i: Guardada en Realm
└─ Resultado: nota-002 en Realm ✓

T3: Cambiar a SQL
├─ Log.i: cambiarMotor iniciado
├─ Log.d: Obtener notas de Realm (nota-001, nota-002)
├─ Log.i: 2 notas recuperadas
├─ Log.i: Motor = SQL
├─ Log.d: Limpiar SQL
├─ Para nota-001:
│  ├─ Log.i: Guardando
│  └─ SqlDataSource.guardarNota(nota-001)
├─ Para nota-002:
│  ├─ Log.i: Guardando
│  └─ SqlDataSource.guardarNota(nota-002)
└─ Log.i: Migración completada ✓

T4: Guardar nota-003
├─ Log.i: guardarNota iniciada
├─ Log.d: Motor = SQL
├─ SqlDataSource.guardarNota(nota-003)
│  └─ Log.i: Guardada en SQL
└─ Resultado: nota-003 en SQL ✓

ESTADO FINAL:
┌─────────────────────┬──────────────────┐
│ Realm               │ SQL              │
├─────────────────────┼──────────────────┤
│ ✓ nota-001          │ ✓ nota-001       │
│ ✓ nota-002          │ ✓ nota-002       │
│ ✗ nota-003          │ ✓ nota-003       │
└─────────────────────┴──────────────────┘
```

---

## 🔍 Validaciones en guardarNota()

```
guardarNota(nota)
    │
    ├─ Validar ID
    │  │
    │  ├─ ✓ ID válido → Continuar
    │  │               Log.d: Validaciones pasadas
    │  │
    │  └─ ✗ ID vacío → Lanzar error
    │                  Log.e: ID vacío
    │                  throw IllegalArgumentException
    │
    ├─ Validar contenido
    │  │
    │  ├─ ✓ Contenido válido → Continuar
    │  │
    │  └─ ✗ Contenido vacío → Lanzar error
    │                         Log.e: Contenido vacío
    │                         throw IllegalArgumentException
    │
    └─ Guardar en DataSource activa
       │
       ├─ ✓ Éxito → Log.i: Guardada exitosamente
       │
       └─ ✗ Error → Log.e: Error al guardar
                    Lanzar excepción
```

---

## 🚨 Manejo de Errores en cambiarMotor()

```
cambiarMotor(esRelacional)
    │
    ├─ Obtener notas del motor anterior
    │  │
    │  ├─ ✓ Éxito → Log.d: X notas recuperadas
    │  │
    │  └─ ✗ Error → Log.e: Error obteniendo notas
    │              Lanzar excepción
    │
    ├─ Cambiar _esRelacionalFlow
    │
    ├─ Limpiar nueva DataSource
    │  │
    │  ├─ ✓ Éxito → Log.d: Limpieza completada
    │  │
    │  └─ ✗ Error → Log.e: Error limpiando
    │              Revertir: _esRelacionalFlow = anterior
    │              Lanzar excepción
    │
    ├─ Migrar cada nota
    │  │
    │  ├─ Nota 1:
    │  │  ├─ ✓ Éxito → Log.d: Migrada (1/X)
    │  │  │
    │  │  └─ ✗ Error → Log.e: Error migrando nota-1
    │  │             Revertir: _esRelacionalFlow = anterior
    │  │             Lanzar excepción
    │  │
    │  └─ ... (repite para cada nota)
    │
    └─ Log.i: Migración completada exitosamente
```

---

## 💾 Estructura de Datos en Base de Datos

### Realm (NoSQL)
```
RealmDB
├─ NotaRealm {
│  ├─ @PrimaryKey id: String
│  └─ contenido: String
│}
│
└─ Tabla de datos:
   ├─ nota-001: "Mi contenido..."
   ├─ nota-002: "Otro contenido..."
   └─ nota-003: "Más contenido..."
```

### SQL (SQLite)
```
SQLiteDB
├─ TABLE Nota {
│  ├─ id TEXT PRIMARY KEY
│  └─ contenido TEXT
│}
│
└─ Registros:
   ├─ nota-001 | "Mi contenido..."
   ├─ nota-002 | "Otro contenido..."
   └─ nota-003 | "Más contenido..."
```

---

## 🎬 Ejemplo de Secuencia de Logs

```
[Estado Inicial]
I/NotasRepositoryImpl: NotasRepositoryImpl inicializado con motor: Realm

[Guardar nota-001]
I/NotasRepositoryImpl: guardarNota: Iniciando guardado en motor activo: Realm
D/NotasRepositoryImpl: guardarNota: Validaciones pasadas, guardando en Realm
I/RealmDataSource: Guardando nota en Realm: id=nota-001
D/RealmDataSource: Insertando nueva nota: id=nota-001
I/RealmDataSource: Nota guardada exitosamente en Realm: id=nota-001
I/NotasRepositoryImpl: guardarNota: Nota guardada exitosamente en Realm - id=nota-001

[Cambiar a SQL]
I/NotasRepositoryImpl: cambiarMotor: Iniciando cambio de motor de Realm a SQL
D/NotasRepositoryImpl: cambiarMotor: Obteniendo todas las notas del motor anterior (Realm)
D/RealmDataSource: Obteniendo todas las notas de Realm de forma directa
D/RealmDataSource: getAllNotasDirect: Se recuperaron 1 notas de Realm
I/NotasRepositoryImpl: cambiarMotor: 1 notas recuperadas del motor anterior
I/NotasRepositoryImpl: cambiarMotor: Motor cambiado a SQL
D/NotasRepositoryImpl: cambiarMotor: Limpiando datos previos en motor nuevo (SQL)
I/SqlDataSource: Limpiando TODAS las notas de SQLDelight
I/SqlDataSource: Todas las notas han sido eliminadas de SQLDelight
D/NotasRepositoryImpl: cambiarMotor: Iniciando migración de 1 notas a SQL
I/SqlDataSource: Guardando nota en SQLDelight: id=nota-001
D/SqlDataSource: Insertando nueva nota: id=nota-001
I/SqlDataSource: Nota guardada exitosamente en SQLDelight: id=nota-001
D/NotasRepositoryImpl: cambiarMotor: Nota migrada (1/1) - id=nota-001
I/NotasRepositoryImpl: cambiarMotor: Migración completada exitosamente. 1 notas migradas de Realm a SQL

[Guardar nota-002]
I/NotasRepositoryImpl: guardarNota: Iniciando guardado en motor activo: SQL
D/NotasRepositoryImpl: guardarNota: Validaciones pasadas, guardando en SQL
I/SqlDataSource: Guardando nota en SQLDelight: id=nota-002
D/SqlDataSource: Insertando nueva nota: id=nota-002
I/SqlDataSource: Nota guardada exitosamente en SQLDelight: id=nota-002
I/NotasRepositoryImpl: guardarNota: Nota guardada exitosamente en SQL - id=nota-002
```

---

## 📊 Comparación: Operaciones en Cada Motor

| Operación | Realm | SQL | Estado |
|-----------|-------|-----|--------|
| guardarNota (T1) | ✓ nota-001 | - | En Realm |
| cambiarMotor (T3) | - | ✓ nota-001 (migrada) | Migrando |
| guardarNota (T4) | - | ✓ nota-002 | En SQL |
| **Total** | 1 nota | 2 notas | Aislados |


