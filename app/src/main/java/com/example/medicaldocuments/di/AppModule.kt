package com.example.medicaldocuments.di

import android.content.Context
import com.example.medicaldocuments.data.local.database.AppDatabase
import com.example.medicaldocuments.data.local.database.DocumentDao
import com.example.medicaldocuments.data.local.repository.DocumentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(dao: DocumentDao): DocumentRepository {
        return DocumentRepository(dao)
    }
}