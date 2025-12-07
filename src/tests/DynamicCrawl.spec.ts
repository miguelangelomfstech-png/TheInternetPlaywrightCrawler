import { test, expect } from '@playwright/test';
import { ChallengingDomPage } from '../pages/ChallengingDomPage';
import { PerformanceService } from '../utils/PerformanceService';
import { ReporterService } from '../utils/ReporterService';

test.describe('Dynamic Crawler - Challenging DOM', () => {
  let challengingDomPage: ChallengingDomPage;
  const performanceService = new PerformanceService();
  const reporterService = new ReporterService();

  test.beforeEach(async ({ page }) => {
    challengingDomPage = new ChallengingDomPage(page);
  });

  test.afterAll(async () => {
    reporterService.printSummary();
    reporterService.printAsciiChart();
    reporterService.saveReport('src/reports/results.json');
    reporterService.saveHtmlReport('src/reports/results.html');
  });

  test('Crawl and interact with detected links', async ({ page }) => {
    const totalSuiteTimer = new PerformanceService();
    totalSuiteTimer.startTimer();

    await test.step('Navigate to Base Page', async () => {
      await challengingDomPage.goto();
    });

    const links = await test.step('Detect Feature Links', async () => {
      const detected = await challengingDomPage.getFeatureLinks();
      console.log(`Detected ${detected.length} links.`);
      return detected;
    });

    for (const link of links) {
      await test.step(`Process Link: ${link.name}`, async () => {
        performanceService.resetTimer();
        performanceService.startTimer();

        try {
          console.log(`Interacting with: ${link.name} (${link.url})`);
          
          // Using a soft assertion to check if the link is visible before clicking
          await expect.soft(page.locator(`a[href="${link.url}"]`).first()).toBeVisible();

          // Prepare for potential navigation
          // If the link is internal (hash), it won't trigger a full page load usually, but sometimes they do.
          // If it is external, it will.
          // We will attempt to click and see what happens.
          
          // Depending on the link type, we might need to wait for load or just click.
          // We'll use a try-catch block for the interaction.
          try {
             const linkLocator = page.locator(`a:has-text("${link.name}")`).first();
             // Click the link
             await linkLocator.click({ timeout: 5000 });
             
             // Dynamic Interaction with common elements if we navigated to a new page
             // or just checking if we are still on the same page but state changed.
             // The requirements say "dynamically attempt interaction with common elements".
             // We will check for inputs/buttons briefly.
             
             const inputs = page.locator('input:visible');
             if (await inputs.count() > 0) {
                 await inputs.first().fill('Test Interaction').catch(() => {});
             }
             
             const buttons = page.locator('button:visible');
              if (await buttons.count() > 0) {
                 // specific buttons might be sensitive, so we just hover or check visibility
                 await expect.soft(buttons.first()).toBeVisible();
             }

          } catch (interactionError) {
             console.error(`Error interacting with ${link.name}:`, interactionError);
             // Take screenshot on failure
             const screenshotPath = `src/reports/screenshots/error_${link.name.replace(/[^a-z0-9]/gi, '_')}.png`;
             await page.screenshot({ path: screenshotPath });
             console.log(`Screenshot saved to ${screenshotPath}`);
             throw interactionError; // Re-throw to be caught by outer catch if needed or just handled
          }

          performanceService.stopTimer();
          reporterService.logResult({
            name: link.name,
            url: link.url,
            status: 'passed',
            duration: performanceService.getDuration()
          });

        } catch (error) {
          performanceService.stopTimer();
          reporterService.logResult({
            name: link.name,
            url: link.url,
            status: 'failed',
            duration: performanceService.getDuration(),
            error: error instanceof Error ? error.message : String(error)
          });
        } finally {
            // Navigate back if we moved away from the base URL or simply reset state
            // Checking current URL
            if (page.url() !== challengingDomPage.url) {
                await page.goBack().catch(async () => {
                    // Fallback if goBack fails
                    await challengingDomPage.goto();
                });
            }
        }
      });
    }

    totalSuiteTimer.stopTimer();
    reporterService.setTotalDuration(totalSuiteTimer.getDuration());
  });
});
