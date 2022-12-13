package com.example.passwordmanagerv1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class LinkedAccountsFieldAdapter (
    private val context: Context,
    private val linkedAccounts: List<String>,
        ): RecyclerView.Adapter<LinkedAccountsFieldAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "Debug LinkedAccountsFieldAdapter"
    }

    override fun getItemCount(): Int {
        return linkedAccounts.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.account_button, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val btnAccountName = itemView.findViewById<Button>(R.id.btnAccountName)
            btnAccountName.text = linkedAccounts[position]
        }
    }
}