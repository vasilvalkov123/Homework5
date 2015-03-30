package org.hackafe.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy
                .Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences sharedpreferences = this.getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        String data;
        data = getForecast(sharedpreferences.getString("text", "Sofia"));
        List<Forecast> forecast = parseForecast(data);


        final ForecastAdapter adapter = new ForecastAdapter(inflater, forecast);
        final ListView collection = (ListView) rootView.findViewById(R.id.container);
        collection.setAdapter(adapter);

        collection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                Forecast forecast = (Forecast) adapter.getItem(position);
             String  forecasts = forecast.desc;

                Intent intent = new Intent(getActivity(), DayForecast.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecasts);
                intent.putExtra("TIMESTAMP",mydate);

                startActivity(intent);

            }
        });

        return rootView;
    }

    private List<Forecast> parseForecast(String data) {
        try {
            List<Forecast> forecastList = new ArrayList<>();
            // parse String so we have JSONObject
            JSONObject obj = new JSONObject(data);
            // get "list" field as array
            JSONArray list = obj.getJSONArray("list");
            // iterate array and get forecast
            for (int i=0; i<list.length(); i++) {
                // get "i"th forecast
                JSONObject forecastObj = list.getJSONObject(i);

                // get "temp" object
                JSONObject temp = forecastObj.getJSONObject("temp");
                // extract "day" temperature
                double dayTemp = temp.getDouble("day");

                // get "weather" array
                JSONArray weather = forecastObj.getJSONArray("weather");
                // get 1st weather
                JSONObject weather1 = weather.getJSONObject(0);

                // extract "description" from 1st weather
                String description = weather1.getString("description");

                // extract "dt" date time in unix epoch format
                long dt = forecastObj.getLong("dt");
                String dateStr = SimpleDateFormat.getDateInstance().format(new Date(dt*1000));

                Forecast forecast = new Forecast();

              double  far = dayTemp*1.8+32;

                forecast.desc = String.format("%s - %s   %.1f°C", dateStr, description,  far);
                forecast.timestamp = dt;
                forecastList.add(forecast);

            }
            return forecastList;
        } catch (Throwable t) {
            Log.e("Sunshine", t.getMessage(), t);
            return null;
        }
    }

    private String getForecast(String poo) {
        try {


            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q="+poo+"&mode=json&units=metric&cnt=7");

            InputStream inputStream = url.openStream();
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }

                return total.toString();
            } finally {
                inputStream.close();
            }
        } catch (Throwable t) {
            Log.e("Sunshine", t.getMessage(), t);
            return null;
        }
    }
}
