package com.akkuma.kitinfo.core.announce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings("serial")
public class CommonAnnouncementHandler {

    public static final String PORTAL_URL = "http://portal10.mars.kanazawa-it.ac.jp/portal/student";

    private boolean isAuthenticated;

    public CommonAnnouncementHandler() {
        CookieHandler.setDefault(new CookieManager());
    }

    public void destroy() {
        CookieHandler.setDefault(null);
        isAuthenticated = false;
    }

    public void authenticate(String userId, String password) throws AuthenticateFailedException {
        String spec = PORTAL_URL + "?uid=" + userId + "&pw=" + password + "&_TRXID=LOGIN";
        URL url;
        try {
            url = new URL(spec);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new AuthenticateFailedException();
        }

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AuthenticateFailedException();
        }

        try {
            con.setRequestMethod("GET");
            con.connect();
            con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AuthenticateFailedException();
        } finally {
            con.disconnect();
        }

        isAuthenticated = true;
    }

    public ArrayList<CommonAnnouncementEntry> get() throws NotAuthenticatedException, CommonAnnouncementException {
        if (!isAuthenticated) {
            throw new NotAuthenticatedException();
        }

        ArrayList<CommonAnnouncementEntry> list = new ArrayList<CommonAnnouncementEntry>();
        URL url;
        try {
            url = new URL(PORTAL_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new CommonAnnouncementException();
        }

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonAnnouncementException();
        }

        try {
            con.setRequestMethod("GET");
            con.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "Windows-31J"));

            boolean formStartFlag = false;
            String _CONTCHECK = null;
            CommonAnnouncementEntry entryBuffer = null;

            for (String buffer; (buffer = reader.readLine()) != null;) {

                if (!formStartFlag && Pattern.matches(".*<form name=\"kokuchiForm\".*>.*", buffer)) {
                    formStartFlag = true;
                    continue;
                }
                if (formStartFlag && Pattern.matches(".*</form>.*", buffer)) {
                    formStartFlag = false;
                    continue;
                }

                if (formStartFlag) {
                    // System.out.println(buffer);
                } else {
                    continue;
                }

                // _CONTCHECK の取得

                Pattern p = Pattern.compile("(<input type=\"hidden\" name=\"_CONTCHECK\" value=\")(.+)(\">)");
                Matcher m = p.matcher(buffer);
                if (m.find()) {
                    _CONTCHECK = m.replaceFirst("$2");
                    continue;
                }

                // データのパース 1行目
                Pattern parsePattern = Pattern.compile("(<li>.*<a.* href=\"javascript:display_subject\\()([0-9]+)(\\)\".*>.*)");
                Matcher parseMatcher = parsePattern.matcher(buffer);
                if (parseMatcher.find()) {
                    entryBuffer = new CommonAnnouncementEntry();
                    entryBuffer.setCONTCHECK(_CONTCHECK);
                    entryBuffer.setId(Integer.parseInt(parseMatcher.replaceFirst("$2")));

                    Pattern isNewPattern = Pattern.compile("<font.* class=\"new\">.+</font>");
                    if (isNewPattern.matcher(buffer).find()) {
                        entryBuffer.setNewEntry(true);
                    } else {
                        entryBuffer.setNewEntry(false);
                    }
                    continue;
                }

                // データのパース 2行目
                Pattern parsePattern2 = Pattern.compile("(.*)(</a></li>|</s></a></li>)");
                Matcher parseMatcher2 = parsePattern2.matcher(buffer);
                if (parseMatcher2.find() && entryBuffer != null) {
                    entryBuffer.setTitle(StringEscapeUtils.unescapeHtml4(parseMatcher2.replaceFirst("$1")).trim());
                    if (Pattern.matches(".*</s>.*", buffer)){
                        entryBuffer.setCanceled(true);
                    } else {
                        entryBuffer.setCanceled(false);
                    }
                    list.add(entryBuffer);
                    entryBuffer = null;
                    continue;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonAnnouncementException();
        } finally {
            con.disconnect();
        }

        return list;
    }

    public CommonAnnouncementDetailEntry getDetailEntry(int id, String _CONTCHECK) throws NotAuthenticatedException, CommonAnnouncementException {
        if (!isAuthenticated) {
            throw new NotAuthenticatedException();
        }
        CommonAnnouncementDetailEntry entry = null;
        String spec = PORTAL_URL + "?_TRXID=RPTL1301&_INPAGEID=TOPPAGE&_CONTCHECK=" + _CONTCHECK + "&noticeno=" + id;
        URL url;
        try {
            url = new URL(spec);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new CommonAnnouncementException();
        }

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonAnnouncementException();
        }

        try {
            con.setRequestMethod("GET");
            con.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "Windows-31J"));
            entry = new CommonAnnouncementDetailEntry();
            entry.setId(id);

            boolean titleParsePreFlag = false;
            boolean titleParseFlag = false;

            boolean bodyParsePreFlag = false;
            boolean bodyParseFlag = false;

            boolean contributorParsePreFlag = false;
            boolean contributorParseFlag = false;

            for (String buffer; (buffer = reader.readLine()) != null;) {

                if (Pattern.matches(".*<th.* class=\"header\" nowrap>用件</th>.*", buffer)) {
                    titleParsePreFlag = true;
                    continue;
                }
                if (titleParsePreFlag) {
                    titleParsePreFlag = false;
                    titleParseFlag = true;
                    continue;
                }
                if (titleParseFlag) {
                    entry.setTitle(StringEscapeUtils.unescapeHtml4(buffer).trim());
                    titleParseFlag = false;
                    continue;
                }

                if (Pattern.matches(".*<th.* class=\"header\" nowrap>詳細内容</th>.*", buffer)) {
                    bodyParsePreFlag = true;
                    continue;
                }
                if (bodyParsePreFlag) {
                    bodyParsePreFlag = false;
                    bodyParseFlag = true;
                    continue;
                }
                if (bodyParseFlag && Pattern.matches(".*</td>.*", buffer)) {
                    bodyParseFlag = false;
                    continue;
                } else if (bodyParseFlag) {
                    if (buffer.isEmpty()) {
                        continue;
                    }
                    String append = entry.getBody();
                    if (append == null) {
                        entry.setBody(StringEscapeUtils.unescapeHtml4(buffer.replaceAll("<.*>", "")).trim());
                    } else {
                        String str = append + "\n" + buffer.replaceAll("<.*>", "").trim();
                        entry.setBody(str);
                    }
                    continue;
                }

                if (Pattern.matches(".*<th.* class=\"header\" nowrap>発信元</th>.*", buffer)) {
                    contributorParsePreFlag = true;
                    continue;
                }
                if (contributorParsePreFlag) {
                    contributorParsePreFlag = false;
                    contributorParseFlag = true;
                    continue;
                }
                if (contributorParseFlag) {
                    if (buffer.isEmpty()) {
                        continue;
                    }
                    entry.setContributor(StringEscapeUtils.unescapeHtml4(buffer).trim());

                    contributorParseFlag = false;
                    continue;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new CommonAnnouncementException();
        } finally {
            con.disconnect();
        }

        return entry;
    }

    public static class AuthenticateFailedException extends Exception {};

    public static class NotAuthenticatedException extends Exception {};

    public static class CommonAnnouncementException extends Exception {};
}
