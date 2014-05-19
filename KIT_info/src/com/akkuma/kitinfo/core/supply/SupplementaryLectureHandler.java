package com.akkuma.kitinfo.core.supply;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

public class SupplementaryLectureHandler {

    public static final String URL = "http://portal10.mars.kanazawa-it.ac.jp/portal/public?_TRXID=RHOK0000";

    public ArrayList<SupplementaryLectureEntry> get() throws SupplementaryLectureException {

        ArrayList<SupplementaryLectureEntry> list = new ArrayList<SupplementaryLectureEntry>();
        URL url = null;
        try {
            url = new URL(URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SupplementaryLectureException();
        }

        try {
            con.setRequestMethod("GET");
            con.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "Windows-31J"));

            boolean flag = false;
            SupplementaryLectureEntry entry = null;
            
            for (String buffer; (buffer = reader.readLine()) != null;) {
                if (Pattern.matches(".*<table .* class=\"list\">.*", buffer)) {
                    flag = true;
                    continue;
                }
                if (Pattern.matches(".*</table>.*", buffer)) {
                    flag = false;
                    continue;
                }
                if (!flag) {
                    continue;
                }
                
                Pattern p = Pattern.compile("(.*<td .*class=\"data\")(>| nowrap>)(.*)(</td>)");
                Matcher m = p.matcher(buffer);
                if (m.find()) {
                    String str = StringEscapeUtils.unescapeHtml4(m.replaceAll("$3").trim());
                    str = str.replace("<BR>", " ");
                    Pattern strikePattern = Pattern.compile("(<strike><i>)(.*)(</i></strike>)");
                    Matcher strikeMatcher = strikePattern.matcher(str);
                    if (strikeMatcher.find()) {
                        str = strikeMatcher.replaceAll("$2").trim();
                        if (entry != null) {
                            entry.setCanceled(true);
                        }
                    }
                    if (entry == null) {
                        entry = new SupplementaryLectureEntry();
                        // 日付
                        Pattern dayPattern = Pattern.compile("(.*)([0-9]+)(/)([0-9]+)(\\(.*\\))(.*)");
                        Matcher dayMatcher = dayPattern.matcher(str);
                        int month = Integer.parseInt(dayMatcher.replaceAll("$2")) - 1;
                        int dayOfMonth = Integer.parseInt(dayMatcher.replaceAll("$4"));
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        calendar.clear();
                        calendar.set(year, month, dayOfMonth, 0, 0, 0);
                        entry.setDay(calendar.getTimeInMillis());
                        continue;
                    }
                    if (entry.getPeriod() == null) {
                        // 時限
                        entry.setPeriod(str);
                        continue;
                    }
                    if (entry.getLectureName() == null) {
                        // 科目名
                        entry.setLectureName(str);
                        continue;
                    }
                    if (entry.getClassName() == null) {
                        // クラス
                        entry.setClassName(str);
                        continue;
                    }
                    if (entry.getNameOfTeacher() == null) {
                        // 担当教員
                        entry.setNameOfTeacher(str);
                        continue;
                    }
                    if (entry.getClassRoom() == null) {
                        // 教室
                        entry.setClassRoom(str);
                        continue;
                    }
                    if (entry.getNote() == null) {
                        // 備考
                    	entry.setNote(str.replaceAll("<.+?>", ""));
                        list.add(entry);
                        entry = null;
                        continue;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new SupplementaryLectureException();
        } finally {
            con.disconnect();
        }

        return list;
    }

    @SuppressWarnings("serial")
    public static class SupplementaryLectureException extends Exception {};

}
