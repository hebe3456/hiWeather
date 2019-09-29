package com.example.hiweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.example.hiweather.gson.Forecast;
import com.example.hiweather.gson.Weather;
import com.example.hiweather.util.HttpUtil;
import com.example.hiweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    // 定义控件
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    // 可变背景图片
    private ImageView bingPicImg;

    // 刷新天气
    public SwipeRefreshLayout swipeRefresh;
    // 记录城市的天气id
    private String mWeatherId;

    // 切换城市，滑动菜单
    public DrawerLayout drawerLayout;
    // 切换城市按钮
    private Button navButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            // Android 5.0 系统才支持，因此家里个判断
            // 拿到 decorView
            View decorView = getWindow().getDecorView();
            // 改变系统UI的显示，传入这两个参数，表示活动的布局会显示在状态栏上面
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // 将状态栏设置成透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);       // R.layout

        // 初始化控件, 获取控件实例
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);           // 控件名字错误，则找不到
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        // 初始化,获取 “每日一图”控件实例
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        // 获取SwipeRefreshLayout 实例
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        // 设置颜色为主题色
//        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);

        // 切换城市，滑动菜单
        // 获取滑动菜单实例
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // 获取切换城市按钮实例
        navButton = (Button) findViewById(R.id.nav_button);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);   // import
        String weatherString = prefs.getString("weather", null);
        if(weatherString != null){
            // 有缓存时直接解析天气数据，（第二次）
            Weather weather = Utility.handleWeatherResponse(weatherString); // import com.example.hiweather.gson.Weather;
            // 定义一个变量，记录城市天气
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气数据，（第一次）
            // 从Intent中取出天气id
//            String weatherId = getIntent().getStringExtra("weather_id");           //没有自动刷新
            mWeatherId = getIntent().getStringExtra("weather_id");            //有自动刷新
            // 请求数据前，将ScrollView隐藏，不然空数据的界面看上去很奇怪
            weatherLayout.setVisibility(View.INVISIBLE);
            // 调用requestWeather 从服务器获取天气数据
//            requestWeather(weatherId);                        //没有自动刷新
            requestWeather(mWeatherId);                         //有自动刷新
        }
        // 自动刷新处理
        // 设置一个监听器， 当触发下拉刷新操作时，就会回调这个监听器的onRefresh()方法
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 调用方法请求信息
                requestWeather(mWeatherId);
            }
        });

        // 打开滑动菜单，切换城市的逻辑在ChooseAreaFragment中进行
        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 尝试从SharedPreferences中读取缓存的背景图片
        String bingPic = prefs.getString("bing_pic", null);    // "bing_pic"是啥？
        if(bingPic != null){
            // 有缓存，就直接使用Glide加载图片
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            // 没缓存，调用方法请求今日的必应背景图
            loadBingPic();
        }
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId){
        // 使用参数中的weatherId 和 之前申请好的 API key拼装出一个接口地址
        Log.d("requestWeather", "requestWeather: 调用");
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=c5fa5c18128444398a3f9b8674cb6d4a";
        Log.d("onResponse", "onResponse: weatherUrl: " + weatherUrl);

        // 向该地址发请求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 服务器会将相应城市的天气信息以json格式返回
                final String responseText = response.body().string();
                Log.d("onResponse", "onResponse: responseText: " + responseText);
                // 在onResponse()回调中先调用Utility.handleWeatherResponse()将返回的数据转换成Weather对象
                final Weather weather = Utility.handleWeatherResponse(responseText);
                Log.d("onResponse", "onResponse: weather: " + weather);

                // 线程切换到主线程
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        if(weather != null && "ok".equals(weather.status)){
                            // server返回的状态是ok，请求成功，此时将返回的数据缓存到SharedPreferences中
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            // 显示内容
                            // 自动刷新
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // 自动刷新结束，隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("requestWeather", "onFailure: " + e);   // 打印错误日志！
                // 线程切换到主线程
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        // 自动刷新结束，隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        // 加载每日一图，每次请求天气信息时，刷新背景图
        loadBingPic();
    }
    /**
     * 处理并展示Weather实体类中的数据
     * 从Weather对象中获取数据，显示到相应的控件中
     */
    private void showWeatherInfo(Weather weather){
        // 拿到变量
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "C";  // .怎么打
        String weatherInfo = weather.now.more.info;
        Log.d("rlj", "showWeatherInfo: cityName:" +cityName );
        Log.d("rlj", "showWeatherInfo: updateTime:" + updateTime );
        Log.d("rlj", "showWeatherInfo: degree:" + degree );
        Log.d("rlj", "showWeatherInfo: weatherInfo:" + weatherInfo );

        // 给控件赋值
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();
        // for循环处理每天的天气
        for (Forecast forecast : weather.forecastList) {
            // 动态加载forecast_item.xml布局，设置相应数据，添加到父布局当中
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);

            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);    // 错误
            TextView maxText = (TextView) view.findViewById(R.id.max_text);    // 错误
            TextView minText = (TextView) view.findViewById(R.id.min_text);    // 错误

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            minText.setText(forecast.temperature.min);
            maxText.setText(forecast.temperature.max);
            forecastLayout.addView(view);
        }

        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动指数：" + weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        // 将ScrollView 可见
        weatherLayout.setVisibility(View.VISIBLE);

        // 激活服务
        // 选中一个城市，首次更新服务后，就会一直在后台运行，没8小时更新一次天气
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 加载每日一图
     */
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        // 获取背景图片链接
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                // 获取背景图片链接
                final String bingPic = response.body().string();
                // 将链接缓存到SharedPreferences
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                // 切到主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 用Glide加载图片
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e){
                e.printStackTrace();
            }
        });
    }
}
