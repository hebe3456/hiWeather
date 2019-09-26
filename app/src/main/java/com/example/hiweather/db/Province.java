package com.example.hiweather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {
    // 定义私有变量
    private int id;                 // 每个实体类都应该有
    private String provinceName;    // 省的名字
    private int provinceCode;       // 省的代号

    // 定义getter setter 方法
    public int getId(){
        return id;
    }

    public void setId(int id){     // 参数
        this.id = id;      //
    }

    public String getProvinceName(){
        return provinceName;
    }

    public void setProvinceName(String provinceName){
        this.provinceName = provinceName;
    }

    public int getProvinceCode(){
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode){
        this.provinceCode = provinceCode;
    }
}
