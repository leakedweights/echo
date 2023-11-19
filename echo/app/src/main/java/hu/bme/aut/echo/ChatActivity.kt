package hu.bme.aut.echo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hu.bme.aut.echo.adapters.MessageAdapter
import hu.bme.aut.echo.databinding.ActivityChatBinding
import hu.bme.aut.echo.models.Message
import hu.bme.aut.echo.http.Chat
import hu.bme.aut.echo.utils.getSigninClient
import hu.bme.aut.echo.utils.startAnimationFromBottom
import java.io.IOException

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var googleSignInClient: GoogleSignInClient

    private var messages: MutableList<Message> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)

        googleSignInClient = getSigninClient(this, getString(R.string.googleid_client_id))

        setupRecyclerView()
        setupUserInput()
        setupSendButton()
        setupLogoutButton()
        playAnimations()
    }
    override fun onStart() {
        super.onStart()
        messages.add(Message("Hi! How can I help you today?", Message.Sender.Echo),)
        user = auth.currentUser!!
    }

    private fun playAnimations() {
        binding.header.ivTitle.startAnimationFromBottom(delay = 300)
        binding.header.ivLogout.startAnimationFromBottom(delay = 300)
        binding.chatInput.startAnimationFromBottom(delay = 900)
    }


    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val target = binding.userInput.text.toString()
            if(target != "") {

                val client = Chat()

                val clientMessage = Message(target, Message.Sender.User)
                var echoMessage: Message

                binding.userInput.setText("")
                hideKeyboard()

                runOnUiThread {
                    messages.add(clientMessage)
                    messageAdapter.notifyItemInserted(messages.size - 1)
                    notifyMessageInsert()
                }

                client.postMessage(user, clientMessage, messages.subList(0, messages.size - 1), object : Chat.PostMessageCallback {
                    override fun onSuccess(responseMessage: String) {
                        runOnUiThread {
                            echoMessage = Message(responseMessage, Message.Sender.Echo)
                            messages.add(echoMessage)
                            messageAdapter.notifyItemInserted(messages.size - 1)
                            notifyMessageInsert()
                        }
                    }


                    override fun onFailure(e: IOException) {
                        Log.d("ChatActivity", "Could not get response.")
                        runOnUiThread {
                            val failureMessage =
                                Message("Oops! Couldn't get response.", Message.Sender.Echo, false)
                            messages.add(failureMessage)
                            messageAdapter.notifyItemInserted(messages.size - 1)
                        }
                    }
                })
            }
        }
    }

    private fun setupLogoutButton() {
        binding.header.ivLogout.setOnClickListener {
            googleSignInClient.signOut()
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun notifyMessageInsert() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
        }, 100)
    }



    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = this.currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }

        binding.chatInput.clearFocus()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages)

        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun setupUserInput() {
        binding.userInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.userInput.setLines(4)
            } else {
                binding.userInput.setLines(1)
            }
        }
    }
}
