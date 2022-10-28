const assert = require('assert')
const {Given, When, Then} = require('@cucumber/cucumber')
const {getOrganizationReceipt} = require("./bizeventservice_client");
const {createDocument} = require("./cosmosdb_client");

let responseToCheck;
let receipt;


// Given
Given('Biz-Event to test with id {string} and save it on Cosmos DB', async function (id) {
	responseToCheck = await createDocument(id);
	console.log(responseToCheck.data)
});


// When
When('the organization asks for a receipt with fiscal code PA {string} and iur {string} and iuv {string}', async function (organizationFiscalCode, iur, iuv) {
    responseToCheck = getOrganizationReceipt(organizationFiscalCode, iur, iuv);
    // save data
    receipt = responseToCheck.data;
});



// Then
Then('the organization gets the status code {int}', function (status) {
    assert.strictEqual(responseToCheck.status, status);
});

Then('the details of the receipt are returned to the organization', async function (receiptId) {
    assert.strictEqual(receipt.receiptId, receiptId);
});

