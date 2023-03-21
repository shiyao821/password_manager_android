package com.example.passwordmanagerv1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.R

class SearchByAccountNameAdapter (
    private val context: Context,
    private var results: List<String>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SearchByAccountNameAdapter.ViewHolder>() {

    private var displayList = results
    private var filterText = ""

    interface OnItemClickListener {
        fun onItemClick(accountName: String)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val accountName = itemView.findViewById<TextView>(R.id.tvSearchItemAccountName)
            accountName.text = displayList[position]
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(displayList[position])
            }
        }
    }

    override fun getItemCount() = displayList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.search_item_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    fun updateData(newResults : List<String>) {
        results = newResults
        displayList = results
        if (filterText.isNotEmpty()) {
            filter(filterText)
            return
        }
        notifyDataSetChanged()
    }

    fun filter(input: String) {
        filterText = input
        displayList = results.filter{ accountName -> accountName.contains(input) }
        notifyDataSetChanged()
    }
}
