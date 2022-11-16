const {Given, When, Then} = require('@cucumber/cucumber')
const {healthCheckInfo} = require("./bizeventservice_client");
const assert = require("assert");


Given('Biz-Events Service running', async function () {
    const response = await healthCheckInfo();
    assert.strictEqual(response.status, 200);
});
