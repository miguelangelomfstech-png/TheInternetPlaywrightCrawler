package com.crawler.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ChallengingDomPage {
    private WebDriver driver;
    private final String url = "https://the-internet.herokuapp.com/challenging_dom";

    public ChallengingDomPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open() {
        driver.get(url);
    }

    public String getUrl() {
        return url;
    }

    public List<Map<String, String>> getFeatureLinks() {
        List<Map<String, String>> featureLinks = new ArrayList<>();
        List<WebElement> links = driver.findElements(By.tagName("a"));

        for (WebElement link : links) {
            String name = link.getText().trim();
            String href = link.getAttribute("href");

            if (!name.isEmpty() && href != null) {
                Map<String, String> linkData = new HashMap<>();
                linkData.put("name", name);
                linkData.put("url", href);
                featureLinks.add(linkData);
            }
        }
        return featureLinks;
    }
}
