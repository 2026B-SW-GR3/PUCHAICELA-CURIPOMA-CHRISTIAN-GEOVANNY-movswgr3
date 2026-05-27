package com.example.proyectomoviles.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.proyectomoviles.AppDatabase
import com.example.proyectomoviles.data.datasources.NotasDataSource
import com.example.proyectomoviles.data.datasources.RealmDataSource
import com.example.proyectomoviles.data.datasources.SqlDataSource
import com.example.proyectomoviles.data.models.NotaRealm
import com.example.proyectomoviles.data.repositories.NotasRepository
import com.example.proyectomoviles.data.repositories.NotasRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 1. Proveer SQLDelight
    @Provides
    @Singleton
    fun provideSqlDatabase(@ApplicationContext context: Context): AppDatabase {
        val driver = AndroidSqliteDriver(AppDatabase.Schema, context, "notas_epn.db")
        return AppDatabase(driver)
    }

    // 2. Proveer Realm
    @Provides
    @Singleton
    fun provideRealmDatabase(): Realm {
        val config = RealmConfiguration.Builder(schema = setOf(NotaRealm::class))
            .name("notas_epn.realm")
            .build()
        return Realm.open(config)
    }

    // 3. Proveer DataSources
    @Provides
    @Singleton
    @Named("SQL")
    fun provideSqlDataSource(database: AppDatabase): NotasDataSource {
        return SqlDataSource(database)
    }

    @Provides
    @Singleton
    @Named("Realm")
    fun provideRealmDataSource(realm: Realm): NotasDataSource {
        return RealmDataSource(realm)
    }

    // 4. Proveer el Repositorio Híbrido
    @Provides
    @Singleton
    fun provideNotasRepository(
        @Named("Realm") realmDataSource: NotasDataSource,
        @Named("SQL") sqlDataSource: NotasDataSource
    ): NotasRepository {
        return NotasRepositoryImpl(
            realmDataSource = realmDataSource,
            sqlDataSource = sqlDataSource,
            esRelacionalInicial = false // Empieza en Realm por defecto
        )
    }
}