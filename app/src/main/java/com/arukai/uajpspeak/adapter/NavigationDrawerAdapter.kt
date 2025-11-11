package com.arukai.uajpspeak.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.model.NavDrawerItem

class NavigationDrawerAdapter(
    context: Context,
    private val data: MutableList<NavDrawerItem>
) : RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    fun delete(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.nav_drawer_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val current = data[position]
        holder.title.text = current.title
        if (current.isSelected) {
            holder.title.setBackgroundResource(R.drawable.drawer_item_selected)
            holder.title.setTextColor(holder.itemView.context.getColor(R.color.selectedCategoryText))
            holder.title.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            holder.title.setBackgroundResource(R.drawable.drawer_item_unselected)
            holder.title.setTextColor(holder.itemView.context.getColor(R.color.unselectedCategoryText))
            holder.title.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    override fun getItemCount(): Int = data.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }
}

