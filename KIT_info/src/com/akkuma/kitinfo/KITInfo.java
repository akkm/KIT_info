package com.akkuma.kitinfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import twitter4j.Twitter;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.akkuma.kitinfo.core.announce.CommonAnnouncementDetailEntry;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementEntry;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.AuthenticateFailedException;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.CommonAnnouncementException;
import com.akkuma.kitinfo.core.announce.CommonAnnouncementHandler.NotAuthenticatedException;
import com.akkuma.kitinfo.core.weather.KanazawaWeatherHandler;
import com.akkuma.kitinfo.util.FileUtils;

/**
 * 
 * @author Yamada Atsuto
 * 
 */
public class KITInfo {

    private static final String LOG_COMMON_ANNOUNCEMENT = "common_announcement.log";
    private static final String LOG_NEXT_TWEET_LIST = "next_tweet.log";

    private DebugOutputListener mListener;
    private CommonAnnouncementHandler mCommonAnnouncementHandler;
    private ArrayList<String> mTweetQueue = new ArrayList<String>();

    public KITInfo() {}

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

        // 共有告知
        mCommonAnnouncementHandler = new CommonAnnouncementHandler();
        try {
            onOutput("共有告知を取得します。");
            HashMap<Integer, CommonAnnouncementEntry> log = (HashMap<Integer, CommonAnnouncementEntry>) FileUtils.readObjectFromFile(LOG_COMMON_ANNOUNCEMENT);
            if (log == null) {
                log = new HashMap<Integer, CommonAnnouncementEntry>();
                onOutput("共有告知のログファイルを新規作成しました。");
            }

            mCommonAnnouncementHandler.authenticate(portalId, portalPassword);
            ArrayList<CommonAnnouncementEntry> entries = mCommonAnnouncementHandler.get();

            onOutput("学生ポータルより共有告知を" + entries.size() + "個取得しました。");

            if (entries.size() != 0) {
                ArrayList<CommonAnnouncementDetailEntry> newDetailEntries = new ArrayList<CommonAnnouncementDetailEntry>();

                for (CommonAnnouncementEntry entry : entries) {
                    CommonAnnouncementEntry logEntry = log.get(entry.getId());
                    if (logEntry == null || logEntry.isCanceled() != entry.isCanceled()) {
                        CommonAnnouncementDetailEntry detailEntry = mCommonAnnouncementHandler.getDetailEntry(entry.getId(), entry.getCONTCHECK());
                        newDetailEntries.add(detailEntry);
                    }
                }
                onOutput("共通告知の新規エントリーは" + newDetailEntries.size() + "個です。");

                for (CommonAnnouncementDetailEntry entry : newDetailEntries) {
                    addTweetQueue("[共通告知]" + entry.getTitle().replace("　", ""));
                    addTweetQueue("内容:" + entry.getBody().replace("　", ""));
                }

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
            mCommonAnnouncementHandler.destroy();
        }

    }

    public void weather(String url) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd H:mm:ss:SSS");
        onOutput("KIT_info weather started," + outputDateFormat.format(calendar.getTime()));
        try {
            addTweetQueue(new KanazawaWeatherHandler().get(url));
        } catch (Exception e) {
            e.printStackTrace();
            onOutput("天気予報取得でエラー");
        }
    }
    
    public void dayTweet() {
        
    }

    private void onOutput(String text) {
        if (mListener != null) {
            mListener.onOutput(text);
        }
    }

    private void addTweetQueue(String text) {
        // URLの存在をチェック
        Pattern urlPattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.MULTILINE);
        Matcher urlMatcher = urlPattern.matcher(text);
        String dummyTweetString;
        if (urlMatcher.find()) {
            dummyTweetString = urlMatcher.replaceAll("https://t.co/AAAAAAAAAA");
        } else {
            dummyTweetString = text;
        }

        if (dummyTweetString.length() <= 140) {
            mTweetQueue.add(text);
        } else {
            String str = text;
            String first = str.substring(0, 139);
            mTweetQueue.add(first + "…");

            str = str.substring(139, str.length());
            boolean setLeader = false;
            if (str.length() > 140) {
                setLeader = true;
            }
            String second = str.substring(0, setLeader ? 136 : str.length());
            if (setLeader) {
                mTweetQueue.add(second + "（省略）");
            } else {
                mTweetQueue.add(second);
            }
        }
    }

    public void tweetQueue(boolean debug, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String proxyHost, int proxyPort) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd H:mm:ss:SSS");
        onOutput("KIT_info tweet session started," + outputDateFormat.format(calendar.getTime()));

        ArrayList<String> queueLog = (ArrayList<String>) FileUtils.readObjectFromFile(LOG_NEXT_TWEET_LIST);
        if (queueLog != null) {
            mTweetQueue.addAll(0, queueLog);
        }

        onOutput("ツイートキュー数:" + mTweetQueue.size());

        ArrayList<String> nextQueueLog = new ArrayList<String>();

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerKey);
        builder.setOAuthConsumerSecret(consumerSecret);
        builder.setOAuthAccessToken(accessToken);
        builder.setOAuthAccessTokenSecret(accessTokenSecret);
        if (!proxyHost.isEmpty()) {
            builder.setHttpProxyHost(proxyHost);
            builder.setHttpProxyPort(proxyPort);
        }

        Twitter twitter = new TwitterFactory(builder.build()).getInstance();

        for (String str : mTweetQueue) {

            try {
                onOutput("ツイート:" + str);
                if (!debug) {
                    twitter.updateStatus(str);
                }
            } catch (TwitterException e) {
                e.printStackTrace();
                onOutput("ツイート失敗:" + str);
                nextQueueLog.add(str);
            }

        }

        FileUtils.writeObjectToFile(nextQueueLog, LOG_NEXT_TWEET_LIST);

    }

    public static interface DebugOutputListener {
        public void onOutput(String text);
    }
}
