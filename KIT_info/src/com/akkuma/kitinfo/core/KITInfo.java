package com.akkuma.kitinfo.core;

import com.akkuma.kitinfo.core.twitter.TweetRequest;

public abstract class KITInfo {

    public static KITInfo getInstance() {
        return new KITInfoImpl();
    }

    public abstract void setDebugOutputListener(DebugOutputListener listener);

    public abstract void destroy();

    public abstract void start(String portalId, String portalPassword);;

    public abstract void commonAnnouncementSession(String portalId, String portalPassword);

    public abstract void kitNewsSession();

    public abstract void weather(String url, String proxyHost, int proxyPort);

    public abstract void dayTweet();

    protected abstract void onOutput(String text);

    protected abstract void addTweetQueue(TweetRequest req);

    public abstract void tweetQueue(boolean debug, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String proxyHost, int proxyPort);

    public static interface DebugOutputListener {
        public void onOutput(String text);
    }

}
