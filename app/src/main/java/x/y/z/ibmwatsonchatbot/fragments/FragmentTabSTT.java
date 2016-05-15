package x.y.z.ibmwatsonchatbot.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.android.speech_to_text.v1.ISpeechDelegate;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.dto.SpeechConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import x.y.z.ibmwatsonchatbot.CustomTypefaceSpan;
import x.y.z.ibmwatsonchatbot.R;
import x.y.z.ibmwatsonchatbot.providers.MyTokenProvider;

public class FragmentTabSTT extends Fragment implements ISpeechDelegate
{
    private static final String TAG = "FragmentTabSTT";

    // session recognition results
    private static String mRecognitionResults = "";

    private enum ConnectionState {
        IDLE, CONNECTING, CONNECTED
    }

    ConnectionState mState = ConnectionState.IDLE;
    public View mView = null;
    public Context mContext = null;
    public JSONObject jsonModels = null;
    private Handler mHandler = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.tab_stt, container, false);
        mContext = getActivity().getApplicationContext();
        mHandler = new Handler();

        setText();
        if (initSTT() == false) {
            displayResult("Error: no authentication credentials/token available, please enter your authentication information");
            return mView;
        }

        if (jsonModels == null) {
            jsonModels = new STTCommands().doInBackground();
            if (jsonModels == null) {
                displayResult("Please, check internet connection.");
                return mView;
            }
        }
        addItemsOnSpinnerModels();

        displayStatus("please, press the button to start speaking");

        Button buttonRecord = (Button)mView.findViewById(R.id.buttonRecord);
        buttonRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (mState == ConnectionState.IDLE) {
                    mState = ConnectionState.CONNECTING;
                    Log.d(TAG, "onClickRecord: IDLE -> CONNECTING");
                    Spinner spinner = (Spinner)mView.findViewById(R.id.spinnerModels);
                    spinner.setEnabled(false);
                    mRecognitionResults = "";
                    displayResult(mRecognitionResults);
                    ItemModel item = (ItemModel)spinner.getSelectedItem();
                    SpeechToText.sharedInstance().setModel(item.getModelName());
                    displayStatus("connecting to the STT service...");
                    // start recognition
                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... none) {
                            SpeechToText.sharedInstance().recognize();
                            return null;
                        }
                    }.execute();
                    setButtonLabel(R.id.buttonRecord, "Connecting...");
                    setButtonState(true);
                }
                else if (mState == ConnectionState.CONNECTED) {
                    mState = ConnectionState.IDLE;
                    Log.d(TAG, "onClickRecord: CONNECTED -> IDLE");
                    Spinner spinner = (Spinner)mView.findViewById(R.id.spinnerModels);
                    spinner.setEnabled(true);
                    SpeechToText.sharedInstance().stopRecognition();
                    setButtonState(false);
                }
            }
        });

        return mView;
    }

    private String getModelSelected() {

        Spinner spinner = (Spinner)mView.findViewById(R.id.spinnerModels);
        ItemModel item = (ItemModel)spinner.getSelectedItem();
        return item.getModelName();
    }

    public URI getHost(String url){
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    // initialize the connection to the Watson STT service
    private boolean initSTT() {

        // DISCLAIMER: please enter your credentials or token factory in the lines below
        String username = "XXX";
        String password = "XXX";

        String tokenFactoryURL = "https://stream.watsonplatform.net/speech-to-text/api";
        String serviceURL = "wss://stream.watsonplatform.net/speech-to-text/api";

        SpeechConfiguration sConfig = new SpeechConfiguration(SpeechConfiguration.AUDIO_FORMAT_OGGOPUS);
        //SpeechConfiguration sConfig = new SpeechConfiguration(SpeechConfiguration.AUDIO_FORMAT_DEFAULT);

        SpeechToText.sharedInstance().initWithContext(this.getHost(serviceURL), getActivity().getApplicationContext(), sConfig);

        // token factory is the preferred authentication method (service credentials are not distributed in the client app)
        if (tokenFactoryURL.equals(tokenFactoryURL) == false) {
            SpeechToText.sharedInstance().setTokenProvider(new MyTokenProvider(tokenFactoryURL));
        }
        // Basic Authentication
        else if (username.equals(password) == false) {
            SpeechToText.sharedInstance().setCredentials(username, password);
        } else {
            // no authentication method available
            return false;
        }

        SpeechToText.sharedInstance().setModel(getString(R.string.modelDefault));
        SpeechToText.sharedInstance().setDelegate(this);

        return true;
    }

    protected void setText() {

        Typeface roboto = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "font/Roboto-Bold.ttf");
        Typeface notosans = Typeface.createFromAsset(getActivity().getApplicationContext().getAssets(), "font/NotoSans-Regular.ttf");

        // title
        TextView viewTitle = (TextView)mView.findViewById(R.id.title);
        String strTitle = getString(R.string.sttTitle);
        SpannableStringBuilder spannable = new SpannableStringBuilder(strTitle);
        spannable.setSpan(new AbsoluteSizeSpan(47), 0, strTitle.length(), 0);
        spannable.setSpan(new CustomTypefaceSpan("", roboto), 0, strTitle.length(), 0);
        viewTitle.setText(spannable);
        viewTitle.setTextColor(0xFF325C80);

        // instructions
        TextView viewInstructions = (TextView)mView.findViewById(R.id.instructions);
        String strInstructions = getString(R.string.sttInstructions);
        SpannableString spannable2 = new SpannableString(strInstructions);
        spannable2.setSpan(new AbsoluteSizeSpan(20), 0, strInstructions.length(), 0);
        spannable2.setSpan(new CustomTypefaceSpan("", notosans), 0, strInstructions.length(), 0);
        viewInstructions.setText(spannable2);
        viewInstructions.setTextColor(0xFF121212);
    }

    public class ItemModel {

        private JSONObject mObject = null;

        public ItemModel(JSONObject object) {
            mObject = object;
        }

        public String toString() {
            try {
                return mObject.getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        public String getModelName() {
            try {
                return mObject.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    protected void addItemsOnSpinnerModels() {

        Spinner spinner = (Spinner)mView.findViewById(R.id.spinnerModels);
        int iIndexDefault = 0;

        JSONObject obj = jsonModels;
        ItemModel [] items = null;
        try {
            JSONArray models = obj.getJSONArray("models");

            // count the number of Broadband models (narrowband models will be ignored since they are for telephony data)
            Vector<Integer> v = new Vector<>();
            for (int i = 0; i < models.length(); ++i) {
                if (models.getJSONObject(i).getString("name").indexOf("Broadband") != -1) {
                    v.add(i);
                }
            }
            items = new ItemModel[v.size()];
            int iItems = 0;
            for (int i = 0; i < v.size() ; ++i) {
                items[iItems] = new ItemModel(models.getJSONObject(v.elementAt(i)));
                if (models.getJSONObject(v.elementAt(i)).getString("name").equals(getString(R.string.modelDefault))) {
                    iIndexDefault = iItems;
                }
                ++iItems;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (items != null) {
            ArrayAdapter<ItemModel> spinnerArrayAdapter = new ArrayAdapter<ItemModel>(getActivity(), android.R.layout.simple_spinner_item, items);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(iIndexDefault);
        }
    }

    public void displayResult(final String result) {
        final Runnable runnableUi = new Runnable(){
            @Override
            public void run() {
                TextView textResult = (TextView)mView.findViewById(R.id.textResult);
                textResult.setText(result);
            }
        };

        new Thread(){
            public void run(){
                mHandler.post(runnableUi);
            }
        }.start();
    }

    public void displayStatus(final String status) {
            /*final Runnable runnableUi = new Runnable(){
                @Override
                public void run() {
                    TextView textResult = (TextView)mView.findViewById(R.id.sttStatus);
                    textResult.setText(status);
                }
            };
            new Thread(){
                public void run(){
                    mHandler.post(runnableUi);
                }
            }.start();*/
    }

    /**
     * Change the button's label
     */
    public void setButtonLabel(final int buttonId, final String label) {
        final Runnable runnableUi = new Runnable(){
            @Override
            public void run() {
                Button button = (Button)mView.findViewById(buttonId);
                button.setText(label);
            }
        };
        new Thread(){
            public void run(){
                mHandler.post(runnableUi);
            }
        }.start();
    }

    /**
     * Change the button's drawable
     */
    public void setButtonState(final boolean bRecording) {

        final Runnable runnableUi = new Runnable(){
            @Override
            public void run() {
                int iDrawable = bRecording ? R.drawable.button_record_stop : R.drawable.button_record_start;
                Button btnRecord = (Button)mView.findViewById(R.id.buttonRecord);
                btnRecord.setBackground(getResources().getDrawable(iDrawable));
            }
        };
        new Thread(){
            public void run(){
                mHandler.post(runnableUi);
            }
        }.start();
    }

    // delegages ----------------------------------------------

    public void onOpen() {
        Log.d(TAG, "onOpen");
        displayStatus("successfully connected to the STT service");
        setButtonLabel(R.id.buttonRecord, "Stop recording");
        mState = ConnectionState.CONNECTED;
    }

    public void onError(String error) {

        Log.e(TAG, error);
        displayResult(error);
        mState = ConnectionState.IDLE;
    }

    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "onClose, code: " + code + " reason: " + reason);
        displayStatus("connection closed");
        setButtonLabel(R.id.buttonRecord, "Record");
        mState = ConnectionState.IDLE;
    }

    public void onMessage(String message) {

        Log.d(TAG, "onMessage, message: " + message);
        try {
            JSONObject jObj = new JSONObject(message);
            // state message
            if(jObj.has("state")) {
                Log.d(TAG, "Status message: " + jObj.getString("state"));
            }
            // results message
            else if (jObj.has("results")) {
                //if has result
                Log.d(TAG, "Results message: ");
                JSONArray jArr = jObj.getJSONArray("results");
                for (int i=0; i < jArr.length(); i++) {
                    JSONObject obj = jArr.getJSONObject(i);
                    JSONArray jArr1 = obj.getJSONArray("alternatives");
                    String str = jArr1.getJSONObject(0).getString("transcript");
                    // remove whitespaces if the language requires it
                    String model = this.getModelSelected();
                    if (model.startsWith("ja-JP") || model.startsWith("zh-CN")) {
                        str = str.replaceAll("\\s+","");
                    }
                    String strFormatted = Character.toUpperCase(str.charAt(0)) + str.substring(1);
                    if (obj.getString("final").equals("true")) {
                        String stopMarker = (model.startsWith("ja-JP") || model.startsWith("zh-CN")) ? "。" : ". ";
                        mRecognitionResults += strFormatted.substring(0,strFormatted.length()-1) + stopMarker;
                        displayResult(mRecognitionResults);
                    } else {
                        displayResult(mRecognitionResults + strFormatted);
                    }
                    break;
                }
            } else {
                displayResult("unexpected data coming from stt server: \n" + message);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
        }
    }

    public void onAmplitude(double amplitude, double volume) {
        //Logger.e(TAG, "amplitude=" + amplitude + ", volume=" + volume);
    }

    public static class STTCommands extends AsyncTask<Void, Void, JSONObject> {

        public JSONObject doInBackground(Void... none) {

            return SpeechToText.sharedInstance().getModels();
        }
    }

}


