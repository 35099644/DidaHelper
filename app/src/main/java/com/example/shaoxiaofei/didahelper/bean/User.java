package com.example.shaoxiaofei.didahelper.bean;

public class User {

    private String phone;
    private String immei;

    public User() {

    }

    public User(String phone, String immei) {
        this.phone = phone;
        this.immei = immei;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImmei() {
        return immei;
    }

    public void setImmei(String immei) {
        this.immei = immei;
    }
}
