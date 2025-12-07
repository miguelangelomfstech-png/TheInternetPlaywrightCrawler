package com.crawler.tests;

import com.crawler.pages.ChallengingDomPage;
import com.crawler.utils.PerformanceService;
import com.crawler.utils.ReporterService;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class DynamicCrawlTest {
    private WebDriver driver;
    private ChallengingDomPage challengingDomPage;
    private PerformanceService performanceService;
    private ReporterService reporterService;
    private PerformanceService suiteTimer;

    @BeforeClass
    public void setup() {
        // Setup Chrome Driver
        // Assumes chromedriver is in path or managed by Selenium Manager (Selenium
        // 4.6+)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Run headless for CI/Speed, remove for debug
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        challengingDomPage = new ChallengingDomPage(driver);
        performanceService = new PerformanceService();
        reporterService = new ReporterService();
        suiteTimer = new PerformanceService();

        suiteTimer.startTimer();
    }

    @Test
    public void crawlAndInteract() {
        challengingDomPage.open();
        List<Map<String, String>> links = challengingDomPage.getFeatureLinks();
        System.out.println("Detected " + links.size() + " links.");

        SoftAssert softAssert = new SoftAssert();

        for (Map<String, String> link : links) {
            String name = link.get("name");
            String url = link.get("url");

            performanceService.resetTimer();
            performanceService.startTimer();

            try {
                System.out.println("Interacting with: " + name + " (" + url + ")");

                // Interaction Logic
                // Re-finding element to avoid StaleElementReferenceException
                // In a real crawl, we might need more robust locators or to not re-find all the
                // time if stable.
                // Since clicking might reload the page, we should handle getting back.

                List<WebElement> currentLinks = driver.findElements(By.tagName("a"));
                WebElement targetLink = null;

                // Simple heuristic to find the link again
                for (WebElement l : currentLinks) {
                    if (l.getAttribute("href") != null && l.getAttribute("href").equals(url)) {
                        targetLink = l;
                        break;
                    }
                }

                if (targetLink != null && targetLink.isDisplayed()) {
                    targetLink.click();
                } else {
                    // Try finding by text if href failed
                    driver.findElement(By.linkText(name)).click();
                }

                // Dynamic Interaction (fill inputs if any)
                List<WebElement> inputs = driver.findElements(By.tagName("input"));
                if (!inputs.isEmpty()) {
                    try {
                        inputs.get(0).sendKeys("Test Interaction");
                    } catch (Exception e) {
                        // ignore
                    }
                }

                performanceService.stopTimer();
                reporterService.logResult(name, url, "passed", performanceService.getDuration(), null);

            } catch (Exception e) {
                performanceService.stopTimer();
                System.err.println("Error interacting with " + name + ": " + e.getMessage());

                // Screenshot
                takeScreenshot("error_" + name.replaceAll("[^a-zA-Z0-9]", "_"));

                reporterService.logResult(name, url, "failed", performanceService.getDuration(), e.getMessage());
                softAssert.fail("Failed interaction with " + name);
            } finally {
                // Navigate back if URL changed significantly
                if (!driver.getCurrentUrl().contains("challenging_dom")) {
                    driver.navigate().back();
                } else {
                    // Sometimes buttons just change state but same URL.
                    // Ideally we ensure we are at base state.
                }
            }
        }

        softAssert.assertAll();
    }

    @AfterClass
    public void tearDown() {
        suiteTimer.stopTimer();
        reporterService.setTotalDuration(suiteTimer.getDuration());

        reporterService.printSummary();
        reporterService.printAsciiChart();
        reporterService.saveReport("src/reports/results.json");
        reporterService.saveHtmlReport("src/reports/results.html");

        if (driver != null) {
            driver.quit();
        }
    }

    private void takeScreenshot(String name) {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            Path destPath = Paths.get("src/reports/screenshots/" + name + ".png");
            if (destPath.getParent() != null)
                Files.createDirectories(destPath.getParent());
            Files.copy(srcFile.toPath(), destPath);
            System.out.println("Screenshot saved: " + destPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
