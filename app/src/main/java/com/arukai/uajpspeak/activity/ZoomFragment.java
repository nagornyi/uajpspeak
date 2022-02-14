package com.arukai.uajpspeak.activity;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arukai.uajpspeak.R;
import com.arukai.uajpspeak.model.AudioPlayer;

import static com.arukai.uajpspeak.activity.MainActivity.context;

public class ZoomFragment extends Fragment {
    //private static final String LOG_TAG = "ZOOM";
    //private static String mFileName = null;
    //private static File mFile = null;
    //TextToSpeech t1;
    //private MediaRecorder mRecorder = null;
    //ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

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

        final String audio = args.getString("audio");
        final int id = getResources().getIdentifier("a" + audio, "raw",
                getActivity().getApplicationContext().getPackageName());

        // recording
        //mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        //mFileName += "/uajpspeak/" + "a" + audio + ".3gp";
        //mFile = new File(mFileName);

        View.OnClickListener l = new View.OnClickListener() {
            //boolean mStartRecording = true;
            @Override
            public void onClick(View v) {
                if(id != 0) {
                    AudioPlayer.play(context, id);
                } else {
                    // recording a new sound
/*                    onRecord(mStartRecording);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                    mStartRecording = !mStartRecording;*/
                }
/*                t1 = new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR) {
                            t1.setLanguage(new Locale("uk_UA"));
                            t1.setSpeechRate(0.5f);
                            t1.speak(ukrainian, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });*/
            }
        };
        View.OnLongClickListener ll = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AudioPlayer.slowPlay(context, id);
                return true;
            }
        };

        ukrainianView.setOnClickListener(l);
        speakerIcon.setOnClickListener(l);

        ukrainianView.setOnLongClickListener(ll);
        speakerIcon.setOnLongClickListener(ll);

        // Inflate the layout for this fragment
        return rootView;
    }

//    private void onRecord(boolean start) {
//        if (start) {
//            startRecording();
//        } else {
//            stopRecording();
//            MediaScannerConnection.scanFile(getActivity().getApplicationContext(),
//                    new String[]{mFileName}, null,
//                    new MediaScannerConnection.OnScanCompletedListener() {
//                        public void onScanCompleted(String path, Uri uri) {
//                        }
//                    });
//        }
//    }

//    private void startRecording() {
//        mFile.delete();
//        mRecorder = new MediaRecorder();
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mRecorder.setOutputFile(mFileName);
//        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
//
//        try {
//            mRecorder.prepare();
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "prepare() failed");
//        }
//
//        mRecorder.start();
//    }
//
//    private void stopRecording() {
//        mRecorder.stop();
//        mRecorder.release();
//        mRecorder = null;
//    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
