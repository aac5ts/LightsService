package com.example.alexandra.lightsservice;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.system.Os.write;


public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    //private EditText ipText;
    private TextView locationText;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ipText = (EditText) findViewById(R.id.editIPAddress);
        locationText = (TextView) findViewById(R.id.locationTextView);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
    }

    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clickedGetLocation(View view){
        mCurrentLocation = mLocationClient.getLastLocation();

        locationText.setText(mCurrentLocation.toString());
    }

    public void sendGreen(View view) {
        String greenJSON = "{\"lights\": [{\"lightId\": 1, \"red\":0,\"green\":255,\"blue\":0, \"intensity\": 0.5}],\"propagate\": true}";

        sendPost(greenJSON);
    }

    public void sendBlue(View view) {
        String blueJSON = "{\"lights\": [{\"lightId\": 1, \"red\":0,\"green\":0,\"blue\":255, \"intensity\": 0.5}],\"propagate\": true}";

        sendPost(blueJSON);

    }

    public void sendRed(View view) {
        String redJSON = "{\"lights\": [{\"lightId\": 1, \"red\":255,\"green\":0,\"blue\":0, \"intensity\": 0.5}],\"propagate\": true}";

        sendPost(redJSON);
    }

    public void sendPost(String json){
        //String ip = ipText.getText().toString();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String ip = sharedPref.getString(SettingsActivity.LIGHTS_IP_PREF, "0.0.0.0");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new SendPOSTTask().execute(ip,json);
        } else {
            //textView.setText("No network connection available.");
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private boolean servicesConnected() {

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private class SendPOSTTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            String json = params[1];
            String ip = params[0];
            String full_url = "http://" + ip + "/rpi";
            //String full_url = "http://172.27.99.122/rpi";

            URL url;
            StringBuffer response = new StringBuffer();
            try{
                url = new URL(full_url);
            }catch (MalformedURLException e){
                System.out.println("Bad URL");
                return "";
            }

            HttpURLConnection urlConnection ;
            try{
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
            }catch (IOException e){
                System.out.println("Couldn't connect to URL");
                return "";
            }

            try {

                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setFixedLengthStreamingMode(json.getBytes().length);
                OutputStream outputStream = urlConnection.getOutputStream();
                PrintWriter out = new PrintWriter(outputStream);
                out.print(json);
                out.close();

                try {

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));

                    String inputLine;


                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    System.out.println(response.toString());

                }finally {
                    urlConnection.disconnect();
                }


            }catch (IOException e){
                System.out.println("Couldn't get Output Stream");

                return urlConnection.getErrorStream().toString();
            }

               return response.toString();
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {

            System.out.println(result);
        }
    }

}
