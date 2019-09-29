package com.example.hiweather.util;

import android.util.Log;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpUtil {
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        // 发起http请求，传入参数：请求地址，注册一个回调处理服务器响应
        OkHttpClient client = new OkHttpClient();
//        下面的也没用，打印不出错误日志
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
//            @Override
//            public void log(String message) {
//                Log.e("OkHttpClient:", message);
//            }
//        })).build();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
