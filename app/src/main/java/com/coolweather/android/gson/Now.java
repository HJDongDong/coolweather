package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dong on 2017/10/17.
 */

public class Now {

    public String tmp;

    @SerializedName("cond")
    public Condition condition;

    public class Condition {
        public String txt;
    }


}
