package opsecurity.oph.passwordmanager.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import opsecurity.oph.passwordmanager.R
import opsecurity.oph.passwordmanager.data.entities.Password

class PasswordAdapter(
    private val onPasswordClick: (Password) -> Unit
) : ListAdapter<Password, PasswordAdapter.PasswordViewHolder>(PasswordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password, parent, false)
        return PasswordViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewUsername: TextView = itemView.findViewById(R.id.textViewUsername)
        private val textViewHint: TextView = itemView.findViewById(R.id.textViewHint)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPasswordClick(getItem(position))
                }
            }
        }

        fun bind(password: Password) {
            textViewTitle.text = password.title
            // The username field can be shown if needed
            if (password.username.isNotEmpty() && password.username != password.title) {
                textViewUsername.text = password.username
                textViewUsername.visibility = View.VISIBLE
                textViewHint.visibility = View.GONE
            } else {
                textViewUsername.visibility = View.GONE
                textViewHint.visibility = View.VISIBLE
            }
        }
    }

    private class PasswordDiffCallback : DiffUtil.ItemCallback<Password>() {
        override fun areItemsTheSame(oldItem: Password, newItem: Password): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Password, newItem: Password): Boolean {
            return oldItem == newItem
        }
    }
} 