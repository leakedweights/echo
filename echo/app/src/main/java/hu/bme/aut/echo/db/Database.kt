package hu.bme.aut.echo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bme.aut.echo.models.Transcription

@Database(entities = [Transcription::class], version = 2)
@TypeConverters(Converters::class)
abstract class TranscriptionsDatabase: RoomDatabase() {
    abstract fun transcriptionDao(): TranscriptionDao

    companion object {
        fun getDatabase(applicationContext: Context): TranscriptionsDatabase {
            return Room.databaseBuilder(
                applicationContext,
                TranscriptionsDatabase::class.java,
                "transcriptions").fallbackToDestructiveMigration().build()
        }
    }
}