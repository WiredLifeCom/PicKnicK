package wiredlife.com.picknick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wiredlife.jsonformatjava.model.unload.User;

import java.util.concurrent.ExecutionException;

import wiredlife.com.picknick.async.DoLoginAsync;

public class LoginActivity extends Activity {

    EditText edtUsername;
    EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = (EditText)findViewById(R.id.edtUsername);
        edtPassword = (EditText)findViewById(R.id.edtPassword);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void btnLogin_Click(View v) {
        boolean result = false;
        try {
            result = new DoLoginAsync().execute(edtUsername.getText().toString(), edtPassword.getText().toString()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (result) {
            //Toast.makeText(getApplicationContext(), "Valid credentials!", Toast.LENGTH_LONG).show();

            User user = new User(edtUsername.getText().toString());

            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("user", user);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Invalid username and/or password", Toast.LENGTH_LONG).show();
        }
    }
}
