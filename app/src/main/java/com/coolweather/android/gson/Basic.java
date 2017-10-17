package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dong on 2017/10/17.
 */

public class Basic {

    public String city;
    public String id;
    public Update update;


    public class Update {
        @SerializedName("loc")
        public String date;
    }
}
