package com.arukai.uajpspeak.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.arukai.uajpspeak.R;
import com.arukai.uajpspeak.model.NavDrawerItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdView;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class MainActivity
        extends AppCompatActivity
        implements FragmentDrawer.FragmentDrawerListener, TextToSpeech.OnInitListener {
    public static String PACKAGE_NAME;
    public static Context context;
    private AdView mAdView;

    public static int prev_position = 0;
    public static int current_position = 0;

    public static Fragment fragment = null;
    public static String category = "";

    protected static final String APP_SETTINGS="AppSettings";
    protected static SharedPreferences app_settings;

    private MenuItem mSearchAction;
    public static boolean isSearchOpened = false;
    private EditText editSearch;
    public static String[] all_phrases;
    public static FragmentDrawer drawerFragment;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, initializationStatus -> {
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        MainActivity.context = getApplicationContext();
        app_settings = getSharedPreferences(APP_SETTINGS, MODE_PRIVATE);
        all_phrases = collectAllPhrases();

        Toolbar mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(0);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set Ukrainian language
            int result = textToSpeech.setLanguage(new Locale("uk"));
            // Log whether language is available or not
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Ukrainian language data is missing or not supported.");
            } else {
                Log.i("TTS", "Ukrainian language data is available.");
            }
        } else {
            Log.e("TTS", "TextToSpeech initialization failed.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            if(isSearchOpened){ //test if the search is open
                hideSearchBar();
            }
            Fragment fragment;
            String title;
            fragment = new AboutFragment();
            title = getString(R.string.title_about);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment, "ABOUT");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            // set the toolbar title
            setActionBarTitle(title);
            drawerFragment.setDrawerState(false);
            drawerFragment.mDrawerToggle.setToolbarNavigationClickListener(v -> onBackPressed());
            enableBackButton(true);

            return true;
        }

        if (id == R.id.action_alphabet) {
            if(isSearchOpened){ //test if the search is open
                hideSearchBar();
            }
            Fragment fragment;
            String title;
            fragment = new AlphabetFragment();
            title = getString(R.string.title_alphabet);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment, "ALPHABET");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();

            // set the toolbar title
            setActionBarTitle(title);
            drawerFragment.setDrawerState(false);
            drawerFragment.mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            enableBackButton(true);

            return true;
        }

        if(id == R.id.action_gender_lang){
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
            builder.setTitle(R.string.action_gender_lang);
            final SharedPreferences.Editor editor = app_settings.edit();

            //list of items
            String[] items = getResources().getStringArray(R.array.gender_lang_selection);
            builder.setSingleChoiceItems(items, app_settings.getInt("gender_lang", 0),
                    (dialog, which) -> editor.putInt("gender_lang", which));

            String positiveText = getString(R.string.ok);
            builder.setPositiveButton(positiveText,
                    (dialog, which) -> {
                        editor.apply();
                        HomeFragment home = (HomeFragment)
                                getSupportFragmentManager().findFragmentByTag("HOME");
                        if (home != null && home.isVisible())
                            displayView(current_position);
                    });

            String negativeText = getString(R.string.cancel);
            builder.setNegativeButton(negativeText,
                    (dialog, which) -> editor.clear());

            AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();

            return true;
        }

        if(id == R.id.action_search){
            handleMenuSearch();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void handleMenuSearch(){
        if(isSearchOpened){ //test if the search is open
            hideSearchBar();
        } else { //open the search entry
            openSearchBar();
        }
    }

    private String[] collectAllPhrases(){
        String[] all_phrases = ArrayUtils.concat(
            getResources().getStringArray(R.array.greetings),
            getResources().getStringArray(R.array.signs),
            getResources().getStringArray(R.array.troubleshooting),
            getResources().getStringArray(R.array.transportation),
            getResources().getStringArray(R.array.directions),
            getResources().getStringArray(R.array.hotel),
            getResources().getStringArray(R.array.numbers),
            getResources().getStringArray(R.array.time),
            getResources().getStringArray(R.array.weekdays),
            getResources().getStringArray(R.array.months),
            getResources().getStringArray(R.array.colors),
            getResources().getStringArray(R.array.common_words),
            getResources().getStringArray(R.array.restaurant),
            getResources().getStringArray(R.array.love),
            getResources().getStringArray(R.array.shopping),
            getResources().getStringArray(R.array.clothing),
            getResources().getStringArray(R.array.drugstore),
            getResources().getStringArray(R.array.driving),
            getResources().getStringArray(R.array.bank)
        );
        return all_phrases;
    }

    public void hideSearchBar(){
        ActionBar action = getSupportActionBar(); //get the actionbar

        action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
        action.setDisplayShowTitleEnabled(true); //show the title in the action bar

        //hides the keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        //add the search icon in the action bar
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_action_search));

        displayView(prev_position);
        isSearchOpened = false;
    }

    private void openSearchBar(){
        displayView(0);

        ActionBar action = getSupportActionBar(); //get the actionbar

        action.setDisplayShowCustomEnabled(true); //enable it to display a
        // custom view in the action bar.
        action.setCustomView(R.layout.search_bar);//add the custom view
        action.setDisplayShowTitleEnabled(false); //hide the title

        editSearch = action.getCustomView().findViewById(R.id.editSearch); //the text editor

        //this is a listener to do a search when the user clicks on search button
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch(v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        //this is a listener to do a search when the user is typing
        editSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                doSearch(s.toString());
            }
        });

        editSearch.requestFocus();

        //open the keyboard focused in the editSearch
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);

        //add the close icon
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_action_close));

        isSearchOpened = true;
    }

    private void doSearch(String search_string) {
        HomeFragment home = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HOME");
        home.mAdapter.getFilter().filter(search_string);
    }

    @Override
    public void onBackPressed(){
        ZoomFragment zoom = (ZoomFragment)
                getSupportFragmentManager().findFragmentByTag("ZOOM");
        AboutFragment about = (AboutFragment)
                getSupportFragmentManager().findFragmentByTag("ABOUT");
        AlphabetFragment alphabet = (AlphabetFragment)
                getSupportFragmentManager().findFragmentByTag("ALPHABET");
        if ((zoom != null && zoom.isVisible()) || (about != null && about.isVisible()) || (alphabet != null && alphabet.isVisible())){
            //Change activity to previous view
            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();

                if(fragmentManager.getBackStackEntryCount() != 0) {
                    fragmentManager.popBackStack();
                } else {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container_body, fragment, "HOME");
                    fragmentTransaction.commit();
                }
                setActionBarTitle(category);
                enableBackButton(false);
                drawerFragment.setDrawerState(true);
            }
        }
        else
            super.onBackPressed();
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        prev_position = current_position;
        current_position = position;

        NavDrawerItem unselItem = FragmentDrawer.activeData.get(MainActivity.prev_position);
        unselItem.setSelected(false);
        NavDrawerItem selItem = FragmentDrawer.activeData.get(MainActivity.current_position);
        selItem.setSelected(true);
        FragmentDrawer.activeData.set(MainActivity.prev_position, unselItem);
        FragmentDrawer.activeData.set(MainActivity.current_position, selItem);
        FragmentDrawer.adapter.notifyItemChanged(MainActivity.prev_position);
        FragmentDrawer.adapter.notifyItemChanged(MainActivity.current_position);

        switch (position) {
            case 0:
                fragment = HomeFragment.newInstance(-1, all_phrases);
                category = getString(R.string.title_all);
                break;
            case 1:
                fragment = HomeFragment.newInstance(R.array.greetings);
                category = getString(R.string.title_greetings);
                break;
            case 2:
                fragment = HomeFragment.newInstance(R.array.signs);
                category = getString(R.string.title_signs);
                break;
            case 3:
                fragment = HomeFragment.newInstance(R.array.troubleshooting);
                category = getString(R.string.title_troubleshooting);
                break;
            case 4:
                fragment = HomeFragment.newInstance(R.array.transportation);
                category = getString(R.string.title_transportation);
                break;
            case 5:
                fragment = HomeFragment.newInstance(R.array.directions);
                category = getString(R.string.title_directions);
                break;
            case 6:
                fragment = HomeFragment.newInstance(R.array.hotel);
                category = getString(R.string.title_hotel);
                break;
            case 7:
                fragment = HomeFragment.newInstance(R.array.numbers);
                category = getString(R.string.title_numbers);
                break;
            case 8:
                fragment = HomeFragment.newInstance(R.array.time);
                category = getString(R.string.title_time);
                break;
            case 9:
                fragment = HomeFragment.newInstance(R.array.weekdays);
                category = getString(R.string.title_weekdays);
                break;
            case 10:
                fragment = HomeFragment.newInstance(R.array.months);
                category = getString(R.string.title_months);
                break;
            case 11:
                fragment = HomeFragment.newInstance(R.array.colors);
                category = getString(R.string.title_colors);
                break;
            case 12:
                fragment = HomeFragment.newInstance(R.array.common_words);
                category = getString(R.string.title_common_words);
                break;
            case 13:
                fragment = HomeFragment.newInstance(R.array.restaurant);
                category = getString(R.string.title_restaurant);
                break;
            case 14:
                fragment = HomeFragment.newInstance(R.array.love);
                category = getString(R.string.title_love);
                break;
            case 15:
                fragment = HomeFragment.newInstance(R.array.shopping);
                category = getString(R.string.title_shopping);
                break;
            case 16:
                fragment = HomeFragment.newInstance(R.array.clothing);
                category = getString(R.string.title_clothing);
                break;
            case 17:
                fragment = HomeFragment.newInstance(R.array.drugstore);
                category = getString(R.string.title_drugstore);
                break;
            case 18:
                fragment = HomeFragment.newInstance(R.array.driving);
                category = getString(R.string.title_driving);
                break;
            case 19:
                fragment = HomeFragment.newInstance(R.array.bank);
                category = getString(R.string.title_bank);
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment, "HOME");
            fragmentTransaction.commit();

            // set the toolbar title
            setActionBarTitle(category);
        }
    }

    public void setActionBarTitle(String title){
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void enableBackButton(boolean state){
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(state);
        }
    }
}