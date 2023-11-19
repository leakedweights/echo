package hu.bme.aut.echo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.echo.R
import hu.bme.aut.echo.databinding.MessageItemBinding
import hu.bme.aut.echo.models.Message
import hu.bme.aut.echo.utils.fadeIn

class MessageAdapter(private val messages: List<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message: Message = messages[position]
        if(message.sender == Message.Sender.User) {
            val userTextColor = ContextCompat.getColor(holder.itemView.context, R.color.text_user)
            holder.binding.messageItem.setBackgroundResource(R.drawable.user_message_background)
            holder.binding.tvSender.setTextColor(userTextColor)
            holder.binding.tvCardContent.setTextColor(userTextColor)
        }
        if(!message.successful) {
            holder.binding.messageItem.setBackgroundResource(R.drawable.failure_background)
        }
        holder.binding.tvSender.text = message.sender.toString().replaceFirstChar(Char::titlecase)
        holder.binding.tvCardContent.text = message.content
        holder.binding.messageItem.fadeIn(delay = 600)
    }


    override fun getItemCount(): Int {
        return messages.size
    }
}
