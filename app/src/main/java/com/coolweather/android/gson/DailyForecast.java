package com.coolweather.android.gson;

/**
 * Created by Dong on 2017/10/17.
 */

public class DailyForecast {

    public String date;
    public Condition cond;
    public Temperature tmp;

    public class Condition {
        public String txt_d;
    }

    public class Temperature {
        public String max;
        public String min;
    }


}
