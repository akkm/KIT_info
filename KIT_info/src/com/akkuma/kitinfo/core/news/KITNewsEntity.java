package com.akkuma.kitinfo.core.news;

import java.io.Serializable;

public class KITNewsEntity implements Serializable {
    
    private static final long serialVersionUID = -2075452104015318873L;
    private String url;
    private String title;
    private String description;
    
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    

}
