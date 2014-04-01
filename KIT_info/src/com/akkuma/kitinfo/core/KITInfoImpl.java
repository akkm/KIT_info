package com.akkuma.kitinfo.core;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.akkuma.kitinfo.core.announce.CommonAnnouncementDetailEntry;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementEntry;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.AuthenticateFailedException;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.CommonAnnouncementException;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.NotAuthenticatedException;
import com.akkuma.kitinfo.core.news.KITNewsEntity;
import com.akkuma.kitinfo.core.news.KITNewsHandler;
import com.akkuma.kitinfo.core.twitter.TweetRequest;
import com.akkuma.kitinfo.core.twitter.TwitterManager;
import com.akkuma.kitinfo.core.weather.KanazawaWeatherHandler;
import com.akkuma.kitinfo.util.FileUtils;

/**
 * 
 * @author Yamada Atsuto
 * 
 */

public final class KITInfoImpl extends KITInfo {

    private DebugOutputListener mListener;
    private TwitterManager mTwitterManager = TwitterManager.getInstance();

    public KITInfoImpl() {}

    public void setDebugOutputListener(DebugOutputListener listener) {
        mListener = listener;
    }

    public void destroy() {
        mListener = null;
    }

    public void start(String portalId, String portalPassword) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd H:mm:ss:SSS");
        onOutput("KIT_info started," + outputDateFormat.format(calendar.getTime()));

        commonAnnouncementSession(portalId, portalPassword);
        kitNewsSession();

    }

    private static final String LOG_KIT_NEWS = "kit_news.log";
    private static final String LOG_COMMON_ANNOUNCEMENT = "common_announcement.log";

    @SuppressWarnings("unchecked")
    public void commonAnnouncementSession(String portalId, String portalPassword) {

        // 共有告知
        CommonAnnouncementHandler commonAnnouncementHandler = new CommonAnnouncementHandler();
        try {
            onOutput("共有告知を取得します。");
            HashMap<Integer, CommonAnnouncementEntry> log = (HashMap<Integer, CommonAnnouncementEntry>) FileUtils.readObjectFromFile(LOG_COMMON_ANNOUNCEMENT);
            if (log == null) {
                log = new HashMap<Integer, CommonAnnouncementEntry>();
                onOutput("共有告知のログファイルを新規作成しました。");
            }

            commonAnnouncementHandler.authenticate(portalId, portalPassword);
            ArrayList<CommonAnnouncementEntry> entries = commonAnnouncementHandler.get();

            onOutput("学生ポータルより共有告知を" + entries.size() + "個取得しました。");

            if (entries.size() == 0) {
                return;
            }

            ArrayList<CommonAnnouncementDetailEntry> newDetailEntries = new ArrayList<CommonAnnouncementDetailEntry>();
            ArrayList<CommonAnnouncementDetailEntry> cancelledEntries = new ArrayList<CommonAnnouncementDetailEntry>();

            for (CommonAnnouncementEntry entry : entries) {
                CommonAnnouncementEntry logEntry = log.get(entry.getId());
                if (logEntry == null || logEntry.isCanceled() != entry.isCanceled()) {
                    CommonAnnouncementDetailEntry detailEntry = commonAnnouncementHandler.getDetailEntry(entry.getId(), entry.getCONTCHECK());

                    if (entry.isCanceled()) {
                        cancelledEntries.add(detailEntry);
                    } else {
                        newDetailEntries.add(detailEntry);
                    }
                }
            }
            onOutput("共通告知の新規エントリーは" + newDetailEntries.size() + "個です。");

            for (CommonAnnouncementDetailEntry entry : cancelledEntries) {
                addTweetQueue(new TweetRequest().add("[共通告知]【※取り消し】" + entry.getTitle().replace("　", "")));
            }

            for (CommonAnnouncementDetailEntry entry : newDetailEntries) {
                TweetRequest req = new TweetRequest();
                req.add("[共通告知]" + entry.getTitle().replace("　", ""));
                req.add("内容:" + entry.getBody().replace("　", ""));
                addTweetQueue(req);
            }

            if (newDetailEntries.size() != 0) {
                log.clear();
                for (CommonAnnouncementEntry entry : entries) {
                    log.put(entry.getId(), entry);
                }

                FileUtils.writeObjectToFile(log, LOG_COMMON_ANNOUNCEMENT);
            }

        } catch (AuthenticateFailedException e) {
            e.printStackTrace();
            onOutput("学生ポータルの認証に失敗");

        } catch (NotAuthenticatedException e) {
            e.printStackTrace();
            onOutput("学生ポータルの認証をしていません");
        } catch (CommonAnnouncementException e) {
            e.printStackTrace();
            onOutput("共有告知の取得でエラー");
        } catch (Exception e) {
            e.printStackTrace();
            onOutput("共有告知の取得でエラー");
        } finally {
            commonAnnouncementHandler.destroy();
        }

    }

    @SuppressWarnings("unchecked")
    public void kitNewsSession() {

        // KITニュース
        KITNewsHandler kitNewsHandler = new KITNewsHandler();
        try {
            onOutput("KITニュースを取得します。");
            HashMap<String, KITNewsEntity> log = (HashMap<String, KITNewsEntity>) FileUtils.readObjectFromFile(LOG_KIT_NEWS);
            if (log == null) {
                log = new HashMap<String, KITNewsEntity>();
                onOutput("KITニュースのログファイルを新規作成しました。");
            }

            ArrayList<KITNewsEntity> list = kitNewsHandler.get(null, 0);
            if (list.size() == 0) {
                return;
            }

            ArrayList<KITNewsEntity> newEntities = new ArrayList<KITNewsEntity>();
            for (KITNewsEntity entity : list) {

                KITNewsEntity logEntity = log.get(entity.getUrl());
                if (logEntity == null) {
                    newEntities.add(entity);
                }
            }
            onOutput("KITニュースの新規エントリーは" + newEntities.size() + "個です。");

            for (KITNewsEntity entity : newEntities) {
                addTweetQueue(new TweetRequest().add("[KITニュース]" + entity.getTitle() + " " + entity.getUrl()));
            }

            if (newEntities.size() != 0) {
                log.clear();
                for (KITNewsEntity entity : list) {
                    log.put(entity.getUrl(), entity);
                }

                FileUtils.writeObjectToFile(log, LOG_KIT_NEWS);
            }

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            onOutput("KITニュースの取得でエラー");
        }
    }

    public void weather(String url, String proxyHost, int proxyPort) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd H:mm:ss:SSS");
        onOutput("KIT_info weather started," + outputDateFormat.format(calendar.getTime()));
        try {
            String str = new KanazawaWeatherHandler().get(url, proxyHost, proxyPort);
            if (str != null) {
                addTweetQueue(new TweetRequest().add(str));
            }
        } catch (Exception e) {
            e.printStackTrace();
            onOutput("天気予報取得でエラー");
        }
    }

    public void dayTweet() {

    }

    protected void onOutput(String text) {
        if (mListener != null) {
            mListener.onOutput(text);
        }
    }

    protected void addTweetQueue(TweetRequest req) {
        mTwitterManager.addRequest(req);
    }

    public void tweetQueue(boolean debug, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String proxyHost, int proxyPort) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd H:mm:ss:SSS");
        onOutput("KIT_info tweet session started," + outputDateFormat.format(calendar.getTime()));

        mTwitterManager.tweetRequests(mListener, debug, consumerKey, consumerSecret, accessToken, accessTokenSecret, proxyHost, proxyPort);

    }

}
