package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout clWeatherlayout;
    private ProgressBar pbProg;
    private TextView tvHeaderCityName, tvTemp, tvCondition;
    private EditText edtSearch;
    private ImageView ivWeatherImg, ivSearchIcon, ivBackground;
    private RecyclerView rvItems;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdaptor weatherRVAdaptor;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FOR FULL SCREEN
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);

        clWeatherlayout = findViewById(R.id.clWeatherlayout);
        pbProg = findViewById(R.id.pbProg);
        tvHeaderCityName = findViewById(R.id.tvHeaderCityName);
        tvTemp = findViewById(R.id.tvTemp);
        tvCondition = findViewById(R.id.tvCondition);
        edtSearch = findViewById(R.id.edtSearch);
        ivWeatherImg = findViewById(R.id.ivWeatherImg);
        rvItems = findViewById(R.id.rvItems);
        ivSearchIcon = findViewById(R.id.ivSearchIcon);
        ivBackground = findViewById(R.id.ivBackground);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdaptor = new WeatherRVAdaptor(this, weatherRVModelArrayList);
        rvItems.setAdapter(weatherRVAdaptor);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);

        ivSearchIcon.setOnClickListener(v -> {
            String city = edtSearch.getText().toString();
            edtSearch.onEditorAction(EditorInfo.IME_ACTION_DONE);
            if (city.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
            } else {
                tvHeaderCityName.setText(cityName);
                getWeatherInfo(city);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.e("TAG", "IF 1");
        if (requestCode == PERMISSION_CODE) {
            //Log.e("TAG", "IF 2");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Log.e("TAG", "IF 3");
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                //Log.e("TAG", "IF 4");
                Toast.makeText(MainActivity.this, "Please Provide Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "NOT FOUND";
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);

            for (Address adr : addresses) {
                if (adr != null) { //adr!=null
//                    Address adr = addresses.get(0);
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                    } else {
                        Log.e("TAG", "CITY NOT FOUND");
                        Toast.makeText(this, "User City Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName) {
        String url = "https://api.weatherapi.com/v1/forecast.json?key=6f1e7d44b4af4e1f855145916230607&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        tvHeaderCityName.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            pbProg.setVisibility(View.GONE);
            clWeatherlayout.setVisibility(View.VISIBLE);
            weatherRVModelArrayList.clear();

            try {
                String temp = response.getJSONObject("current").getString("temp_c");
                tvTemp.setText(temp+"°C");
                int isDay = response.getJSONObject("current").getInt("is_day");

                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("https:".concat(conditionIcon)).into(ivWeatherImg);
                tvCondition.setText(condition);

                if (isDay == 1) {
                    ivBackground.setImageResource(R.drawable.day);
                } else {
                    ivBackground.setImageResource(R.drawable.night);
                }


                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = forecastO.getJSONArray("hour");

                for (int i = 0; i < hourArray.length(); i++) {
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temp_c = hourObj.getString("temp_c");
                    String icon = hourObj.getJSONObject("condition").getString("icon");
                    String wind_kph = hourObj.getString("wind_kph");

                    weatherRVModelArrayList.add(new WeatherRVModel(time, temp_c, icon, wind_kph));
                }
                weatherRVAdaptor.notifyDataSetChanged();

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, error -> Toast.makeText(MainActivity.this, "Please Enter Valid City Name", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }
}