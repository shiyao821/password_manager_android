package com.example.passwordmanagerv1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.R

class LinkedAccountsAdapter(
    val context: Context,
    val linkedAccounts: List<String>,
    val onLinkedAccountClickListener: OnLinkedAccountClickListener,
    val onUnlinkAccountClickListener: OnUnlinkAccountClickListener,
) : RecyclerView.Adapter<LinkedAccountsAdapter.ViewHolder>() {

    companion object {
        const val TAG = "clg:LinkedAccAdapter"
    }

    interface OnLinkedAccountClickListener {
        fun onButtonClick(linkedAccountName: String) {}
    }

    interface OnUnlinkAccountClickListener {
        fun onButtonClick(linkedAccountName: String) {}
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val btnAccountName = itemView.findViewById<Button>(R.id.btnAccountName)
            btnAccountName.text = linkedAccounts[position]
            btnAccountName.setOnClickListener {
                onLinkedAccountClickListener.onButtonClick(linkedAccounts[position])
            }
            val ivUnlink = itemView.findViewById<ImageView>(R.id.ivUnlink)
            ivUnlink.setOnClickListener {
                onUnlinkAccountClickListener.onButtonClick(linkedAccounts[position])
            }
        }
    }

    override fun getItemCount() = linkedAccounts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.existing_linked_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }
}
