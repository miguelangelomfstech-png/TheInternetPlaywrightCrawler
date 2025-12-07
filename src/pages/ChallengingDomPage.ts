import { Page, Locator } from '@playwright/test';

/**
 * Page Object Model for the Challenging DOM page.
 */
export class ChallengingDomPage {
  readonly page: Page;
  readonly url = 'https://the-internet.herokuapp.com/challenging_dom';

  constructor(page: Page) {
    this.page = page;
  }

  /**
   * Navigates to the Challenging DOM page.
   */
  async goto(): Promise<void> {
    await this.page.goto(this.url);
  }

  /**
   * Dynamically detects all visible feature links on the page.
   * Returns an array of objects containing the link name and url.
   * Filters out empty links or links that are not relevant if needed.
   * 
   * @returns Promise<{ name: string, url: string }[]>
   */
  async getFeatureLinks(): Promise<{ name: string, url: string }[]> {
    // Select all anchor tags that are visible
    const links = this.page.locator('a:visible');
    const count = await links.count();
    const headers = [];

    for (let i = 0; i < count; i++) {
      const link = links.nth(i);
      const name = await link.innerText();
      const href = await link.getAttribute('href');

      // Basic filtering to ensure we have meaningful links
      // We also might want to exclude the "Elemental Selenium" footer link or "Fork me on GitHub" if we only want "feature" links
      // For now, adhering to "all visible feature links", but I will try to target the main content area mainly if possible.
      // However, the prompt asks for "all detected links". I will exclude empty names.
      if (name && href) {
         headers.push({ name: name.trim(), url: href });
      }
    }
    
    return headers;
  }
}
