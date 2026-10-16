package com.patikagunlugu.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.patikagunlugu.app.db.AppDatabase
import com.patikagunlugu.app.utils.NotificationHelper
import java.util.Calendar

class VaccineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val vaccineDao = db.vaccineDao()
        val petDao = db.petDao()

        val threeDaysFromNow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 3)
        }.timeInMillis

        val upcomingVaccines = vaccineDao.getUpcomingVaccines(threeDaysFromNow)
        
        upcomingVaccines.forEach { vaccine ->
            // In a real app, you'd fetch the pet name too
            // NotificationHelper.showNotification(...)
        }

        return Result.success()
    }
}
