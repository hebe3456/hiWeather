package com.example.hiweather;

import android.app.ProgressDialog;  //
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.hiweather.db.City;
import com.example.hiweather.db.County;
import com.example.hiweather.db.Province;
import com.example.hiweather.gson.Weather;
import com.example.hiweather.util.HttpUtil;
import com.example.hiweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    // 定义静态常量
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;    // 初始化
    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                if (currentLevel == LEVEL_PROVINCE){
                    // 判断当前级别，如果是省,
                    selectedProvince = provinceList.get(position);
                    // 查询市的数据
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    // 判断当前级别，如果是市,
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    // 判断当前级别，如果是LEVEL_COUNTY，就启动WeatherActivity,传入天气id
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity){
                        // 判断一个对象是类的实例，在碎片中调用getActivity()方法
                        // 碎片在MainActivity中，
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        // 碎片在WeatherActivity中
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        // 关闭滑动菜单
                        activity.drawerLayout.closeDrawers();
                        // 显示下拉刷新进度条
                        activity.swipeRefresh.setRefreshing(true);
                        // 请求城市信息
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (currentLevel== LEVEL_COUNTY){
                    // 判断当前级别，如果是县
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    // 判断当前级别，如果是市,
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
        // 头布局的标题设置成中国
        titleText.setText("中国");
        // 隐藏返回按钮,因为省级列表不能再返回了
        backButton.setVisibility(View.GONE);
        // 调用litepal的查询接口，从数据库中读取省级数据
        provinceList = DataSupport.findAll(Province.class);
        if( provinceList.size() > 0 ){
            Log.d("provinceList", "queryProvinces: 调用if");
            // 取到数据
            // 清空dataList
            dataList.clear();
            for(Province province: provinceList){
                // 遍历数据列表，显示到界面上
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            Log.d("provinceList", "queryProvinces: 2");
            // 没取到，就调用queryFromServer()从服务器上查询数据
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询全省所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        // 返回按钮可见,
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(
                selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            // 没取到，就调用queryFromServer()从服务器上查询数据
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;   // 少了个“/”，错误
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询全省所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        // 返回按钮可见,
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;        // 错误
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;    // 错误
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * queryFromServer（）向服务器发送请求，响应数据会回调到onResponse() 方法中
     * 调用Utility的handleProvinceResponse（）方法解析和处理服务器返回的数据，存储到数据库中
     * 再次调用queryProvinces（）方法，重新加载省数据
     * queryProvinces（）方法 有UI操作，需要在主线程调用，借助runOnUiThread（）实现从子线程切换到主线程
     * 此时数据库已经存在了数据，调用queryProvinces（）方法就会将数据显示到界面上
     * 到了省，就会进入ListView的
     */
    private void queryFromServer(String address, final String type){
        // 进度显示
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onFailure(Call call, IOException e){
                // 通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
//                        closeProgressDialog();     // 直接进入到这儿，所以没有看到加载的进度显示条
                        // toast信息
                        Toast.makeText(getContext(), "加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException{
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }

                if (result){
                    // 通过runOnUiThread()方法回到主线程处理逻辑
                    getActivity().runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            closeProgressDialog();
                            if ("province".equals(type)){       // type是啥？
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
        if (progressDialog==null){
            // 如果progressDialog为空
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private  void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
