package com.akkuma.kitinfo.core.announce;

import java.io.Serializable;

public class CommonAnnouncementDetailEntry implements Serializable {

    private static final long serialVersionUID = -5220525142263922369L;
    private int id;
    private String title;
    private String body;
    private String contributor;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

}
