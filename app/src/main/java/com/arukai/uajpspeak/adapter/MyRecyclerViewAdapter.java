package com.arukai.uajpspeak.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.arukai.uajpspeak.R;
import com.arukai.uajpspeak.model.Abecadlo;
import com.arukai.uajpspeak.model.DataObject;

import java.util.ArrayList;

import static com.arukai.uajpspeak.activity.HomeFragment.gender;

public class MyRecyclerViewAdapter extends RecyclerView
        .Adapter<MyRecyclerViewAdapter
        .DataObjectHolder> implements Filterable {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<DataObject> mDataset;
    private static MyClickListener myClickListener;

    ArrayList<DataObject> mFilteredDataset;
    private UserFilter userFilter;
    private static Abecadlo abc;

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView firstRow;
        TextView secondRow;
        TextView thirdRow;

        public DataObjectHolder(View itemView) {
            super(itemView);
            firstRow = itemView.findViewById(R.id.textView);
            secondRow = itemView.findViewById(R.id.textView2);
            thirdRow = itemView.findViewById(R.id.textView3);
            itemView.setOnClickListener(this);
            abc = new Abecadlo();
        }

        @Override
        public void onClick(View v) {
            v.setOnClickListener(null);
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        MyRecyclerViewAdapter.myClickListener = myClickListener;
    }

    public MyRecyclerViewAdapter(ArrayList<DataObject> myDataset) {
        mDataset = myDataset;
        mFilteredDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_row, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        String part1 = mDataset.get(position).getmText1();
        String part2 = mDataset.get(position).getmText2();
        String part3 = mDataset.get(position).getmText3();

        String jp = "";
        String ukr = "";
        String phonetic = "";

        char code = part1.charAt(part1.length() - 1);
        if(code == gender || (code != 'm' && code != 'f')) {
            jp = part2;
            phonetic = abc.convert(part3);
            ukr = part3.replace("*", "");
        }

        holder.firstRow.setText(jp);
        holder.secondRow.setText(ukr);
        holder.thirdRow.setText(phonetic);
    }

    public void addItem(DataObject dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
    }

    @Override
    public Filter getFilter() {
        if(userFilter == null)
            userFilter = new UserFilter(this, mDataset);
        return userFilter;
    }

    private static class UserFilter extends Filter {

        private final MyRecyclerViewAdapter adapter;

        private final ArrayList<DataObject> originalList;

        private final ArrayList<DataObject> filteredList;

        private UserFilter(MyRecyclerViewAdapter adapter, ArrayList<DataObject> originalList) {
            super();
            this.adapter = adapter;
            this.originalList = new ArrayList<>(originalList);
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();

                for (final DataObject user : originalList) {
                    String jpn = user.getmText2();
                    String ukr = user.getmText3().toLowerCase().replace("*", "");
                    if (jpn.contains(filterPattern) || ukr.contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.mFilteredDataset.clear();
            adapter.mDataset.addAll((ArrayList<DataObject>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}