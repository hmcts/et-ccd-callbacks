#!/bin/bash

#Checkout xui tests for ET project
git clone git@github.com:hmcts/et-xui-e2e-tests.git
cd et-xui-e2e-tests

echo "Switch to Master branch on et-xui-e2e-tests repo"
git checkout master

echo "Increase login redirect wait for preview smoke tests"
perl -0pi -e 's/waitForURL\(new RegExp\(baseUrl\), \{ timeout: 10000 \}\)/waitForURL(new RegExp(baseUrl), { timeout: 30000 })/' playwrighte2e/pages/loginPage.ts
