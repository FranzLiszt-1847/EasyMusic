package com.franz.easymusicplayer.bean;

import java.util.List;

public class RegionBean {
    private String provinceName;//省
    private int provinceCode;//省代码
    private List<City> cityList;

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

    public List<City> getCityList() {
        return cityList;
    }

    public void setCityList(List<City> cityList) {
        this.cityList = cityList;
    }

    class City{
        private String cityName;//市
        private int cityCode;//市代码
        private List<County> countyList;

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public int getCityCode() {
            return cityCode;
        }

        public void setCityCode(int cityCode) {
            this.cityCode = cityCode;
        }

        public List<County> getCountyList() {
            return countyList;
        }

        public void setCountyList(List<County> countyList) {
            this.countyList = countyList;
        }
    }

    class County{
        private String countyName;//区or县
        private int countyCode;//区or县代码

        public String getCountyName() {
            return countyName;
        }

        public void setCountyName(String countyName) {
            this.countyName = countyName;
        }

        public int getCountyCode() {
            return countyCode;
        }

        public void setCountyCode(int countyCode) {
            this.countyCode = countyCode;
        }
    }
}
