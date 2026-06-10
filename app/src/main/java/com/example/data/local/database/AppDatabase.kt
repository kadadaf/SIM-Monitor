package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SIMCardDao
import com.example.data.local.entity.ReminderRecord
import com.example.data.local.entity.RuleTemplate
import com.example.data.local.entity.SIMCard
import com.example.data.local.entity.UsageRecord

@Database(
    entities = [
        SIMCard::class,
        RuleTemplate::class,
        UsageRecord::class,
        ReminderRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun simCardDao(): SIMCardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sim_monitor_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
