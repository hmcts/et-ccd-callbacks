import { expect, test } from '@playwright/test';
import CaseDetailsPage from '../pages/caseDetailsPage';

test.beforeEach(async ({ page }) => {
  await page.setContent(`
    <style>mat-tab-body, .mat-tab-body-content { display: block; }</style>
    <div role="tablist">
      <div role="tab" aria-label="Case Details, Please use left and right arrow keys to navigate case tabs"
           aria-selected="true" aria-controls="details"><div>Case Details</div></div>
      <div role="tab" aria-selected="false" aria-controls="claimant"><div>Claimant</div></div>
    </div>
    <mat-tab-body id="details" role="tabpanel" class="mat-tab-body-active">
      <div class="mat-tab-body-content"><table><tr><th>Claimant</th><td>Test Person</td></tr></table></div>
    </mat-tab-body>
    <mat-tab-body id="claimant" role="tabpanel" style="display:none">
      <div class="mat-tab-body-content">Claimant details</div>
    </mat-tab-body>
    <script>
      document.addEventListener('click', event => {
        const tab = event.target.closest('[role="tab"]');
        if (!tab) return;
        document.querySelectorAll('[role="tab"]').forEach(t => t.setAttribute('aria-selected', String(t === tab)));
        document.querySelectorAll('mat-tab-body').forEach(panel => {
          const selected = panel.id === tab.getAttribute('aria-controls');
          panel.style.display = selected ? 'block' : 'none';
          panel.classList.toggle('mat-tab-body-active', selected);
        });
      });
    </script>
  `);
});

test('keeps the requested tab when XUI prepends tabs before the click', async ({ page }) => {
  await page.evaluate(() => {
    document.body.insertAdjacentHTML('beforeend', '<div data-testid="loading">Loading</div>');
  });
  // The first check waits for the tab to attach; the next precedes the click.
  let checks = 0;
  await page.addLocatorHandler(page.getByTestId('loading'), async () => {
    if (++checks === 1) return;
    await page.evaluate(() => {
      document.querySelector('[role="tablist"]')!.insertAdjacentHTML('afterbegin', `
        <div role="tab" aria-selected="false" aria-controls="tasks"><div>Tasks</div></div>
        <div role="tab" aria-selected="false" aria-controls="roles"><div>Roles and access</div></div>
      `);
      document.body.insertAdjacentHTML('beforeend', `
        <mat-tab-body id="tasks" role="tabpanel" style="display:none">
          <div class="mat-tab-body-content">Active tasks</div>
        </mat-tab-body>
        <mat-tab-body id="roles" role="tabpanel" style="display:none">
          <div class="mat-tab-body-content">Roles</div>
        </mat-tab-body>
      `);
      document.querySelector('[data-testid="loading"]')!.remove();
    });
  }, { noWaitAfter: true });

  await new CaseDetailsPage(page).assertTabData([{
    tabName: 'Case Details',
    tabContent: [{ tabItem: 'Claimant', value: 'Test Person', exact: true }],
  }]);

  await expect(page.getByRole('tab').first()).toHaveText('Tasks');
  await expect(page.getByRole('tab', { name: /^Case Details,/ })).toHaveAttribute('aria-selected', 'true');
});

test('checks exclusions within the requested panel rather than other tab labels', async ({ page }) => {
  await new CaseDetailsPage(page).assertTabData([{
    tabName: 'Case Details',
    tabContent: ['Claimant'],
    excludedContent: ['Case Details'],
  }, {
    tabName: 'Claimant',
    tabContent: ['Claimant details'],
    excludedContent: ['Test Person'],
  }]);
  await expect(page.getByRole('tab', { name: 'Claimant', exact: true })).toHaveAttribute('aria-selected', 'true');
});
