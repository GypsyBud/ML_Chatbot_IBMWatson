package x.y.z.ibmwatsonchatbot.activities;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.android.text_to_speech.v1.TextToSpeech;

import org.json.JSONException;

import x.y.z.ibmwatsonchatbot.R;
import x.y.z.ibmwatsonchatbot.fragments.FragmentTabSTT;
import x.y.z.ibmwatsonchatbot.fragments.FragmentTabTTS;

// IBM Watson SDK

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    TextView textTTS;

    ActionBar.Tab tabSTT, tabTTS;
    FragmentTabSTT fragmentTabSTT = new FragmentTabSTT();
    FragmentTabTTS fragmentTabTTS = new FragmentTabTTS();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Strictmode needed to run the http/wss request for devices > Gingerbread
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        setContentView(R.layout.activity_tab_text);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        tabSTT = actionBar.newTab().setText("Speech to Text");
        tabTTS = actionBar.newTab().setText("Text to Speech");

        tabSTT.setTabListener(new MyTabListener(fragmentTabSTT));
        tabTTS.setTabListener(new MyTabListener(fragmentTabTTS));

        actionBar.addTab(tabSTT);
        actionBar.addTab(tabTTS);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void playTTS(View view) throws JSONException {

        TextToSpeech.sharedInstance().setVoice(fragmentTabTTS.getSelectedVoice());
        Log.d(TAG, fragmentTabTTS.getSelectedVoice());

        //Get text from text box
        textTTS = (TextView)fragmentTabTTS.mView.findViewById(R.id.prompt);
        String ttsText=textTTS.getText().toString();
        Log.d(TAG, ttsText);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textTTS.getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);

        //Call the sdk function
        TextToSpeech.sharedInstance().synthesize(ttsText);
    }

    public class MyTabListener implements ActionBar.TabListener {

        Fragment fragment;
        public MyTabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.replace(R.id.fragment_container, fragment);
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // nothing done here
        }
    }


}
