package x.y.z.ibmwatsonchatbot.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by timmanas on 2016-05-21.
 */
public class ChatHelper
{

    public static URI getHost(String url){
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }



}
