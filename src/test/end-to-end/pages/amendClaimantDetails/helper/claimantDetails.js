const testConfig = require('../../../config');
const { I } = inject();

function verifyClaimantDetails() {

    I.waitForText('Claimant Details', testConfig.TestTimeToWaitForText);
    I.see('Title');
    I.see('Other');
    I.see('Other title');
    I.see('Commander');
    I.see('First Name');
    I.see('Harbour');
    I.see('Last Name');
    I.see('Vikrant');
    I.see('Date of birth');
    I.see('27 Feb 1986');
    I.see('Gender');
    I.see('Male');
    I.see('Sex');
    I.see('Female');
    I.see('Is the claimant\'s identity and sex registered at birth the same?');
    I.see('Yes');
    I.see('Gender Identity description');
    I.see('Transitional');
    I.see('Address');
    I.see('Building and Street');
    I.see('ROyaL Mail, Southend-on-sea M l o');
    I.see('Address Line 2');
    I.see('Short Street');
    I.see('Town or City');
    I.see('Southend-on-sea');
    I.see('Postcode/Zipcode');
    I.see('SS1 1AA');
    I.see('Country');
    I.see('United Kingdom');
    I.see('Phone number');
    I.see('01234567890');
    I.see('Phone number');
    I.see('01234567890');
    I.see('Email address');
    I.see('test.xxx@hcts.net');
    I.see('Contact preference');
    I.see('Email');
    I.see('Other details');
    I.see('Employment Details');
    I.see('Occupation');
    I.see('Claimant occupation');
    I.see('Employed from');
    I.see('20 Sep 2017');
    I.see('Is the employment continuing?');
    I.see('Yes');
    I.see('Notice Period End Date');
    I.see('30 Sep 2017');
    I.see('Are there any disabilities or special requirements?');
    I.see('Please provide details');
    I.see('Notice Period');
    I.see('Notice Weeks or Months');
    I.see('Notice Period Duration');
    I.see('1');
    I.see('Average weekly hours');
    I.see('40');
    I.see('Pay before tax');
    I.see('40000');
    I.see('Pay after tax');
    I.see('35000');
    I.see('Weekly, monthly or annual pay');
    I.see('Months');
    I.see('Pension Contribution');
    I.see('Not Sure');
    I.see('10000');
    I.see('Employee Benefits');
    I.see('Employee Benefits Details');
    I.see('Moble Phone and Health Insurances');
    I.see('Claimant Work Address');
    I.see('Building and Street');
    I.see('ROyaL Mail, Southend-on-sea M l o');
    I.see('Address Line 2');
    I.see('Short Street');
    I.see('Town or City');
    I.see('Southend-on-sea');
    I.see('Postcode/Zipcode');
    I.see('SS1 1AA');
    I.see('Country');
    I.see('United Kingdom');
    I.see('Work phone number');
    I.see('01234567870');
    I.see('Additional Claimant Information');
    I.see('What are the claimant\'s hearing preferences');
    I.see('Video');
    I.see('Phone');
    I.see('Tell us what support you need to request');
    I.see('Wheelchair please');
    I.see('Contact language');
    I.see('English');
    I.see('If a hearing is required, what language do you want to speak at a hearing?');
    I.see('Welsh');
}
module.exports = { verifyClaimantDetails };
