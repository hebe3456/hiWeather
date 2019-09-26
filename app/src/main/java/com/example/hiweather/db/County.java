package com.example.hiweather.db;

import org.litepal.crud.DataSupport;

public class County extends DataSupport {
    // 定义私有变量
    private int id;
    private String countyName;   // 县名
    private String weatherId;     // 县对应的天气id
    private int cityId;           // 县所属市的id

    // 定义getter setter 方法
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getCountyName(){
        return countyName;
    }

    public void setCountyName(String countyName){
        this.countyName = countyName;
    }

    public String getWeatherId(){
        return weatherId;
    }

    public void setWeatherId(String weatherId){
        this.weatherId = weatherId;
    }

    public int getCityId(){
        return cityId;
    }

    public void setCityId(int cityId){
        this.cityId = cityId;
    }
}
