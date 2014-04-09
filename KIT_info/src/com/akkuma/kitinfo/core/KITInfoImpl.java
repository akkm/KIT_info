package com.akkuma.kitinfo.core;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.akkuma.kitinfo.core.announce.CommonAnnouncementDetailEntry;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementEntry;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.AuthenticateFailedException;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.CommonAnnouncementException;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.NotAuthenticatedException;
import com.akkuma.kitinfo.core.cancel.CanceledLectureEntry;
import com.akkuma.kitinfo.core.cancel.CanceledLectureHandler;
import com.akkuma.kitinfo.core.cancel.CanceledLectureHandler.CanceledLectureException;
import com.akkuma.kitinfo.core.news.KITNewsEntity;
import com.akkuma.kitinfo.core.news.KITNewsHandler;
import com.akkuma.kitinfo.core.supply.SupplementaryLectureEntry;
import com.akkuma.kitinfo.core.supply.SupplementaryLectureHandler;
import com.akkuma.kitinfo.core.supply.SupplementaryLectureHandler.SupplementaryLectureException;
import com.akkuma.kitinfo.core.twitter.TweetRequest;
import com.akkuma.kitinfo.core.twitter.TwitterManager;
import com.akkuma.kitinfo.core.weather.KanazawaWeatherHandler;
import com.akkuma.kitinfo.util.FileUtils;
import com.google.gson.reflect.TypeToken;

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

        cancelledLectureSession();
        supplementaryLectureSession();
        commonAnnouncementSession(portalId, portalPassword);
        kitNewsSession();

    }

    private static final String LOG_CANCELED_LECTURE = "canceled_lecture.json";
    private static final String LOG_SUPPLEMENTARY_LECTURE = "supplementary_lecture.json";
    private static final String LOG_KIT_NEWS = "kit_news.json";
    private static final String LOG_COMMON_ANNOUNCEMENT = "common_announcement.json";

    @SuppressWarnings("unchecked")
    public void commonAnnouncementSession(String portalId, String portalPassword) {

        // 共有告知
        CommonAnnouncementHandler commonAnnouncementHandler = new CommonAnnouncementHandler();
        try {
            onOutput("共有告知を取得します。");
            Map<Integer, CommonAnnouncementEntry> log = (Map<Integer, CommonAnnouncementEntry>) FileUtils.read(LOG_COMMON_ANNOUNCEMENT,
                    new TypeToken<Map<Integer, CommonAnnouncementEntry>>() {}.getType());
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

            if (newDetailEntries.size() != 0 || cancelledEntries.size() != 0) {
                log.clear();
                for (CommonAnnouncementEntry entry : entries) {
                    log.put(entry.getId(), entry);
                }

                FileUtils.write(log, LOG_COMMON_ANNOUNCEMENT, new TypeToken<Map<Integer, CommonAnnouncementEntry>>() {}.getType());
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
    public void cancelledLectureSession() {

        CanceledLectureHandler handler = new CanceledLectureHandler();

        try {
            onOutput("休講情報を取得します。");
            Map<Integer, CanceledLectureEntry> log = (Map<Integer, CanceledLectureEntry>) FileUtils.read(LOG_CANCELED_LECTURE, new TypeToken<Map<Integer, CanceledLectureEntry>>() {}.getType());
            if (log == null) {
                log = new HashMap<Integer, CanceledLectureEntry>();
                onOutput("休講情報のログファイルを新規作成しました。");
            }

            ArrayList<CanceledLectureEntry> list = handler.get();

            onOutput("休講情報エントリーは" + list.size() + "個です。");

            if (list.size() == 0) {
                return;
            }

            boolean updated = true;
            for (CanceledLectureEntry entry : list) {
                CanceledLectureEntry logEntry = log.get(entry.getHashCode());
                if (logEntry == null) {
                    updated = true;
                    TweetRequest req = new TweetRequest();
                    req.add("【更新・休講】" + entry.toOutputString());
                    addTweetQueue(req);
                }

            }

            if (updated) {
                log.clear();

                for (CanceledLectureEntry entry : list) {
                    log.put(entry.getHashCode(), entry);
                }

                FileUtils.write(log, LOG_CANCELED_LECTURE, new TypeToken<Map<Integer, CanceledLectureEntry>>() {}.getType());
            }
        } catch (CanceledLectureException e) {
            e.printStackTrace();
            onOutput("休講情報の取得でエラー");
        }

    }

    @SuppressWarnings("unchecked")
    public void supplementaryLectureSession() {

        SupplementaryLectureHandler handler = new SupplementaryLectureHandler();

        try {
            onOutput("補講情報を取得します。");
            Map<Integer, SupplementaryLectureEntry> log = (Map<Integer, SupplementaryLectureEntry>) FileUtils.read(LOG_SUPPLEMENTARY_LECTURE, new TypeToken<Map<Integer, SupplementaryLectureEntry>>() {}.getType());
            if (log == null) {
                log = new HashMap<Integer, SupplementaryLectureEntry>();
                onOutput("補講情報のログファイルを新規作成しました。");
            }

            ArrayList<SupplementaryLectureEntry> list = handler.get();

            onOutput("補講情報エントリーは" + list.size() + "個です。");

            if (list.size() == 0) {
                return;
            }

            boolean updated = true;
            for (SupplementaryLectureEntry entry : list) {
                SupplementaryLectureEntry logEntry = log.get(entry.getHashCode());
                if (logEntry == null) {
                    updated = true;
                    TweetRequest req = new TweetRequest();
                    req.add("【更新・補講】" + entry.toOutputString());
                    addTweetQueue(req);
                }

            }

            if (updated) {
                log.clear();

                for (SupplementaryLectureEntry entry : list) {
                    log.put(entry.getHashCode(), entry);
                }

                FileUtils.write(log, LOG_SUPPLEMENTARY_LECTURE, new TypeToken<Map<Integer, SupplementaryLectureEntry>>() {}.getType());
            }
        } catch (SupplementaryLectureException e) {
            e.printStackTrace();
            onOutput("補講情報の取得でエラー");
        }

    }
    
    @SuppressWarnings("unchecked")
    public void kitNewsSession() {

        // KITニュース
        KITNewsHandler kitNewsHandler = new KITNewsHandler();
        try {
            onOutput("KITニュースを取得します。");
            Map<String, KITNewsEntity> log = (Map<String, KITNewsEntity>) FileUtils.read(LOG_KIT_NEWS, new TypeToken<Map<String, KITNewsEntity>>() {}.getType());
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

                FileUtils.write(log, LOG_KIT_NEWS, new TypeToken<Map<String, KITNewsEntity>>() {}.getType());
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
        try {
            ArrayList<CanceledLectureEntry> canceledLectureEntries = new CanceledLectureHandler().get();

            for (CanceledLectureEntry entry : canceledLectureEntries) {
                if (entry.isTodayEntry() && !entry.isCanceled()) {
                    addTweetQueue(new TweetRequest("【今日の休講】" + entry.toOutputString(false)));
                }
            }
        } catch (CanceledLectureException e) {
            e.printStackTrace();
            onOutput("休講情報の取得でエラー");
        }
        
        try {
            ArrayList<SupplementaryLectureEntry> supplementaryLectureEntries = new SupplementaryLectureHandler().get();

            for (SupplementaryLectureEntry entry : supplementaryLectureEntries) {
                if (entry.isTodayEntry() && !entry.isCanceled()) {
                    addTweetQueue(new TweetRequest("【今日の補講】" + entry.toOutputString(false)));
                }
            }
        } catch (SupplementaryLectureException e) {
            e.printStackTrace();
            onOutput("補講情報の取得でエラー");
        }
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
