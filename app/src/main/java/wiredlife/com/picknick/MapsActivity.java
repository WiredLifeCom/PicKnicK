package wiredlife.com.picknick;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wiredlife.jsonformatjava.model.unload.Unload;
import com.wiredlife.jsonformatjava.model.unload.User;
import com.wiredlife.jsonformatjava.model.unload.Zone;
import com.wiredlife.jsonformatjava.utility.Lock;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import wiredlife.com.picknick.async.DoMiningProgressAsync;
import wiredlife.com.picknick.async.DoSendUnloadAsync;
import wiredlife.com.picknick.utility.MiningModule;

public class MapsActivity extends FragmentActivity implements LocationListener {

    private Lock lock;

    private List<Zone> zones;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private MiningModule miningModule;
    private Thread miningThread;

    private Thread miningProgressThread;

    private PopupWindow popupWindow;

    private Zone currentZone;

    private DoMiningProgressAsync async;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lock = new Lock();

        zones = new ArrayList<Zone>();

        zones.add(new Zone(55.517099, 13.230408, 100, "Wood", 10));
        zones.add(new Zone(55.516586, 13.230489, 1, "Stone", 10));

        zones.add(new Zone(55.615384, 12.986267, 100, "Stone", 10));
        zones.add(new Zone(55.615718, 12.987341, 25, "Wood", 20));
        zones.add(new Zone(55.6146294, 12.9873182, 50, "Stone", 20));

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, this);

        Intent intent = getIntent();
        User user = (User)intent.getSerializableExtra("user");

        miningModule = new MiningModule(user);

        miningThread = new Thread(miningModule);
        miningThread.start();

        miningProgressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                async = new DoMiningProgressAsync(getApplicationContext(), miningModule);
                async.execute();
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        //showPopup(MapsActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.send:
                showDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText editText= new EditText(MapsActivity.this);
        alert.setMessage("Send to HomePiServer?");
        alert.setTitle("DebugMode");

        alert.setView(editText);

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.i("MapsActivity","Sending to HomePiServer...");

                String address = editText.getText().toString();

                Log.i("Address", address);

                miningModule.leaveZone();

                Unload unload = miningModule.getUnload();
                unload.setUnload(DateTime.now());

                DoSendUnloadAsync async = new DoSendUnloadAsync(address.trim().replace(" ", ""));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, unload);
                } else{
                    async.execute();
                }

                //async.execute(miningModule.getUnload());
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        for (Zone z : zones) {
            int resourceId = getResources().getIdentifier(z.getMaterial().toLowerCase().trim(), "mipmap", getPackageName());
            Log.i("ID", String.valueOf(resourceId));
            mMap.addMarker(new MarkerOptions().position(new LatLng(z.getLatitude(), z.getLongitude())).title(z.getMaterial()).icon(BitmapDescriptorFactory.fromResource(resourceId)));
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("onLocationChanged", "Location changed!");

        // Toast.makeText(getApplicationContext(), "Location changed!", Toast.LENGTH_SHORT).show();

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Location thisLocation = new Location("");
        thisLocation.setLatitude(latitude);
        thisLocation.setLongitude(longitude);

        Log.i("Coordinates", "Lat: " + latitude + ", Long: " + longitude);

        Zone currentZone = miningModule.getCurrentZone();
        if (currentZone.getArrival() != null && currentZone.getDeparture() == null) {
            Location zoneLocation = new Location("");
            zoneLocation.setLatitude(currentZone.getLatitude());
            zoneLocation.setLongitude(currentZone.getLongitude());

            float distance = thisLocation.distanceTo(zoneLocation);

            Log.i("Distance", String.valueOf(distance));

            if (distance > currentZone.getRadius()) {
                miningProgressThread.interrupt();
                Toast.makeText(getApplicationContext(), "Leaving zone!", Toast.LENGTH_SHORT).show();
                miningModule.leaveZone();
                async.setCurrentZone(null);
            }
        }

        for (Zone z : zones) {
            Location zoneLocation = new Location("");
            zoneLocation.setLatitude(z.getLatitude());
            zoneLocation.setLongitude(z.getLongitude());

            float distance = thisLocation.distanceTo(zoneLocation);

            Log.i("Distance", String.valueOf(distance));

            if (z.getLongitude() == currentZone.getLongitude() && z.getLatitude() == currentZone.getLatitude()) {
                continue;
            }

            if (distance < z.getRadius()) {
                miningProgressThread.start();
                Toast.makeText(getApplicationContext(), "Entered zone!", Toast.LENGTH_SHORT).show();
                miningModule.enterZone(z);
                async.setCurrentZone(z);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("onProviderEnabled", "Provider enabled!");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("onProviderDisabled", "Provider disabled!");
    }

    // The method that displays the popup.
    private void showPopup(final Activity context) {
        findViewById(R.id.map).post(new Runnable() {
            public void run() {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.popup_mining, null);

                PopupWindow popup = new PopupWindow(layout, 300, 370, true);
                popup.setBackgroundDrawable(new BitmapDrawable());
                popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        });
    }
}
