package com.akkuma.kitinfo.core.twitter;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TwitterManagerImplTest {

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testAddRequest() {
        TwitterManagerImpl target = new TwitterManagerImpl();
        target.addRequest(new TweetRequest());
        assertEquals(target.getRequests().size(), 1);
    }

    @Test
    public void testTweetRequests() {
    }

}
