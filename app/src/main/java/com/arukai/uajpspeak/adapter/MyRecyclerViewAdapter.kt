package com.arukai.uajpspeak.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.activity.HomeFragment
import com.arukai.uajpspeak.model.Abecadlo
import com.arukai.uajpspeak.model.DataObject
import com.arukai.uajpspeak.util.LocaleHelper
import com.arukai.uajpspeak.App

class MyRecyclerViewAdapter(
    private val mDataset: ArrayList<DataObject>
) : RecyclerView.Adapter<MyRecyclerViewAdapter.DataObjectHolder>(), Filterable {

    var mFilteredDataset: ArrayList<DataObject> = mDataset
    private var userFilter: UserFilter? = null
    private var myClickListener: MyClickListener? = null

    companion object {
        private const val LOG_TAG = "MyRecyclerViewAdapter"
        private val abc = Abecadlo()
    }

    interface MyClickListener {
        fun onItemClick(position: Int, v: View)
    }

    inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val firstRow: TextView = itemView.findViewById(R.id.textView)
        val secondRow: TextView = itemView.findViewById(R.id.textView2)
        val thirdRow: TextView = itemView.findViewById(R.id.textView3)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            v.setOnClickListener(null)
            myClickListener?.onItemClick(adapterPosition, v)
        }
    }

    fun setOnItemClickListener(listener: MyClickListener) {
        this.myClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_row, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        val part1 = mDataset[position].mText1
        val part2 = mDataset[position].mText2
        val part3 = mDataset[position].mText3

        // The dataset is already filtered by HomeFragment according to current gender.
        // Bind values directly.
        val firstLine = part2
        val ukr = part3.replace("*", "")
        val lang = LocaleHelper.getSavedLanguage(App.appContext)
        val phonetic = if (lang == "ja") abc.convert(part3) else abc.romanize(part3)

        holder.firstRow.text = firstLine
        holder.secondRow.text = ukr
        holder.thirdRow.text = phonetic

        val firstFlagRes = when (lang) {
            "en" -> R.drawable.uk
            "de" -> R.drawable.de
            "fr" -> R.drawable.fr
            "ja" -> R.drawable.jp
            else -> R.drawable.uk
        }
        holder.firstRow.setCompoundDrawablesWithIntrinsicBounds(firstFlagRes, 0, 0, 0)
        holder.secondRow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ua, 0, 0, 0)
    }

    fun addItem(dataObj: DataObject, index: Int) {
        mDataset.add(index, dataObj)
        notifyItemInserted(index)
    }

    fun deleteItem(index: Int) {
        mDataset.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun getItemCount(): Int = mDataset.size

    override fun getFilter(): Filter {
        if (userFilter == null)
            userFilter = UserFilter(this, mDataset)
        return userFilter!!
    }

    private class UserFilter(
        private val adapter: MyRecyclerViewAdapter,
        originalList: ArrayList<DataObject>
    ) : Filter() {

        private val originalList: ArrayList<DataObject> = ArrayList(originalList)
        private val filteredList: ArrayList<DataObject> = ArrayList()

        override fun performFiltering(constraint: CharSequence): FilterResults {
            filteredList.clear()
            val results = FilterResults()

            if (constraint.isEmpty()) {
                filteredList.addAll(originalList)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()

                for (user in originalList) {
                    val sourceText = user.mText2.lowercase()
                    val ukr = user.mText3.lowercase().replace("*", "")
                    if (sourceText.contains(filterPattern) || ukr.contains(filterPattern)) {
                        filteredList.add(user)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            adapter.mFilteredDataset.clear()
            @Suppress("UNCHECKED_CAST")
            adapter.mDataset.addAll(results.values as ArrayList<DataObject>)
            adapter.notifyDataSetChanged()
        }
    }
}
