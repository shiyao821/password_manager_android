package com.example.passwordmanagerv1

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MenuAdapter (
    private val context: Context,
    private var optionCodesOrder: List<String>,
    private var optionTextsMap: Map<String, String>,
    private var onOptionClickedListener: onOptionClickListener
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    companion object {
        private val TAG = "Menu Adapter"
    }

    interface onOptionClickListener {
        fun onOptionClicked(position: Int)
    }

    override fun getItemCount() = optionCodesOrder.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val option = itemView.findViewById<TextView>(R.id.tvOption)
        fun bind(position: Int) {
            option.text = optionTextsMap[optionCodesOrder[position]].toString()
            option.setOnClickListener {
                Log.i(TAG, "Option ${option.text} clicked")
                onOptionClickedListener.onOptionClicked(position)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.option_main_menu, parent, false)
        return ViewHolder(view)
    }
}