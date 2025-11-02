package com.arukai.uajpspeak.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.arukai.uajpspeak.R;
import com.arukai.uajpspeak.adapter.MyRecyclerViewAdapter;
import com.arukai.uajpspeak.model.DataObject;

import java.util.ArrayList;

import static com.arukai.uajpspeak.activity.MainActivity.category;
import static com.arukai.uajpspeak.activity.MainActivity.drawerFragment;
import static com.arukai.uajpspeak.activity.MainActivity.fragment;

public class HomeFragment extends Fragment {

    private RecyclerView mRecyclerView;
    public MyRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static int index = -1;
    public static int top = -1;
    public static char gender;
    public static String[] phrases = null;
    private ArrayList<DataObject> dataSet;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private ArrayList<DataObject> getDataSet() {
        Bundle args = getArguments();
        int index = args.getInt("index", 0);
        ArrayList results = new ArrayList<>();
        if (phrases == null) phrases = getResources().getStringArray(index);
        for (String s: phrases) {
            String[] parts = s.split("/");
            char code = parts[0].charAt(parts[0].length() - 1);
            if(code == gender || (code != 'm' && code != 'f')) {
                DataObject obj = new DataObject(parts[0], parts[1], parts[2]);
                results.add(obj);
            }
        }
        return results;
    }

    private String getAudio(String ukr){
        for (DataObject item : dataSet) {
            if(item.getmText3().replace("*", "").equals(ukr)) return item.getmText1();
        }
        return null;
    }

    public static HomeFragment newInstance(int index, String[] selected_phrases) {
        HomeFragment f = new HomeFragment();
        switch(MainActivity.app_settings.getInt("gender_lang", 0)){
            case 1: gender = 'f'; break;
            default: gender = 'm'; break;
        }
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        phrases = selected_phrases;
        f.setArguments(args);
        return f;
    }

    public static HomeFragment newInstance(int index) {
        return newInstance(index, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = rootView.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        dataSet = getDataSet();
        mAdapter = new MyRecyclerViewAdapter(dataSet);
        mRecyclerView.setAdapter(mAdapter);
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(index != -1)
        {
            mLayoutManager.scrollToPosition(index);
        }
        mAdapter.setOnItemClickListener((position, v) -> {
            if(MainActivity.isSearchOpened) {
                ActionBar action = ((MainActivity)getActivity()).getSupportActionBar();
                action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
                action.setDisplayShowTitleEnabled(true); //show the title in the action bar

                //hides the keyboard
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                MainActivity.isSearchOpened = false;
            }

            Fragment fragment;
            String jpn = ((TextView)v.findViewById(R.id.textView)).getText().toString();
            String ukr = ((TextView)v.findViewById(R.id.textView2)).getText().toString();
            String phonetic = ((TextView)v.findViewById(R.id.textView3)).getText().toString();
            fragment = ZoomFragment.newInstance(jpn, ukr, phonetic, getAudio(ukr));
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_body, fragment, "ZOOM");
                fragmentTransaction.addToBackStack(null).commit();
                ((MainActivity) getActivity()).setActionBarTitle("");
                drawerFragment.setDrawerState(false);
                drawerFragment.mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                enableBackButton(true);
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        LinearLayoutManager layoutManager = ((LinearLayoutManager)mRecyclerView.getLayoutManager());
        index = layoutManager.findFirstVisibleItemPosition();
        View v = mRecyclerView.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - mRecyclerView.getPaddingTop());
    }

    public void onBackPressed(){
                if(getFragmentManager().getBackStackEntryCount() != 0) {
                    getFragmentManager().popBackStack();
                } else {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.container_body, fragment, "HOME");
                    fragmentTransaction.commit();
                }
                ((MainActivity)getActivity()).getSupportActionBar().setTitle(category);
                enableBackButton(false);
                drawerFragment.setDrawerState(true);
    }

    public void enableBackButton(boolean state){
        ActionBar actionbar = ((MainActivity)getActivity()).getSupportActionBar();
        if(actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(state);
        }
    }

}
