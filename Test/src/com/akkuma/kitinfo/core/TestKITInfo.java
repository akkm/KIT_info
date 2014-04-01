package com.akkuma.kitinfo.core;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestKITInfo {
    
    private Pattern mUrlPattern = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.MULTILINE);
    

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testAddTweetQueue() {
        
        KITInfo info = new KITInfo();
        info.addTweetQueue("テストツイート");
        assertEquals(info.getTweetQueue().size(), 1);
        info.getTweetQueue().clear();
        
        info.addTweetQueue("あああ https://www.google.co.jp/search?q=aa&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:ja:official&hl=ja&client=firefox-a#hl=ja&q=%E3%81%86%E3%82%8F%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82&rls=org.mozilla:ja:official");
        assertEquals(info.getTweetQueue().size(), 1);
        info.getTweetQueue().clear();
        
        info.addTweetQueue("あああ https://www.google.co.jp/search?q=aa&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:ja:official&hl=ja&client=firefox-a#hl=ja&q=%E3%81%86%E3%82%8F%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82&rls=org.mozilla:ja:official");
        assertEquals(info.getTweetQueue().size(), 1);
        info.getTweetQueue().clear();
        
        info.addTweetQueue("あああ https://www.google.co.jp/search?q=aa&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:ja:official&hl=ja&client=firefox-a#hl=ja&q=%E3%81%86%E3%82%8F%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82%E3%81%82&rls=org.mozilla:ja:official");
        assertEquals(info.getTweetQueue().size(), 1);
        info.getTweetQueue().clear();
        
        info.addTweetQueue(
//                "平成２６年度前学期の履修計画修正を次の日程で受け付けます。\n"
//                + "◆履修計画修正期間\n"
//                + "平成２６年度４月４日(金)８:３０ ～ 平成２６年４月５日（土）１３:００\n"
//                + "☆５日１３：００以降の申請は一切受け付けできません。\n\n"
//                 "◆学生ポータル内の履修申請システムから修正を受け付けます。\n\n"
                 "aaa◆履修許可の確認\n"
                + "履修計画修正を行った翌日に履修申請システムより確認してください。\n\n"
                + "◆「履修申請の手引き」について\n"
                + "履修申請の手引きには授業科目の開講期や授業の実施方法等が説明されています。\n"
                + "http://mercury.kanazawa-it.ac.jp/kyoumu/"
                + "平成２５年度後学期までの修学状況をふまえ、履修申請の手引きを確認した上で履修計画修正を行ってください。\n\n"
                + "冊子の配付：１号館２階の学生コミュニティセンター前\n"
                + "履修申請の手引きの閲覧、訂正個所の確認については教務課ホームページで確認してください。\n"
                + "http://mercury.kanazawa-it.ac.jp/kyoumu/"
                );
        assertEquals(info.getTweetQueue().size(), 2);
        
        String first = info.getTweetQueue().get(0).replaceAll("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", "https://t.co/AAAAAAAAAA");
        String second = info.getTweetQueue().get(1).replaceAll("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", "https://t.co/AAAAAAAAAA");
        
        System.out.println("1st:"+first);
        System.out.println("2nd:"+second);
        System.out.println(first.length());
        System.out.println(second.length());
        
        
        
        assertTrue(first.length() <= 140);
        assertTrue(second.length() <= 140);
        
    }

}
