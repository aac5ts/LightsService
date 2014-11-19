package com.example.alexandra.lightsservice;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
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
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
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
import java.text.DecimalFormat;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.provider.CalendarContract;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.database.Cursor;


import static android.system.Os.write;


public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Cursor> {
    //private EditText ipText;
    private TextView longitudeText;
    private TextView latitudeText;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private SimpleCursorAdapter mAdapter;

    DecimalFormat latLong = new DecimalFormat("#0.0000000");

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*EVENT CALENDAR QUERY VARIABLES*/
    private static final String DEBUG_TAG = "MyActivity";
    public static final String[] INSTANCE_PROJECTION = new String[] {
            CalendarContract.Instances._ID,
            CalendarContract.Instances.EVENT_ID,      // 0
            CalendarContract.Instances.BEGIN,         // 1
            CalendarContract.Instances.TITLE          // 2
    };


    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_BEGIN_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ipText = (EditText) findViewById(R.id.editIPAddress);
        longitudeText = (TextView) findViewById(R.id.longitudeTextView);
        latitudeText = (TextView) findViewById(R.id.latitudeTextView);

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);

        ListView eventsListView = (ListView)findViewById(R.id.eventsListView);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {CalendarContract.Instances.TITLE, CalendarContract.Instances.BEGIN};
        int[] toViews = {android.R.id.text1, android.R.id.text2}; // The TextView in simple_list_item_1

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null,
                fromColumns, toViews, 0);

        mAdapter.setViewBinder(new MyViewBinder());

        eventsListView.setAdapter(mAdapter);

        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                Toast.makeText(getApplicationContext(), tv.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);


    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Calendar beginTime = Calendar.getInstance(); //begin time is current time
        long startMillis = beginTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.set(beginTime.get(beginTime.YEAR), beginTime.get(beginTime.MONTH), beginTime.get(beginTime.DAY_OF_MONTH), 23, 59); //set end time to today at 11:59PM
        long endMillis = endTime.getTimeInMillis();

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        String selection = CalendarContract.Instances.BEGIN ;

        Log.i(DEBUG_TAG, "URI: " + builder.build());

        return new CursorLoader(this, builder.build(),
                INSTANCE_PROJECTION, selection, null, CalendarContract.Instances.BEGIN);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)

        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
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

        latitudeText.setText("Latitude: " + latLong.format(mCurrentLocation.getLatitude()));
        longitudeText.setText("Longitude: " + latLong.format(mCurrentLocation.getLongitude()));

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

    public Cursor getEventData(){
        // Specify the date range you want to search for recurring
        // event instances
        Calendar beginTime = Calendar.getInstance(); //begin time is current time
        Log.i(DEBUG_TAG, "NOW: " + beginTime.getTime());
        Log.i(DEBUG_TAG, "NOW YEAR: " + beginTime.get(beginTime.YEAR) );
        Log.i(DEBUG_TAG, "NOW MONTH: " + beginTime.get(beginTime.MONTH));
        Log.i(DEBUG_TAG, "NOW DAY: " + beginTime.get(beginTime.DAY_OF_MONTH));
        Log.i(DEBUG_TAG, "NOW HOUR: " + beginTime.get(beginTime.HOUR_OF_DAY));
        long startMillis = beginTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.set(beginTime.get(beginTime.YEAR), beginTime.get(beginTime.MONTH), beginTime.get(beginTime.DAY_OF_MONTH), 23, 59); //set end time to today at 11:59PM
        Log.i(DEBUG_TAG, "END: " + endTime.getTime());
        long endMillis = endTime.getTimeInMillis();

        Cursor cur = null;
        ContentResolver cr = getContentResolver();

        // The ID of the recurring event whose instances you are searching
        // for in the Instances table
        String selection = CalendarContract.Instances.EVENT_ID ; //+ " = ?";
        //String[] selectionArgs = new String[] {"207"};

        // Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

    // Submit the query
        cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                null,
                null);
        /*
        while (cur.moveToNext()) {
            Log.i(DEBUG_TAG, "IN WHILE");
            String title = null;
            long eventID = 0;
            long beginVal = 0;

            // Get the field values
            eventID = cur.getLong(PROJECTION_ID_INDEX);
            beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
            title = cur.getString(PROJECTION_TITLE_INDEX);

            // Do something with the values.
            Log.i(DEBUG_TAG, "Event:  " + title);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(beginVal);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy  H:m");
            Log.i(DEBUG_TAG, "Date: " + formatter.format(calendar.getTime()));
        }*/

        return cur;
   }

}
