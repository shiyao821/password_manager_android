package com.example.passwordmanagerv1

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanagerv1.utils.CommonUIBehaviors

class MiscFieldsAdapter(
    private val context: Context,
    private val misc: List<Pair<String, String>>,
    private val onEditMiscClickListener: OnEditMiscClickListener
    ) : RecyclerView.Adapter<MiscFieldsAdapter.ViewHolder>() {

    interface OnEditMiscClickListener {
        fun onClick(fieldTitle: String) {}
    }

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
            val ivMiscValueEdit = itemView.findViewById<ImageView>(R.id.ivMiscValueEdit)
            val ivMiscValueCopy = itemView.findViewById<ImageView>(R.id.ivMiscValueCopy)

            val title = misc[position].first
            val value = misc[position].second
            tvMiscTitle.text = title
            tvMiscValue.text = value

            ivMiscValueEdit.setOnClickListener {
                onEditMiscClickListener.onClick(title)
            }
            ivMiscValueCopy.setOnClickListener {
                CommonUIBehaviors.copyToClipboard(context, value, title)
            }
        }
    }

}
