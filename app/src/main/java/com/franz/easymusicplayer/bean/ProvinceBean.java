package com.franz.easymusicplayer.bean;

import java.util.List;

public class ProvinceBean {
    private String provinceName;//็
    private int provinceCode;//็ไปฃ็ 
    private List<CityBean> cityList;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public List<CityBean> getCityList() {
        return cityList;
    }

    public void setCityList(List<CityBean> cityList) {
        this.cityList = cityList;
    }
}
