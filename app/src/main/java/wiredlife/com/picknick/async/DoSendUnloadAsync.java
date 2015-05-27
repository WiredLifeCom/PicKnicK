package wiredlife.com.picknick.async;

import android.os.AsyncTask;
import android.util.Log;

import com.wiredlife.jsonformatjava.model.unload.Unload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import wiredlife.com.picknick.utility.HomePiAPI;

/**
 * Created by Daniel on 2015-05-25.
 */
public class DoSendUnloadAsync extends AsyncTask<Unload, Integer, String> {

    private String address;

    public DoSendUnloadAsync(String address) {
        this.address = address;
    }

    @Override
    protected String doInBackground(Unload... params) {
        Log.i("UnloadToSend", Unload.toJson(params[0]));

        HomePiAPI api = new HomePiAPI(address);
        try {
            return api.sendUnload(params[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
