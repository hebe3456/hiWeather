package com.example.hiweather.db;

import org.litepal.crud.DataSupport;    //DataSupport

public class City extends DataSupport {
    // 定义私有变量
    private int id;
    private String cityName;      // 城市的名字
    private int cityCode;         // 城市代号
    private int provinceId;       // 当前市所属省的id值

    // 定义getter setter 方法
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getCityName(){
        return cityName;
    }

    public void setCityName(String cityName){
        this.cityName = cityName;
    }

    public int getCityCode(){
        return cityCode;
    }

    public void setCityCode(int cityCode){
        this.cityCode = cityCode;
    }

    public int getProvinceId(){
        return provinceId;
    }

    public void setProvinceId(int provinceId){
        this.provinceId = provinceId;
    }



}
