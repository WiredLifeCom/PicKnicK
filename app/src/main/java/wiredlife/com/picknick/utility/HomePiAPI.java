package wiredlife.com.picknick.utility;

import android.util.Log;

import com.wiredlife.jsonformatjava.model.unload.Unload;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Daniel on 2015-05-27.
 */
public class HomePiAPI {

    private String address;

    public HomePiAPI(String address) {
        this.address = address;
    }

    public String sendUnload(Unload unload) throws IOException {
        Log.i("SendUnload", "Trying to send Unload...");

        URL url = new URL(address);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(Unload.toJson(unload));
        wr.flush();
        wr.close();

        StringBuilder builder = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        String line;
        while ((line = br.readLine()) != null) {
            builder.append(line);
        }

        Log.i("Response", builder.toString());

        br.close();

        connection.disconnect();

        return builder.toString();
    }
}
