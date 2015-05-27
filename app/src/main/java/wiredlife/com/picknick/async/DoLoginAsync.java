package wiredlife.com.picknick.async;

import android.os.AsyncTask;

import java.io.IOException;

import wiredlife.com.picknick.utility.YggdrasilAPI;

public class DoLoginAsync extends AsyncTask<String, Integer, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
        String username = params[0];
        String password = params[1];

        YggdrasilAPI api = new YggdrasilAPI();
        try {
            return api.login(username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
