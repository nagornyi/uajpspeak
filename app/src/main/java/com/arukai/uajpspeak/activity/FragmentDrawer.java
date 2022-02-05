package com.arukai.uajpspeak.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.arukai.uajpspeak.R;
import com.arukai.uajpspeak.adapter.NavigationDrawerAdapter;
import com.arukai.uajpspeak.model.NavDrawerItem;

public class FragmentDrawer extends Fragment {

    private static String TAG = FragmentDrawer.class.getSimpleName();

    private RecyclerView recyclerView;
    public ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    public static NavigationDrawerAdapter adapter;
    private View containerView;
    private static String[] titles = null;
    private static String[] titles_pro = null;
    private static int p_counter = 0;
    private static int p_pro_counter = 0;
    private FragmentDrawerListener drawerListener;
    public static List<NavDrawerItem> activeData;

    public FragmentDrawer() {
    }

    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    public List<NavDrawerItem> getData() {
        List<NavDrawerItem> data = new ArrayList<>();

        // preparing navigation drawer items
        for (int i = 0; i < titles.length; i++) {
            NavDrawerItem navItem = new NavDrawerItem();
            navItem.setTitle(titles[i]);
            data.add(navItem);
        }

        p_counter = getActivity().getResources().getStringArray(R.array.greetings).length +
                getActivity().getResources().getStringArray(R.array.signs).length +
                getActivity().getResources().getStringArray(R.array.troubleshooting).length +
                getActivity().getResources().getStringArray(R.array.transportation).length +
                getActivity().getResources().getStringArray(R.array.directions).length +
                getActivity().getResources().getStringArray(R.array.hotel).length +
                getActivity().getResources().getStringArray(R.array.numbers).length +
                getActivity().getResources().getStringArray(R.array.time).length +
                getActivity().getResources().getStringArray(R.array.weekdays).length +
                getActivity().getResources().getStringArray(R.array.months).length +
                getActivity().getResources().getStringArray(R.array.colors).length +
                getActivity().getResources().getStringArray(R.array.common_words).length;

        p_pro_counter = getActivity().getResources().getStringArray(R.array.restaurant).length +
                getActivity().getResources().getStringArray(R.array.love).length +
                getActivity().getResources().getStringArray(R.array.shopping).length +
                getActivity().getResources().getStringArray(R.array.clothing).length +
                getActivity().getResources().getStringArray(R.array.drugstore).length;

        if (getResources().getString(R.string.app_name).equalsIgnoreCase("ウクライナ語会話集 Pro")) {
            for (int i = 0; i < titles_pro.length; i++) {
                NavDrawerItem navItem = new NavDrawerItem();
                navItem.setTitle(titles_pro[i]);
                data.add(navItem);
            }
            p_counter += p_pro_counter;
        }

        data.get(MainActivity.current_position).setSelected(true);
        return data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // drawer labels
        titles = getActivity().getResources().getStringArray(R.array.nav_drawer_labels);
        titles_pro = getActivity().getResources().getStringArray(R.array.nav_drawer_labels_pro);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflating view layout
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        recyclerView = layout.findViewById(R.id.drawerList);
        activeData = getData();
        adapter = new NavigationDrawerAdapter(getActivity(), activeData);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                drawerListener.onDrawerItemSelected(view, position);
                mDrawerLayout.closeDrawer(containerView);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        layout.setOnClickListener(null);

        TextView buy_pro_handler = layout.findViewById(R.id.buyPro);
        TextView phrases_count = layout.findViewById(R.id.phrasesCount);
        if (getResources().getString(R.string.app_name).equals("ウクライナ語会話集 Pro")) {
            buy_pro_handler.setVisibility(View.GONE);
            phrases_count.setText(p_counter+"フレーズ（フルバージョン）");
        } else {
            phrases_count.setText(p_counter+"フレーズ（フリーバージョン）");
            buy_pro_handler.setText("+"+p_pro_counter+"フレーズを追加するには、フルバージョンにアップグレードしてください");
            View.OnClickListener buy_pro_listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "market://details?id=com.arukai.uajpspeak.pro";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            };
            buy_pro_handler.setOnClickListener(buy_pro_listener);
        }
        return layout;
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {

                if(MainActivity.isSearchOpened) {
                    ActionBar action = ((MainActivity)getActivity()).getSupportActionBar();
                    action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
                    action.setDisplayShowTitleEnabled(true); //show the title in the action bar

                    //hides the keyboard
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                    MainActivity.isSearchOpened = false;
                }

                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public interface FragmentDrawerListener {
        void onDrawerItemSelected(View view, int position);
    }

    public void setDrawerState(boolean enabled) {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        mDrawerLayout.setDrawerLockMode(lockMode);
        mDrawerToggle.setDrawerIndicatorEnabled(enabled);
    }
}
