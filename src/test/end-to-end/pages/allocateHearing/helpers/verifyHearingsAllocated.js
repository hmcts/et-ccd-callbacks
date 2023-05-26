const { I } = inject();

function verifyHearingsAllocated() {

    I.click('//div[contains(text(),\'Hearings\')]');
    I.see('Hearing type');
    I.see('Hearing Venue');
    I.click('//img[@alt=\'image\']');
    I.see('Preliminary Hearing');
    I.see('Hull Combined Court Centre');
    I.see('Hearing Format');
    I.see('In person');
    I.see('Judicial Mediation');
    I.see('Yes');
    I.see('Public or Private?');
    I.see('Public');
    I.see('Hearing Number');
    I.see('1');
    I.see('Estimated hearing length');
    I.see('Days, Hours or Minutes');
    I.see('Sit Alone or Full Panel');

    // Resources
    I.see('Employment Judge');
    I.see('A Judge');
    I.see('Employee Member');
    I.see('EE Member');
    I.see('Day');
    I.see('Day');
    I.see('Day 1');
    I.see('Hearing Date');
    I.see('Hearing Status');
    I.see('Listed');
    I.see('Hearing Venue');
    I.see('Hull Combined Court Centre');
    I.see('Room');
    I.see('Clerk');
    I.see('A Clerk');
}
module.exports = { verifyHearingsAllocated };
