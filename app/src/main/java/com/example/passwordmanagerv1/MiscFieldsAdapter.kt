package com.example.passwordmanagerv1

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MiscFieldsAdapter(
    private val context: Context,
    private val misc: List<Pair<String, String>>
    ) : RecyclerView.Adapter<MiscFieldsAdapter.ViewHolder>() {

    override fun getItemCount(): Int = misc.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.misc_field, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val tvMiscTitle = itemView.findViewById<TextView>(R.id.tvMiscTitle)
            val tvMiscValue = itemView.findViewById<TextView>(R.id.tvMiscValue)
            tvMiscTitle.text = misc[position].first
            tvMiscValue.text = misc[position].second
        }
    }

}
