package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    private String TAG = "AutoUpdateService";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeatherInfo();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hours = 8 * 60 * 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + hours;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);//删除之前的manager.set的设置
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新必应图片地址缓存
     */
    private void updateBingPic() {
        String requestBingPicAdr = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPicAdr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(AutoUpdateService.this, "获取图片失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", responseText);
                editor.apply();
            }
        });
    }

    /**
     * 更新天气服务器返回数据
     */
    private void updateWeatherInfo() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        String weatherString = pref.getString("weather", null);
        if (weatherString != null) {
            final Weather weather = Utility.handleWeatherWithGSON(weatherString);
            String weatherId = weather.basic.id;
            String address = "http://guolin.tech/api/weather?cityid=" + weatherId +
                    "&key=fdf14cbcf74749dd9af19be31bd6353d";
            HttpUtil.sendOkHttpRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(AutoUpdateService.this, "自动获取天气信息失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weather", responseText);
                    editor.apply();
                }
            });
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate:  run");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: run");
        super.onDestroy();
    }
}
