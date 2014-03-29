package com.akkuma.kitinfo.core.announce;

import java.io.Serializable;

public class CommonAnnouncementEntry implements Serializable {

    private static final long serialVersionUID = 7596588370348037691L;
    private int id;
    private String title;
    private boolean isNewEntry;
    private boolean isCanceled;
    private String _CONTCHECK;

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

    public boolean isNewEntry() {
        return isNewEntry;
    }

    public void setNewEntry(boolean isNewEntry) {
        this.isNewEntry = isNewEntry;
    }

    public String getCONTCHECK() {
        return _CONTCHECK;
    }

    public void setCONTCHECK(String _CONTCHECK) {
        this._CONTCHECK = _CONTCHECK;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

}
