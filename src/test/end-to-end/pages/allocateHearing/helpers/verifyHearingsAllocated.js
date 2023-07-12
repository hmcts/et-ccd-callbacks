const { I } = inject();

function verifyHearingsAllocated() {

    I.click('//div[contains(text(),\'Hearings\')]');
    I.see('Hearing type');
    I.see('Hearing Venue');
    I.click('//img[@alt="image"]');
    I.see('Leeds');
    I.see('Judicial Mediation');
    I.see('Yes');
    I.see('Hearing Number');
    I.see('Estimated hearing length');
    I.see('Days, Hours or Minutes');
    I.see('Sit Alone or Full Panel');

    // Resources
    I.see('Employment Judge');
    I.see('A Judge');
    I.see('Day');
    I.see('Day 1');
    I.see('Hearing Date');
    I.see('Hearing Status');
    I.see('Listed');
    I.see('Hearing Venue');
    I.see('Leeds');
    I.see('Clerk');
    I.see('A Clerk');
}
module.exports = { verifyHearingsAllocated };
