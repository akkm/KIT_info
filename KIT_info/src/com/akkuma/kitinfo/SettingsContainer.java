package com.akkuma.kitinfo;

import java.io.Serializable;

public class SettingsContainer implements Serializable {

    
    private static final long serialVersionUID = 380761023779328668L;
    private String portalId = "";
    private String portalPassword = "";
    
    private String consumerKey = "";
    private String consumerSecret = "";
    private String accessToken = "";
    private String accessTokenSecret = "";
    private String proxyHost = "";
    private String proxyPort = "";
    private boolean disableTweetCheck = false;
    private String weatherRssUrl = "http://rss.weather.yahoo.co.jp/rss/days/5610.xml";
    private int weatherTweetHour;
    private int weatherTweetMinute;
    private int dayTweetTimeHour;
    private int dayTweetTimeMinute;
    
    public String getPortalId() {
        return portalId;
    }
    public void setPortalId(String portalId) {
        this.portalId = portalId;
    }
    public String getPortalPassword() {
        return portalPassword;
    }
    public void setPortalPassword(String portalPassword) {
        this.portalPassword = portalPassword;
    }
    public String getConsumerKey() {
        return consumerKey;
    }
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }
    public String getConsumerSecret() {
        return consumerSecret;
    }
    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }
    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }
    public String getProxyHost() {
        return proxyHost;
    }
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
    public String getProxyPort() {
        return proxyPort;
    }
    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }
    public boolean isDisableTweetCheck() {
        return disableTweetCheck;
    }
    public void setDisableTweetCheck(boolean disableTweetCheck) {
        this.disableTweetCheck = disableTweetCheck;
    }
    public String getWeatherRssUrl() {
        return weatherRssUrl;
    }
    public void setWeatherRssUrl(String weatherRssUrl) {
        this.weatherRssUrl = weatherRssUrl;
    }
    public int getWeatherTweetHour() {
        return weatherTweetHour;
    }
    public void setWeatherTweetHour(int weatherTweetHour) {
        this.weatherTweetHour = weatherTweetHour;
    }
    public int getWeatherTweetMinute() {
        return weatherTweetMinute;
    }
    public void setWeatherTweetMinute(int weatherTweetMinute) {
        this.weatherTweetMinute = weatherTweetMinute;
    }
    public int getDayTweetTimeHour() {
        return dayTweetTimeHour;
    }
    public void setDayTweetTimeHour(int dayTweetTimeHour) {
        this.dayTweetTimeHour = dayTweetTimeHour;
    }
    public int getDayTweetTimeMinute() {
        return dayTweetTimeMinute;
    }
    public void setDayTweetTimeMinute(int dayTweetTimeMinute) {
        this.dayTweetTimeMinute = dayTweetTimeMinute;
    }
    
    
}
