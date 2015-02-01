package com.becmartin.fitfund;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;


public class MainActivity extends ActionBarActivity implements GPSCallback{
    private final static double[] multipliers = {
            1.0,1.0936133,0.001,0.000621371192
    };

    private final static String[] unitstrings = {
            "m","y","km","mi"
    };

    private GPSManager gpsManager = null;
    private int unitindex = 0;
    private double currentLon = 0;
    private double distance = 0;
    private double currentLat = 0;
    private double startLon = 0;
    private double startLat = 0;
    private boolean isMeasuring = false;
    private TextView mInfoText;
    private TextView mDistanceText;
    private SharedPreferences mPreferences;
    private Button mButton;
    private Double totalDistance;

    private static final String TASKS_URL = "http://172.16.20.147:8000/campaign/readings/";
    public String TAG = this.getClass().getSimpleName();

    private AsyncHttpClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner = (Spinner) findViewById(R.id.unitspinner);
        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(2);
        spinner.setOnItemSelectedListener(onMeasurementUnitClicked);

        gpsManager = new GPSManager();

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        gpsManager.startListening(this);
        gpsManager.setGPSCallback(this);

        mInfoText = (TextView)findViewById(R.id.info);
        mDistanceText = (TextView)findViewById(R.id.distance);

        mButton = (Button)findViewById(R.id.measuring);
        mButton.setOnClickListener(onButtonClicked);

        //enableButtons(false);
    }

    @Override
    public void onGPSUpdate(Location location)
    {
        currentLon = location.getLongitude();
        currentLat = location.getLatitude();

        if(isMeasuring){
            updateMeasurement();
        }
    }

    @Override
    protected void onDestroy() {
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);

        gpsManager = null;

        super.onDestroy();
    }

    private void startMeasuring(){
        isMeasuring = true;
        startLon = currentLon;
        startLat = currentLat;

        mInfoText.setText(getString(R.string.measuring_info));

       // enableButtons(true);

        updateMeasurement();
    }

    private void stopMeasuring(){
        isMeasuring = false;
        startLon = 0.0;
        startLat = 0.0;
        setProgressBarIndeterminateVisibility(true);

        mInfoText.setText(getString(R.string.start_measuring_info));
        createNewReading(totalDistance);

       // enableButtons(false);
    }

    private void updateMeasurement(){
        distance = distance + calcGeoDistance(startLat,startLon,currentLat,currentLon) * multipliers[unitindex];

        totalDistance = RoundDecimal(distance, 1);
        String distanceText = "" + RoundDecimal(distance,1) + " " + unitstrings[unitindex];

        ((TextView)findViewById(R.id.distance)).setText(distanceText);
    }

    /*private void enableButtons(boolean isMeasuring){
        ((Button)findViewById(R.id.startmeasuring)).setEnabled(isMeasuring ? false : true);
        ((Button)findViewById(R.id.stopmeasuring)).setEnabled(isMeasuring ? true : false);
        ((Button)findViewById(R.id.startmeasuring)).setVisibility(isMeasuring ? View.INVISIBLE : View.VISIBLE);
        ((Button)findViewById(R.id.stopmeasuring)).setVisibility(isMeasuring ? View.VISIBLE : View.INVISIBLE);
        if(isMeasuring){
            mStopWatch.setImageResource(R.drawable.stopwatch_red);
        }else{
            mStopWatch.setImageResource(R.drawable.stopwatch_green);
        }
    }*/

    private double calcGeoDistance(final double lat1, final double lon1, final double lat2, final double lon2)
    {
        double distance = 0.0;

        try
        {
            final float[] results = new float[3];

            Location.distanceBetween(lat1, lon1, lat2, lon2, results);

            distance = results[0];
        }
        catch (final Exception ex)
        {
            distance = 0.0;
        }

        return distance;
    }

    public double RoundDecimal(double value, int decimalPlace)
    {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, 6);

        return bd.doubleValue();
    }

    private AdapterView.OnItemSelectedListener onMeasurementUnitClicked = new AdapterView.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            Log.i("Item", "" + position + ", " + id);

            unitindex = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0)
        {
        }
    };

    private View.OnClickListener onButtonClicked = new View.OnClickListener(){
        @Override
        public void onClick(View v)
        {
            if(isMeasuring){
                stopMeasuring();
                totalDistance = distance;
                mButton.setText("Start Measuring");

            }else{
                startMeasuring();
                mButton.setText("Stop Measuring");
            }

        }
    };

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
        switch(id){
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, CampaignSettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                Intent intent2 = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent2);
                break;
            case R.id.share_action:
                //need to build the image here.
                //Photo meme = (Photo) getListAdapter().getItem(mSelectedItem);
                //Bitmap bitmap = createMeme(meme);


                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                //shareIntent.putExtra(Intent.EXTRA_STREAM, uriForShare);
                //shareIntent.setType("image/jpeg");
                //startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
                break;
        }


        return super.onOptionsItemSelected(item);
    }



    private void createNewReading(Double totalDistance) {
        client = new AsyncHttpClient();


        // create the JSONobject
        JSONObject jsonReading = new JSONObject();

        try {

            jsonReading.put("distance", totalDistance);
        } catch (JSONException e) {
            e.printStackTrace();
        }



        StringEntity se = null;
        try {
            se = new StringEntity(jsonReading.toString());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            Log.d("JSON", jsonReading.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        client.addHeader("Authorization", "Token " + mPreferences.getString("token", ""));
        client.post(null, TASKS_URL, se, "application/json", new TextHttpResponseHandler() {


            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                Log.d("Failure", s);
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                Log.d("HTTP", "onSuccess: " + s.toString());
                try {
                    if (s != null) {
                        // everything is ok

                        Toast.makeText(MainActivity.this, "Reading Submitted", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    // something went wrong: show a Toast
                    // with the exception message
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } finally {
                    super.onFinish();
                }

            }

        });


    }
}

