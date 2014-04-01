package com.akkuma.kitinfo.core.twitter;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.akkuma.kitinfo.core.KITInfo.DebugOutputListener;
import com.akkuma.kitinfo.util.FileUtils;

class TwitterManagerImpl extends TwitterManager {

    private static final String LOG_NEXT_TWEET_LIST = "next_tweet.log";

    ArrayList<TweetRequest> mRequests = new ArrayList<TweetRequest>();

    TwitterManagerImpl() {
    }

    @Override
    public void addRequest(TweetRequest req) {
        mRequests.add(req);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void tweetRequests(DebugOutputListener out, boolean debug, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String proxyHost, int proxyPort) {

        ArrayList<TweetRequest> queueLog = (ArrayList<TweetRequest>) FileUtils.readObjectFromFile(LOG_NEXT_TWEET_LIST);
        if (queueLog != null) {
            mRequests.addAll(0, queueLog);
        }

        if (out != null) {
            out.onOutput("ツイートキュー数:" + mRequests.size());
        }

        ArrayList<TweetRequest> nextQueueLog = new ArrayList<TweetRequest>();

        while (mRequests.size() > 7) {
            nextQueueLog.add(mRequests.remove(7));
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

        for (TweetRequest req : mRequests) {

            for (String str : req.getList()) {

                try {
                    if (out != null) {
                        out.onOutput("ツイート:" + str);
                    }
                    if (!debug) {
                        twitter.updateStatus(str);
                    }
                } catch (TwitterException e) {
                    e.printStackTrace();
                    if (out != null) {
                        out.onOutput("ツイート失敗");
                        out.onOutput(e.toString());
                    }
                    if (e.getErrorCode() != 187) {
                        nextQueueLog.add(req);
                        break;
                    }
                }
            }
        }

        if (queueLog == null || queueLog.size() != 0 || nextQueueLog.size() != 0) {
            FileUtils.writeObjectToFile(nextQueueLog, LOG_NEXT_TWEET_LIST);
        }

    }
    
    ArrayList<TweetRequest> getRequests() {
        return mRequests;
    }

}
