using System;
using System.Linq;
using Crawler.Pages;
using Crawler.Utils;
using OpenQA.Selenium;
using OpenQA.Selenium.Chrome;

namespace Crawler
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("Starting Crawler...");
            
            // Setup Services
            var performanceService = new PerformanceService();
            var reporterService = new ReporterService();
            var suiteTimer = new PerformanceService();

            // Setup WebDriver
            var options = new ChromeOptions();
            // options.AddArgument("--headless=new"); // Uncomment for headless mode
            IWebDriver driver = new ChromeDriver(options);
            driver.Manage().Timeouts().ImplicitWait = TimeSpan.FromSeconds(5);

            try
            {
                suiteTimer.StartTimer();

                var page = new ChallengingDomPage(driver);
                page.Open();
                
                var links = page.GetFeatureLinks();
                Console.WriteLine($"Detected {links.Count} links.");

                foreach (var link in links)
                {
                    performanceService.ResetTimer();
                    performanceService.StartTimer();

                    try
                    {
                        Console.WriteLine($"Interacting with: {link.Name} ({link.Url})");

                        // Interaction Logic
                        // Re-finding logic similar to Java version
                        var currentLinks = driver.FindElements(By.TagName("a"));
                        IWebElement? targetLink = null;

                        foreach(var l in currentLinks)
                        {
                            if (l.GetAttribute("href") == link.Url)
                            {
                                targetLink = l;
                                break;
                            }
                        }

                        if (targetLink != null && targetLink.Displayed)
                        {
                             targetLink.Click();
                        }
                        else
                        {
                             driver.FindElement(By.LinkText(link.Name)).Click();
                        }

                        // Interact with Inputs
                        var inputs = driver.FindElements(By.TagName("input"));
                        if (inputs.Count > 0)
                        {
                            try { inputs[0].SendKeys("Test Interaction"); } catch { }
                        }

                        performanceService.StopTimer();
                        reporterService.LogResult(link.Name, link.Url, "passed", performanceService.GetDuration());

                    }
                    catch (Exception ex)
                    {
                        performanceService.StopTimer();
                        Console.Error.WriteLine($"Error interacting with {link.Name}: {ex.Message}");
                        
                        // Screenshot
                        try
                        {
                            var screenshot = ((ITakesScreenshot)driver).GetScreenshot();
                            var filename = $"reports/screenshots/error_{System.Text.RegularExpressions.Regex.Replace(link.Name, "[^a-zA-Z0-9]", "_")}.png";
                            Directory.CreateDirectory(Path.GetDirectoryName(filename)!);
                            screenshot.SaveAsFile(filename);
                            Console.WriteLine($"Screenshot saved: {filename}");
                        }
                        catch { }

                        reporterService.LogResult(link.Name, link.Url, "failed", performanceService.GetDuration(), ex.Message);
                    }
                    finally
                    {
                        if (!driver.Url.Contains("challenging_dom"))
                        {
                            driver.Navigate().Back();
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine("Fatal Error: " + ex.Message);
            }
            finally
            {
                suiteTimer.StopTimer();
                reporterService.SetTotalDuration(suiteTimer.GetDuration());
                
                reporterService.PrintSummary();
                reporterService.PrintAsciiChart();
                reporterService.SaveReport("reports/results.json");
                reporterService.SaveHtmlReport("reports/results.html");

                driver.Quit();
                Console.WriteLine("Crawler Finished. Press Enter to exit.");
                Console.ReadLine();
            }
        }
    }
}
