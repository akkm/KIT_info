package com.akkuma.kitinfo.core.weather;

import java.io.IOException;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class KanazawaWeatherHandler {
    
    public String get(String url) throws ParserConfigurationException, SAXException, IOException {
        
        String str = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(url);
        Element root = document.getDocumentElement();

        NodeList itemList = root.getElementsByTagName("item");

        Calendar cal = Calendar.getInstance();
        int date = cal.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < itemList.getLength(); i++) {

            Element element = (Element) itemList.item(i);

            NodeList desc = element.getElementsByTagName("description");
            NodeList title = element.getElementsByTagName("title");
            NodeList link = element.getElementsByTagName("link");

            try {
                int nextDate = Integer.parseInt(title.item(0).getFirstChild()
                        .getNodeValue().split("日")[0].substring(2));
                if (nextDate == date) {
                    String twtString = "[今日の金沢の天気] "
                            + desc.item(0).getFirstChild().getNodeValue() + " "
                            + link.item(0).getFirstChild().getNodeValue();
                    str = twtString;
                    break;
                }
            } catch (NumberFormatException e) {}
        }
        
        return str;
    }

}
