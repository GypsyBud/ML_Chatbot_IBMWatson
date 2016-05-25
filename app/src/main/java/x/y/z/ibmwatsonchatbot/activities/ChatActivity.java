package x.y.z.ibmwatsonchatbot.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.android.speech_to_text.v1.ISpeechDelegate;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.dto.SpeechConfiguration;

import x.y.z.ibmwatsonchatbot.R;
import x.y.z.ibmwatsonchatbot.providers.MyTokenProvider;
import x.y.z.ibmwatsonchatbot.util.ChatHelper;

/**
 * Created by timmanas on 2016-05-18.
 */
public class ChatActivity extends Activity implements ISpeechDelegate
{

    private static final String TAG = "ChatActivity";

    ImageButton recordButton;
    TextView speechText;

    private enum ConnectionState {
        IDLE, CONNECTING, CONNECTED
    }

    ConnectionState mState = ConnectionState.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity_v2);
        setupButtons();

        if(init_WatsonService_Speech2Text() == false){
            Log.d(TAG, "Incorrect Credentials for Speech To Text");
            return;
        }


    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onOpen()
    {
        Log.d(TAG, "");
    }

    @Override
    public void onError(String error)
    {

    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {

    }

    @Override
    public void onMessage(String message)
    {

    }

    @Override
    public void onAmplitude(double amplitude, double volume)
    {

    }

    public Context getContext(){
        return this;
    }

    private void setupButtons()
    {
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        speechText = (TextView) findViewById(R.id.processedText);
    }

    private boolean init_WatsonService_Speech2Text()
    {
        String username = "98409437-992c-4a0e-bb68-3f2f2b664995";
        String password = "Cgrd3554gOPY";

        String tokenFactoryURL = "https://stream.watsonplatform.net/speech-to-text/api";
        String serviceURL = "wss://stream.watsonplatform.net/speech-to-text/api";

        SpeechConfiguration sConfig = new SpeechConfiguration(SpeechConfiguration.AUDIO_FORMAT_OGGOPUS);
        SpeechToText.sharedInstance().initWithContext(ChatHelper.getHost(serviceURL), getContext(), sConfig);

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

}


