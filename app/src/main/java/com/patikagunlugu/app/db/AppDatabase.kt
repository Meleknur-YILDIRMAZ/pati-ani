package com.patikagunlugu.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.patikagunlugu.app.dao.MemoryDao
import com.patikagunlugu.app.dao.PetDao
import com.patikagunlugu.app.dao.VaccineDao
import com.patikagunlugu.app.model.Memory
import com.patikagunlugu.app.model.Pet
import com.patikagunlugu.app.model.Vaccine

@Database(entities = [Pet::class, Memory::class, Vaccine::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun petDao(): PetDao
    abstract fun memoryDao(): MemoryDao
    abstract fun vaccineDao(): VaccineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "patikagunlugu_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
