package com.example.hiweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    // 注解的方式来让JSON字段和java字段建立映射关系
    // 这个变量就会被替换json中为city的字段
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
