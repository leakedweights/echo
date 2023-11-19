package hu.bme.aut.echo.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import hu.bme.aut.echo.models.Transcription

@Dao
interface TranscriptionDao {
    @Query("select * from transcriptions")
    fun getAll(): List<Transcription>

    @Query("select * from transcriptions where id=:id")
    fun getById(id: Long): Transcription

    @Query("select * from transcriptions order by created desc limit 1")
    fun getLast(): Transcription?

    @Insert
    fun insert(transcription: Transcription)

    @Delete
    fun delete(transcription: Transcription)

    @Query("select ifnull(sum(word_count), 0)  from transcriptions")
    fun getWordCount(): Int
}