package wiredlife.com.picknick.async;

import android.os.AsyncTask;
import android.util.Log;

import com.wiredlife.jsonformatjava.model.unload.Unload;

import java.io.IOException;

import wiredlife.com.picknick.utility.HomePiAPI;

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
