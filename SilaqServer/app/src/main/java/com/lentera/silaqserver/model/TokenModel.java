package com.lentera.silaqserver.model;

public class TokenModel {
    private String phone,token;
    private boolean serverToken,driverToken;

    public TokenModel() {
    }

    public TokenModel(String phone, String token, boolean serverToken, boolean driverToken) {
        this.phone = phone;
        this.token = token;
        this.serverToken = serverToken;
        this.driverToken = driverToken;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isServerToken() {
        return serverToken;
    }

    public void setServerToken(boolean serverToken) {
        this.serverToken = serverToken;
    }

    public boolean isDriverToken() {
        return driverToken;
    }

    public void setDriverToken(boolean driverToken) {
        this.driverToken = driverToken;
    }
}
