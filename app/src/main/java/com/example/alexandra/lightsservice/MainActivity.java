package com.example.alexandra.lightsservice;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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


public class MainActivity extends Activity {
    private EditText ipText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipText = (EditText) findViewById(R.id.editIPAddress);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendGreen(View view) {
        String greenJSON = "{\"lights\": [{\"lightId\": 1, \"red\":0,\"green\":255,\"blue\":0, \"intensity\": 0.5}],\"propagate\": true}";

        String ip = ipText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new SendPOSTTask().execute(ip,greenJSON);
        } else {
            //textView.setText("No network connection available.");
        }
    }

    public void sendBlue(View view) {
        String blueJSON = "{\"lights\": [{\"lightId\": 1, \"red\":0,\"green\":0,\"blue\":255, \"intensity\": 0.5}],\"propagate\": true}";

        String ip = ipText.getText().toString();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new SendPOSTTask().execute(ip,blueJSON);
        } else {
            //textView.setText("No network connection available.");
        }

    }

    public void sendRed(View view) {
        String redJSON = "{\"lights\": [{\"lightId\": 1, \"red\":255,\"green\":0,\"blue\":0, \"intensity\": 0.5}],\"propagate\": true}";

        String ip = ipText.getText().toString();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new SendPOSTTask().execute(ip,redJSON);
        } else {
            //textView.setText("No network connection available.");
        }
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
