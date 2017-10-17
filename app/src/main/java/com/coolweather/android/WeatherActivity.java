package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.DailyForecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    private ScrollView weatherView;

    private TextView titleCity;
    private TextView titleTime;
    private TextView nowTmp;
    private TextView nowTxt;
    private LinearLayout forecastLayout;
    private TextView aqiAqi;
    private TextView aqiPm25;
    private TextView comfort;
    private TextView carWash;
    private TextView sport;
    private ImageView bingImage;

    private String TAG = "WeatherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
        setContentView(R.layout.activity_weather);
        drawerLayout = (DrawerLayout) findViewById(R.id.draw_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        weatherView = (ScrollView) findViewById(R.id.scroll_view);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleTime = (TextView) findViewById(R.id.title_time);
        nowTmp = (TextView) findViewById(R.id.now_temp_text);
        nowTxt = (TextView) findViewById(R.id.now_cond_txt);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiAqi = (TextView) findViewById(R.id.aqi_aqi);
        aqiPm25 = (TextView) findViewById(R.id.aqi_pm25);
        comfort = (TextView) findViewById(R.id.suggestion_comfort);
        carWash = (TextView) findViewById(R.id.suggestion_cw);
        sport = (TextView) findViewById(R.id.suggestion_sport);
        weatherView.setVisibility(View.INVISIBLE);
        bingImage = (ImageView) findViewById(R.id.background_image);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pref.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherWithGSON(weatherString);
            mWeatherId = weather.basic.id;
            showWeatherInfo(weather);
        } else {
            Intent intent = getIntent();
            mWeatherId = intent.getStringExtra("weather_id");
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        String bingPicAdr = pref.getString("bing_pic", null);
        if (bingPicAdr != null) {
            Glide.with(this).load(bingPicAdr).into(bingImage);
        } else {
            loadBingPic();
        }
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }


    /**
     * 向和风天气接口发送天气请求，并调用方法在活动中显示数据
     * @param weatherId
     */
    public void requestWeather(String weatherId) {
        String address = "http://guolin.tech/api/weather?cityid=" + weatherId +
                "&key=fdf14cbcf74749dd9af19be31bd6353d";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                        Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherWithGSON(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            Log.d(TAG, "run:  "+weather.forecastList.get(0).date);
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 在活动中中显示获得的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        titleCity.setText(weather.basic.city);
        titleTime.setText(weather.basic.update.date.split(" ")[1]);
        nowTmp.setText(weather.now.tmp+"℃");
        nowTxt.setText(weather.now.condition.txt);
        aqiAqi.setText(weather.aqi.city.aqi);
        aqiPm25.setText(weather.aqi.city.pm25);
        for (DailyForecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView forecastDate = (TextView) view.findViewById(R.id.forecast_date);
            TextView forecastTxt = (TextView) view.findViewById(R.id.forecast_cond_txt);
            TextView forecastTmpMax = (TextView) view.findViewById(R.id.forecast_tmp_max);
            TextView forecastTmpMin = (TextView) view.findViewById(R.id.forecast_tmp_min);
            forecastDate.setText(forecast.date);
            forecastTxt.setText(forecast.cond.txt_d);
            forecastTmpMax.setText(forecast.tmp.max);
            forecastTmpMin.setText(forecast.tmp.min);
            forecastLayout.addView(view);
        }


        for (int i = 0; i < weather.forecastList.size(); i++) {

        }
        comfort.setText(weather.suggestion.comfort.txt);
        carWash.setText(weather.suggestion.carWash.txt);
        sport.setText(weather.suggestion.sport.txt);
        weatherView.setVisibility(View.VISIBLE);
    }

    /**
     * 用OKhttp在子线程中获取必应每日一图,并切换主线程设置图片
     */
    private void loadBingPic() {
        String requestBingPicAdr = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPicAdr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, "获取背景图片失败",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPicAdr = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPicAdr);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPicAdr).into(bingImage);
                    }
                });
            }
        });
    }
}
