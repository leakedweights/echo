package hu.bme.aut.echo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hu.bme.aut.echo.databinding.ActivityHomeBinding
import hu.bme.aut.echo.models.Transcription
import hu.bme.aut.echo.db.TranscriptionsDatabase
import hu.bme.aut.echo.http.Vectorize
import hu.bme.aut.echo.utils.getSigninClient
import hu.bme.aut.echo.utils.startAnimationFromBottom
import java.io.IOException
import java.util.Locale
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var db: TranscriptionsDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var googleSignInClient: GoogleSignInClient

    private var speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = results?.get(0) ?: ""
            saveTranscription(recognizedText)
            Log.d("MainActivity", recognizedText)
        } else {
            Log.d("MainActivity", "Google Speech recognizer could not pick up speech, inserting mock context.")
            val mockText = """
                In a world kissed by the gentle touch of nature, a perfect day unfolded. It began with a sunrise hike through a forest where the light danced through leaves, casting a tapestry of shadows and sunbeams. At the summit, a breathtaking view of rolling hills on one side and an endless ocean on the other greeted the hikers. This was followed by a delightful breakfast at a quaint café, where the aroma of freshly brewed coffee and sweet pastries filled the air, accompanied by laughter and stories shared among friends.
                The afternoon and evening were a seamless blend of joy and relaxation. A beach visit offered warm sands, playful waves, and the simple pleasure of building sandcastles and swimming in the turquoise sea. As the day mellowed into a golden evening, a leisurely bike ride through picturesque streets led to a cozy backyard barbecue. Surrounded by family and friends, with a backdrop of starry skies and the soft strumming of music, the day drew to a close. It was a day so full of warmth, camaraderie, and serene beauty that it remained etched in memory as the epitome of perfection.
            """.trimIndent()

            saveTranscription(mockText)
        }
    }

    companion object {
        const val REQUEST_MICROPHONE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        binding = ActivityHomeBinding.inflate(layoutInflater)
        db = TranscriptionsDatabase.getDatabase(applicationContext)
        setContentView(binding.root)

        googleSignInClient = getSigninClient(this, getString(R.string.googleid_client_id))

        binding.header.ivLogout.setOnClickListener {
            googleSignInClient.signOut()
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        playAnimations()
        setupRecordButton()
    }

    private fun playAnimations() {
        binding.header.ivTitle.startAnimationFromBottom(delay = 300)
        binding.header.ivLogout.startAnimationFromBottom(delay = 300)
        binding.tvGreeting.startAnimationFromBottom(delay = 500)
        binding.btnChat.startAnimationFromBottom(delay = 700)
        binding.statsCard.startAnimationFromBottom(delay = 900)
        binding.recentCard.startAnimationFromBottom(delay = 1100)
        binding.btnRecord.startAnimationFromBottom(delay = 1300)
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        user = auth.currentUser!!
        binding.tvGreeting.text = "Hi, ${user.displayName?.split(" ")?.get(0) ?: "Anon"} 👋"
    }


    private fun setupRecordButton() {
        val btnRecord: Button = binding.btnRecord

        val drawable = ContextCompat.getDrawable(this, R.drawable.recording)?.apply {
            setBounds(0, 0, 112, 112)
        }
        btnRecord.setCompoundDrawables(drawable, null, null, null)

        btnRecord.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
            } else {
                startRecording()
            }
        }

        binding.btnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_MICROPHONE)
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
            }
            speechResultLauncher.launch(intent)
        }
    }


    private fun saveTranscription(text: String) {

        val client = Vectorize()

        client.postVectorizeRequest(user, text, object : Vectorize.PostVectorizeCallback {
            override fun onSuccess(searchId: String) {

                val transcription = Transcription(0L, searchId, text, text.split(' ').size)
                db.transcriptionDao().insert(transcription)

                runOnUiThread {
                    setRecentNote()
                }
            }

            override fun onFailure(e: IOException) {
                e.printStackTrace()
            }
        })
    }

    private fun setRecentNote() {
        var lastItem: Transcription?
        thread {
            lastItem = db.transcriptionDao().getLast()
            runOnUiThread {
                binding.tvRecent.text = lastItem?.text ?: "You don't have any notes. Start recording to add one."
            }
        }
    }

    private fun setWordCount() {
        thread {
            val totalWordCount: Int = db.transcriptionDao().getWordCount()
            runOnUiThread {
                binding.tvWordCount.text = "$totalWordCount Words"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setRecentNote()
        setWordCount()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_MICROPHONE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startRecording()
            } else {
                Toast.makeText(this, "Microphone permission is required to use this feature.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
