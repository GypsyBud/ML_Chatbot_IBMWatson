package x.y.z.ibmwatsonchatbot.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.android.text_to_speech.v1.TextToSpeech;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import x.y.z.ibmwatsonchatbot.CustomTypefaceSpan;
import x.y.z.ibmwatsonchatbot.R;
import x.y.z.ibmwatsonchatbot.providers.MyTokenProvider;
import x.y.z.ibmwatsonchatbot.util.ChatHelper;

public class FragmentTabTTS extends Fragment
{
    private static final String TAG = "FragmentTabTTS";

    public View mView = null;
    public Context mContext = null;
    public JSONObject jsonVoices = null;
    private Handler mHandler = null;

    TextView textTTS;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateTTS");
        mView = inflater.inflate(R.layout.tab_tts, container, false);
        mContext = getActivity().getApplicationContext();

        setText();
        if (initTTS() == false) {
            TextView viewPrompt = (TextView) mView.findViewById(R.id.prompt);
            viewPrompt.setText("Error: no authentication credentials or token available, please enter your authentication information");
            return mView;
        }

        if (jsonVoices == null) {
            jsonVoices = new TTSCommands().doInBackground();
            if (jsonVoices == null) {
                return mView;
            }
        }
        addItemsOnSpinnerVoices();
        updatePrompt(getString(R.string.voiceDefault));

        Spinner spinner = (Spinner) mView.findViewById(R.id.spinnerVoices);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                Log.d(TAG, "setOnItemSelectedListener");
                final Runnable runnableUi = new Runnable() {
                    @Override
                    public void run() {
                        FragmentTabTTS.this.updatePrompt(FragmentTabTTS.this.getSelectedVoice());
                    }
                };
                new Thread() {
                    public void run() {
                        mHandler.post(runnableUi);
                    }
                }.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        mHandler = new Handler();
        return mView;
    }

    private boolean initTTS() {

        // DISCLAIMER: please enter your credentials or token factory in the lines below

        String username = "XXX";
        String password = "XXX";
        String tokenFactoryURL = "https://stream.watsonplatform.net/text-to-speech/api";
        String serviceURL = "https://stream.watsonplatform.net/text-to-speech/api";

        TextToSpeech.sharedInstance().initWithContext(ChatHelper.getHost(serviceURL));

        // token factory is the preferred authentication method (service credentials are not distributed in the client app)
        if (tokenFactoryURL.equals(tokenFactoryURL) == false) {
            TextToSpeech.sharedInstance().setTokenProvider(new MyTokenProvider(tokenFactoryURL));
        }
        // Basic Authentication
        else if (username.equals(password) == false) {
            TextToSpeech.sharedInstance().setCredentials(username, password);
        } else {
            // no authentication method available
            return false;
        }

        TextToSpeech.sharedInstance().setVoice(getString(R.string.voiceDefault));

        return true;
    }

    protected void setText() {

        Typeface roboto = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "font/Roboto-Bold.ttf");
        Typeface notosans = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "font/NotoSans-Regular.ttf");

        TextView viewTitle = (TextView)mView.findViewById(R.id.title);
        String strTitle = getString(R.string.ttsTitle);
        SpannableString spannable = new SpannableString(strTitle);
        spannable.setSpan(new AbsoluteSizeSpan(47), 0, strTitle.length(), 0);
        spannable.setSpan(new CustomTypefaceSpan("", roboto), 0, strTitle.length(), 0);
        viewTitle.setText(spannable);
        viewTitle.setTextColor(0xFF325C80);

        TextView viewInstructions = (TextView)mView.findViewById(R.id.instructions);
        String strInstructions = getString(R.string.ttsInstructions);
        SpannableString spannable2 = new SpannableString(strInstructions);
        spannable2.setSpan(new AbsoluteSizeSpan(20), 0, strInstructions.length(), 0);
        spannable2.setSpan(new CustomTypefaceSpan("", notosans), 0, strInstructions.length(), 0);
        viewInstructions.setText(spannable2);
        viewInstructions.setTextColor(0xFF121212);
    }

    public class ItemVoice {

        public JSONObject mObject = null;

        public ItemVoice(JSONObject object) {
            mObject = object;
        }

        public String toString() {
            try {
                return mObject.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void addItemsOnSpinnerVoices() {

        Spinner spinner = (Spinner)mView.findViewById(R.id.spinnerVoices);
        int iIndexDefault = 0;

        JSONObject obj = jsonVoices;
        ItemVoice [] items = null;
        try {
            JSONArray voices = obj.getJSONArray("voices");
            items = new ItemVoice[voices.length()];
            for (int i = 0; i < voices.length(); ++i) {
                items[i] = new ItemVoice(voices.getJSONObject(i));
                if (voices.getJSONObject(i).getString("name").equals(getString(R.string.voiceDefault))) {
                    iIndexDefault = i;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (items != null) {
            ArrayAdapter<ItemVoice> spinnerArrayAdapter = new ArrayAdapter<ItemVoice>(getActivity(), android.R.layout.simple_spinner_item, items);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(iIndexDefault);
        }
    }

    // return the selected voice
    public String getSelectedVoice() {

        // return the selected voice
        Spinner spinner = (Spinner)mView.findViewById(R.id.spinnerVoices);
        ItemVoice item = (ItemVoice)spinner.getSelectedItem();
        String strVoice = null;
        try {
            strVoice = item.mObject.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strVoice;
    }

    // update the prompt for the selected voice
    public void updatePrompt(final String strVoice) {

        TextView viewPrompt = (TextView)mView.findViewById(R.id.prompt);
        if (strVoice.startsWith("en-US") || strVoice.startsWith("en-GB")) {
            viewPrompt.setText(getString(R.string.ttsEnglishPrompt));
        } else if (strVoice.startsWith("es-ES")) {
            viewPrompt.setText(getString(R.string.ttsSpanishPrompt));
        } else if (strVoice.startsWith("fr-FR")) {
            viewPrompt.setText(getString(R.string.ttsFrenchPrompt));
        } else if (strVoice.startsWith("it-IT")) {
            viewPrompt.setText(getString(R.string.ttsItalianPrompt));
        } else if (strVoice.startsWith("de-DE")) {
            viewPrompt.setText(getString(R.string.ttsGermanPrompt));
        } else if (strVoice.startsWith("ja-JP")) {
            viewPrompt.setText(getString(R.string.ttsJapanesePrompt));
        }
    }

    public static class TTSCommands extends AsyncTask<Void, Void, JSONObject>
    {

        public JSONObject doInBackground(Void... none) {

            return TextToSpeech.sharedInstance().getVoices();
        }
    }

}