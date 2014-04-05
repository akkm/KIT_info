package com.akkuma.kitinfo.core.cancel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CanceledLectureEntry {

    private long day = 0L;
    private String period;
    private String lectureName;
    private String className;
    private String nameOfTeacher;
    private String note;
    private boolean isCanceled;

    public long getDay() {
        return day;
    }

    public void setDay(long day) {
        this.day = day;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getLectureName() {
        return lectureName;
    }

    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getNameOfTeacher() {
        return nameOfTeacher;
    }

    public void setNameOfTeacher(String nameOfTeacher) {
        this.nameOfTeacher = nameOfTeacher;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    public Integer getHashCode() {
        return new HashCodeBuilder().append(day).append(period).append(className).append(nameOfTeacher).append(note).append(isCanceled).build();
    }

    public String toOutputString() {
        return toOutputString(true);
    }

    public String toOutputString(boolean appendToday) {

        StringBuilder builder = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("M/d（E）", Locale.JAPANESE);

        if (isCanceled) {
            builder.append("【※取り消し訂正】");
        }

        builder.append("[" + format.format(new Date(day)));
        if (appendToday && isTodayEntry()) {
            builder.append("（今日）");
        }
        builder.append(period + "限] ");

        builder.append(className);

        builder.append(" <" + lectureName + "> ");

        builder.append(note);

        return builder.toString();
    }

    public boolean isTodayEntry() {

        Calendar mine = Calendar.getInstance();
        mine.setTimeInMillis(day);
        Calendar today = Calendar.getInstance();
        return mine.get(Calendar.YEAR) == today.get(Calendar.YEAR) && mine.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

    }
}
