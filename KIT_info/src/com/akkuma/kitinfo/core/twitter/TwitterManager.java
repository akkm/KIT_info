package com.akkuma.kitinfo.core.twitter;

import com.akkuma.kitinfo.core.KITInfo.DebugOutputListener;

public abstract class TwitterManager {
    
    public static TwitterManager getInstance() {
        return new TwitterManagerImpl();
    }
    
    public abstract void addRequest(TweetRequest req);
    
    public abstract void tweetRequests(DebugOutputListener out,boolean debug,String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String proxyHost, int proxyPort);

}
