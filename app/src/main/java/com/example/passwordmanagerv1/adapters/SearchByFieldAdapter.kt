package com.example.passwordmanagerv1.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.R

class SearchByFieldAdapter (
    private val context: Context,
    private val results: List<String>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SearchByFieldAdapter.ViewHolder>() {

    private var displayList = results

    interface OnItemClickListener {
        fun onItemClick(item: String)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val accountName = itemView.findViewById<TextView>(R.id.tvSearchItemAccountName)
            accountName.text = if (displayList[position] != "")
                displayList[position] else context.resources.getString(R.string.field_empty)
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


    fun filter(input: String) {
        displayList = results.filter{ accountName -> accountName.contains(input) }
        notifyDataSetChanged()
    }
}
