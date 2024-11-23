package com.arukai.uajpspeak.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arukai.uajpspeak.R;

import java.util.Locale;

public class ZoomFragment extends Fragment {
    TextToSpeech t1;

    public ZoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static ZoomFragment newInstance(String japanese, String ukrainian, String phonetic, String audio) {
        ZoomFragment f = new ZoomFragment();
        // Supply selected phrase as an argument.
        Bundle args = new Bundle();
        args.putString("japanese", japanese);
        args.putString("ukrainian", ukrainian);
        args.putString("phonetic", phonetic);
        args.putString("audio", audio);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_alphabet).setVisible(false);
        menu.findItem(R.id.action_about).setVisible(false);
        menu.findItem(R.id.action_gender_lang).setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_zoom, container, false);

        final TextView japaneseView = rootView.findViewById(R.id.zoomJapanese);
        final TextView ukrainianView = rootView.findViewById(R.id.zoomUkrainian);
        final ImageView speakerIcon = rootView.findViewById(R.id.zoomSpeaker);
        final TextView phoneticView = rootView.findViewById(R.id.zoomPhonetic);

        Bundle args = getArguments();
        final String japanese = args.getString("japanese");
        final String ukrainian = args.getString("ukrainian");
        final String phonetic = args.getString("phonetic");
        japaneseView.setText(japanese);
        ukrainianView.setText(ukrainian.toUpperCase());
        phoneticView.setText(phonetic);

        View.OnClickListener l = v -> t1 = new TextToSpeech(getActivity().getApplicationContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                t1.setLanguage(new Locale("uk"));
                t1.setSpeechRate(0.5f);
                t1.speak(ukrainian, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        ukrainianView.setOnClickListener(l);
        speakerIcon.setOnClickListener(l);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
