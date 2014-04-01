package com.akkuma.kitinfo.core;

import java.io.IOException;
import java.util.ArrayList;
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
import com.akkuma.kitinfo.util.FileUtils;

@SuppressWarnings("unchecked")
public abstract class KITInfoParser {

    private static final String LOG_KIT_NEWS = "kit_news.log";
    private static final String LOG_COMMON_ANNOUNCEMENT = "common_announcement.log";

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
                addTweetQueue("[共通告知]【※取り消し】" + entry.getTitle().replace("　", ""));
            }
            
            for (CommonAnnouncementDetailEntry entry : newDetailEntries) {
                addTweetQueue("[共通告知]" + entry.getTitle().replace("　", ""));
                addTweetQueue("内容:" + entry.getBody().replace("　", ""));
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
                addTweetQueue("[KITニュース]" + entity.getTitle() + " " + entity.getUrl());
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

    protected abstract void addTweetQueue(String string);

    protected abstract void onOutput(String string);

}
