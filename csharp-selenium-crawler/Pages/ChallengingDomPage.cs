using System;
using System.Collections.Generic;
using System.Linq;
using OpenQA.Selenium;

namespace Crawler.Pages
{
    public class ChallengingDomPage
    {
        private IWebDriver _driver;
        private const string Url = "https://the-internet.herokuapp.com/challenging_dom";

        public ChallengingDomPage(IWebDriver driver)
        {
            _driver = driver;
        }

        public void Open()
        {
            _driver.Navigate().GoToUrl(Url);
        }

        public string GetUrl()
        {
            return Url;
        }

        public List<LinkInfo> GetFeatureLinks()
        {
            var links = _driver.FindElements(By.TagName("a"));
            var featureLinks = new List<LinkInfo>();

            foreach (var link in links)
            {
                var name = link.Text.Trim();
                var href = link.GetAttribute("href");

                if (!string.IsNullOrEmpty(name) && !string.IsNullOrEmpty(href))
                {
                    featureLinks.Add(new LinkInfo { Name = name, Url = href });
                }
            }

            return featureLinks;
        }
    }

    public class LinkInfo
    {
        public string Name { get; set; } = "";
        public string Url { get; set; } = "";
    }
}
