package com.akkuma.kitinfo.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.akkuma.kitinfo.core.weather.KanazawaWeatherHandler;
import com.akkuma.kitinfo.util.FileUtils;

/**
 * 
 * @author Yamada Atsuto
 * 
 */

@SuppressWarnings("unchecked")
public final class KITInfo extends KITInfoParser {

    private static final String LOG_NEXT_TWEET_LIST = "next_tweet.log";

    private DebugOutputListener mListener;
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

        commonAnnouncementSession(portalId, portalPassword);
        kitNewsSession();

    }

    public void weather(String url, String proxyHost, int proxyPort) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy/MM/dd H:mm:ss:SSS");
        onOutput("KIT_info weather started," + outputDateFormat.format(calendar.getTime()));
        try {
            String str = new KanazawaWeatherHandler().get(url, proxyHost, proxyPort);
            if (str != null) {
                addTweetQueue(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
            onOutput("天気予報取得でエラー");
        }
    }

    public void dayTweet() {

    }

    @Override
    protected void onOutput(String text) {
        if (mListener != null) {
            mListener.onOutput(text);
        }
    }

    @Override
    protected void addTweetQueue(String text) {
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

            urlMatcher = urlPattern.matcher(str);
            if (urlMatcher.find()) {
                dummyTweetString = urlMatcher.replaceAll("https://t.co/AAAAAAAAAA");
            } else {
                dummyTweetString = text;
            }
            
            boolean setLeader = false;
            if (dummyTweetString.length() > 140) {
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

        while (mTweetQueue.size() > 7) {
            nextQueueLog.add(mTweetQueue.remove(7));
        }

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
                onOutput(e.toString());
                nextQueueLog.add(str);
            }

        }

        if (queueLog.size() != 0 || nextQueueLog.size() != 0) {
            FileUtils.writeObjectToFile(nextQueueLog, LOG_NEXT_TWEET_LIST);
        }

    }

    public static interface DebugOutputListener {
        public void onOutput(String text);
    }
}
