package com.example.hiweather.util;

import android.text.TextUtils;   //

import com.example.hiweather.db.Province;
import com.example.hiweather.db.City;
import com.example.hiweather.db.County;
import com.example.hiweather.gson.Weather;
import com.google.gson.Gson;


import org.json.JSONArray;    //
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    /**
     * 解析和处理服务器返回的升级数据
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){    // TextUtils
            try{
                // 实例化 JSONArray
                JSONArray allProvinces = new JSONArray(response);
                for (int i=0; i<allProvinces.length(); i++){
                    // 实例化
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;   // true
            }catch (JSONException e){   //
                e.printStackTrace();    //
            }
        }
        return false;
    }

    /**
     * 用来解析和处理服务器返回的市级数据
     */

    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities = new JSONArray(response);
                for (int i=0; i<allCities.length(); i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 用来解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allCountries = new JSONArray(response);
                for(int i=0; i<allCountries.length(); i++){
                    JSONObject countyObject = allCountries.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的json数据解析成weather实体类
     */
    public static Weather handleWeatherResponse(String response){
        try{
            // 通过 JSONObject JSONArray 解析天气数据中的主体内容
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
