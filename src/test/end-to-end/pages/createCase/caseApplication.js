'use strict';

const commonConfig = require('../../data/commonConfig.json');
const caseApplicationConfig = require('./caseApplicationConfig.json');


module.exports =  async function () {

    const I = this;

    //General Details Tab
    I.see('has been created');
    I.see('Next step');
    I.see('Print');

    //Case Details Tab
    I.click(caseApplicationConfig.case_tab_details);
    I.wait(commonConfig.time_interval_4_seconds);

    I.see('Claimant');
    I.see('Joe Bloggs');
    I.see('Respondent');
    I.see('Respondent Name');
    I.see('Case Status: Submitted');
    I.see('Tribunal Office');
    I.see('London Central');
    I.see('Current Position');
    I.see('Manually created');
    I.see('Single or Multiple');
    I.see('Single');
    I.see('Submission Reference');
    I.see('Date of Receipt');
    I.see('Target Hearing Date');

    //Claimant Tab.
    I.click(caseApplicationConfig.claimant_tab_details);
    I.wait(commonConfig.time_interval_4_seconds);

    //Claimant Personal Details Section
    I.see('Claimant Details');
    I.see('First Name');
    I.see('Joe');
    I.see('Last Name');
    I.see('Bloggs');
    I.see('Date of birth');
    I.see('Sex');
    I.see('Male');
    I.see('Gender Identity description');

    //Address Section
    I.see('Test Gender');
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
    I.see('07928621415');
    I.see('Alternative number');
    I.see('07928621415');
    I.see('Email address');
    I.see('xxxx@test.com');
    I.see('Contact preference');
    I.see('Email');

    //Other details Section
    I.see('Other details');
    I.see('Employment Details');
    I.see('Occupation');
    I.see('Test - Occupation');
    I.see('Employed from');
    I.see('Is the employment continuing?');
    I.see('Notice Period End Date');
    I.see('Are there any disabilities or special requirements?');
    I.see('No');

    //Claimant Work Address Section
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

    //Claimant Hearing Preferences Section
    I.see('What are the claimant\'s hearing preferences');
    I.see('Neither');
    I.see('Why is the claimant unable to take part in video or phone hearings');
    I.see('Has a condition');

    //Respondent Details Tab
    I.click(caseApplicationConfig.respondent_tab_details);
    I.wait(commonConfig.time_interval_4_seconds);
    I.see('Respondents');
    I.see('Name of respondent');
    I.see('Has the ET3 form been received?');
    I.see('Respondent Name');
    I.see('No');

    //Referrals Details Tab
    I.click(caseApplicationConfig.referrals_tab_details);
    I.wait(commonConfig.time_interval_4_seconds);
    I.see('Referrals');
    I.see('Send a new referral');
    I.see('Reply to a referral');
    I.see('Close a referral');

    //History Details Tabs
    I.click(caseApplicationConfig.history_tab_details);
    I.wait(commonConfig.time_interval_4_seconds);
    I.see('Details');
    I.see('Date');
    I.see('Author');
    I.see('End state');
    I.see('Event');
    I.see('Summary');
    I.see('Comment');
}
