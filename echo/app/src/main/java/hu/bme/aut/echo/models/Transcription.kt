package hu.bme.aut.echo.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName="transcriptions")
data class Transcription (
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    @ColumnInfo(name="search_id") var searchId: String,
    @ColumnInfo(name = "text") var text: String,
    @ColumnInfo(name="word_count") var wordCount: Int,
    @ColumnInfo(name="created") var created: Date = Date(),
)