package com.akkuma.kitinfo.core.news;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class KITNewsHandler {

    private static final String KITNEWSRSS_URL = "http://www.kanazawa-it.ac.jp/kitnews/rss.xml";
    
    public ArrayList<KITNewsEntity> get(String proxyHost, int port) throws IOException, ParserConfigurationException, SAXException {
        
        ArrayList<KITNewsEntity> list = new ArrayList<KITNewsEntity>();
        URL url = new URL(KITNEWSRSS_URL);
        URLConnection connection;
        if (proxyHost != null && !proxyHost.isEmpty()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
            connection = url.openConnection(proxy);
        } else {
            connection = url.openConnection();
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(connection.getInputStream());

        Element root = document.getDocumentElement();

        NodeList itemList = root.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {

            KITNewsEntity entity = new KITNewsEntity();
            Element element = (Element) itemList.item(i);

            NodeList itemUrl = element.getElementsByTagName("link");
            NodeList itemTitle = element.getElementsByTagName("title");
            NodeList itemDesc = element.getElementsByTagName("description");
            
            entity.setUrl(itemUrl.item(0).getFirstChild().getNodeValue());
            entity.setTitle(itemTitle.item(0).getFirstChild().getNodeValue());
            entity.setDescription(itemDesc.item(0).getFirstChild().getNodeValue());
            
            list.add(entity);
        }
        
        return list;
        
    }

}
