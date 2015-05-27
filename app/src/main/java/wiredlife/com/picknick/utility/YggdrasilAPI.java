package wiredlife.com.picknick.utility;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class YggdrasilAPI {

    public boolean login(String username, String password) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"agent\": {");                             // defaults to Minecraft
        builder.append("\"name\": \"Minecraft\",");                 // For Mojang's other game Scrolls, "Scrolls" should be used
        builder.append("\"version\": 1");                         // This number might be increased by the vanilla client in the future
        builder.append("},");
        builder.append("\"username\": \"" + username + "\",");     // Can be an email address or player name for unmigrated accounts
        builder.append("\"password\": \"" + password + "\"");
        builder.append("}");

        URL url = new URL("https://authserver.mojang.com/authenticate");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(builder.toString());
        wr.flush();
        wr.close();

        Log.i("Code", String.valueOf(connection.getResponseCode()));

        if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
            return true;
        } else {
            return false;
        }
    }

}
