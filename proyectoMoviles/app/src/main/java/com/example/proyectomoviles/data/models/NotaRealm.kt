package com.example.proyectomoviles.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

// ¡Es vital que tenga ": RealmObject"!
class NotaRealm : RealmObject {
    @PrimaryKey
    var id: String = ""
    var contenido: String = ""
}