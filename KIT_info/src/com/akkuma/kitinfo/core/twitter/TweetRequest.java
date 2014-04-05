package com.akkuma.kitinfo.core.twitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweetRequest implements Serializable {

    private static final String TWITTER_SHORT_URL = "https://t.co/AAAAAAAAAA";
    private static final long serialVersionUID = 125994125721140448L;
    
    

    private ArrayList<String> list;
    
    public TweetRequest() {
        list = new ArrayList<String>();
    }
    
    public TweetRequest(String text) {
        list = new ArrayList<String>();
        add(text);
    }
    
    public TweetRequest add(String text) {

        Pattern urlPattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.MULTILINE);
        
        Matcher urlMatcher;
        String dmString;
        urlMatcher = urlPattern.matcher(text);
        if (urlMatcher.find()) {
            dmString = urlMatcher.replaceAll(TWITTER_SHORT_URL);
        } else {
            dmString = text;
        }
        
        if (dmString.length() <= 140) {
            list.add(text);
            return this;
        }
        
        // テキスト内のURLを探す
        ArrayList<String> urlList = new ArrayList<String>();
        urlMatcher = urlPattern.matcher(text);
        
        String str;
        while(urlMatcher.find()) {
            urlList.add(urlMatcher.group());
            str = urlMatcher.replaceFirst("");
            urlMatcher = urlPattern.matcher(str);
        }
        
        // 1つ目
        String first = text.substring(0, 139);
        String dd = first;
        for (String url : urlList) {
            dd = dd.replace(url, "");
        }
        urlMatcher = urlPattern.matcher(dd);
        if (urlMatcher.find()) {
            String invalidUrl = urlMatcher.group();
            first = first.replace(invalidUrl, "");
        }
        list.add(first + "…");
        
        // 2つ目
        String second = "（続き）" + text.replace(first, "");
        boolean leader = false;
        String footer = "";
        if (second.length() > 140) {
            leader = true;
            footer = "（省略）";
        }
        second = second.substring(0, leader ? 136 : second.length());
        String ee = second;
        for (String url : urlList) {
            ee = ee.replace(url, "");
        }
        urlMatcher = urlPattern.matcher(ee);
        if (urlMatcher.find()) {
            String invalidUrl2 = urlMatcher.group();
            second = second.replace(invalidUrl2, "");
        }
        list.add(second + footer);
        
        return this;
    }

    ArrayList<String> getList() {
        return list;
    }

    void setList(ArrayList<String> list) {
        this.list = list;
    }
    
    
    
}
