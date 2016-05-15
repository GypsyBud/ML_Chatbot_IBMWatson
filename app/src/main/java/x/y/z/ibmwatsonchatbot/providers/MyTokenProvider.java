package x.y.z.ibmwatsonchatbot.providers;

import android.util.Log;

import com.ibm.watson.developer_cloud.android.speech_common.v1.TokenProvider;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class MyTokenProvider implements TokenProvider
{

    private static final String TAG = "MyTokenProvider";

    String m_strTokenFactoryURL = null;

    public MyTokenProvider(String strTokenFactoryURL) {
        m_strTokenFactoryURL = strTokenFactoryURL;
    }

    public String getToken() {

        Log.d(TAG, "attempting to get a token from: " + m_strTokenFactoryURL);
        try {
            // DISCLAIMER: the application developer should implement an authentication mechanism from the mobile app to the
            // server side app so the token factory in the server only provides tokens to authenticated clients
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(m_strTokenFactoryURL);
            HttpResponse executed = httpClient.execute(httpGet);
            InputStream is = executed.getEntity().getContent();
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            String strToken = writer.toString();
            Log.d(TAG, strToken);
            return strToken;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}