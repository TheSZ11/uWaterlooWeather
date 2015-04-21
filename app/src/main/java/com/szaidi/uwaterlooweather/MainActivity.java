package com.szaidi.uwaterlooweather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    Context context;
    String feedUrl = "https://api.uwaterloo.ca/v2/weather/current.json";

    String temp;
    String observationTime;
    String windchill;
    String hourMaxTemp;
    String hourMinTemp;
    String windSpeed;
    String windDirection;
    String observationDate;
    String date;
    String Time;


    TextView textView;
    TextView textViewObservationDate;
    TextView textViewObservationTime;
    TextView textViewWindchill;
    TextView textViewHourMaxTemp;
    TextView textViewHourMinTemp;
    TextView textViewWindSpeed;
    TextView textViewWindDirection;

    SwipeRefreshLayout swipeLayout;
    WeatherListTask loaderTask;

    int statusCode;

    private ProgressBar spinner;


    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        context = this;

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        if (!isInternetAvailable()) {
            alert.setTitle("Connectivity Issues");
            alert.setMessage("Your feed could not be updated at this time, please check your internet connection and try again.");
            alert.setPositiveButton("Okay",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alert.create();
            alertDialog.show();
        }

        loaderTask = new WeatherListTask();
        loaderTask.execute();

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setColorSchemeColors(Color.BLACK, Color.YELLOW);

        swipeLayout.setOnRefreshListener(onRefreshListener);

        textView = (TextView) findViewById(R.id.textView);
        textViewObservationDate = (TextView) findViewById(R.id.textViewObservationDate);
        textViewObservationTime = (TextView) findViewById(R.id.textViewObservationTime);
        textViewWindchill = (TextView) findViewById(R.id.textViewWindchill);
        textViewHourMaxTemp = (TextView) findViewById(R.id.textViewHourMaxTemp);
        textViewHourMinTemp = (TextView) findViewById(R.id.textViewHourMinTemp);
        textViewWindSpeed = (TextView) findViewById(R.id.textViewWindSpeed);
        textViewWindDirection = (TextView) findViewById(R.id.textViewWindDirection);


    }

    OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {

            AlertDialog.Builder alert = new AlertDialog.Builder(context);

            if (!isInternetAvailable()) {
                alert.setTitle("Connectivity Issues");
                alert.setMessage("Your feed could not be updated at this time, please check your internet connection and try again.");
                alert.setPositiveButton("Okay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }

            WeatherListTask refreshTask = new WeatherListTask();
            refreshTask.execute();
            swipeLayout.setRefreshing(false);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public class WeatherListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            spinner = (ProgressBar) findViewById(R.id.progressbar);
            //spinner.getIndeterminateDrawable().setColorFilter(0xFFcc0000, PorterDuff.Mode.MULTIPLY);
            spinner.setVisibility(View.VISIBLE);

            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            HttpClient client = new DefaultHttpClient();
            HttpGet getRequest = new HttpGet(feedUrl);

            try {
                HttpResponse responce = client.execute(getRequest);
                StatusLine statusLine = responce.getStatusLine();
                statusCode = statusLine.getStatusCode();

                InputStream jsonStream = responce.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(jsonStream));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {

                    builder.append(line);
                }

                String jsonData = builder.toString();

                JSONObject json = new JSONObject(jsonData);
                JSONObject weather = json.getJSONObject("data");
                temp = weather.getString("temperature_current_c");
                observationTime = weather.getString("observation_time");
                windchill = weather.getString("windchill_c");
                hourMaxTemp = weather.getString("temperature_24hr_max_c");
                hourMinTemp = weather.getString("temperature_24hr_min_c");
                windSpeed = weather.getString("wind_speed_kph");
                windDirection = weather.getString("wind_direction_degrees");


                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy");
                try {
                    Date Date = input.parse(observationTime);                 // parse input
                    date = (output.format(Date));    // format output
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
                Date time = new Date();
                Time = fmt.format(time);

                temp = temp + "°c";
                hourMaxTemp = "high of " + hourMaxTemp + "°c";
                hourMinTemp = "low of " + hourMinTemp + "°c";

                if (windchill == "null") {
                    windchill = "feels like " + temp;
                } else {
                    windchill = "feels like " + windchill + "°c";
                }

                windSpeed = windSpeed + " km/h";

                int windDirectionInt = Integer.valueOf(windDirection);

                if (348.75 <= windDirectionInt && windDirectionInt <= 360) {
                    windDirection = "N";
                } else if (0 <= windDirectionInt && windDirectionInt <= 11.25) {
                    windDirection = "N";
                } else if (11.25 < windDirectionInt && windDirectionInt <= 33.75) {
                    windDirection = "NNE";
                } else if (33.75 < windDirectionInt && windDirectionInt <= 56.25) {
                    windDirection = "NE";
                } else if (56.25 < windDirectionInt && windDirectionInt <= 78.75) {
                    windDirection = "ENE";
                } else if (78.75 < windDirectionInt && windDirectionInt <= 101.25) {
                    windDirection = "E";
                } else if (101.25 < windDirectionInt && windDirectionInt <= 123.75) {
                    windDirection = "ESE";
                } else if (123.75 < windDirectionInt && windDirectionInt <= 146.25) {
                    windDirection = "SE";
                } else if (146.25 < windDirectionInt && windDirectionInt <= 168.75) {
                    windDirection = "SSE";
                } else if (168.75 < windDirectionInt && windDirectionInt <= 191.25) {
                    windDirection = "S";
                } else if (191.25 < windDirectionInt && windDirectionInt <= 213.75) {
                    windDirection = "SSW";
                } else if (213.75 < windDirectionInt && windDirectionInt <= 236.25) {
                    windDirection = "SW";
                } else if (236.25 < windDirectionInt && windDirectionInt <= 258.75) {
                    windDirection = "WSW";
                } else if (258.75 < windDirectionInt && windDirectionInt <= 281.25) {
                    windDirection = "W";
                } else if (281.25 < windDirectionInt && windDirectionInt <= 303.75) {
                    windDirection = "WNW";
                } else if (303.75 < windDirectionInt && windDirectionInt <= 326.25) {
                    windDirection = "NW";
                } else if (326.25 < windDirectionInt && windDirectionInt < 348.75) {
                    windDirection = "NNW";
                } else {
                    windDirection = "-";
                }

            } catch (ClientProtocolException e) {

                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            spinner.setVisibility(View.GONE);

            super.onPostExecute(result);

            textView.setText(date);
            textViewObservationDate.setText(Time);
            textViewObservationTime.setText(hourMaxTemp);
            textViewWindchill.setText(temp);
            textViewHourMaxTemp.setText(hourMinTemp);
            textViewHourMinTemp.setText(windchill);
            textViewWindSpeed.setText(windSpeed);
            textViewWindDirection.setText(windDirection);
        }

    }

}
