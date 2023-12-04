const assert = require('assert')
const {Given, When, Then, setDefaultTimeout, After} = require('@cucumber/cucumber')
const {getOrganizationReceipt, getBizEventById, getBizEventByOrgFiscalCodeAndIuv} = require("./bizeventservice_client");
const {createDocument, deleteDocument} = require("./cosmosdb_client");

let responseToCheck;
let receipt;
let bizEvent;

setDefaultTimeout(360 * 1000);

// After each Scenario
After(async function () {
    // remove event
    responseToCheck = null;
	receipt = null;
	bizEvent = null;
});


// Given
Given('Biz-Event to test with id {string} and save it on Cosmos DB', async function (id) {
	//precondition - delete any typos
	responseToCheck = await deleteDocument(id);
	assert.ok(responseToCheck.status == 204 || responseToCheck.status == 404);
	
	responseToCheck = await createDocument(id);
	assert.strictEqual(responseToCheck.status, 201);
});


// When
When('the organization asks for a receipt with fiscal code PA {string} and iur {string} and iuv {string}', async function (organizationFiscalCode, iur, iuv) {
    responseToCheck = await getOrganizationReceipt(organizationFiscalCode, iur, iuv);
    // save data
    receipt = responseToCheck.data;
});

When('the operator asks for a Biz-Event with fiscal code PA {string} and iuv {string}', async function (organizationFiscalCode, iuv) {
    responseToCheck = await getBizEventByOrgFiscalCodeAndIuv(organizationFiscalCode, iuv);
    // save data
    bizEvent = responseToCheck.data;
});

When('the operator asks for a Biz-Event with id {string}', async function (id) {
    responseToCheck = await getBizEventById(id);
    // save data
    bizEvent = responseToCheck.data;
});


// Then
Then('the organization gets the status code {int}', async function (status) {
    assert.strictEqual(responseToCheck.status, status);
});

Then('the details of the receipt are returned to the organization with receiptId {string}', async function (receiptId) {
	assert.strictEqual(receipt.receiptId, receiptId);
});

Then('the Biz-Event to test with id {string} is removed', async function (id) {
	//post condition - delete test data
	responseToCheck = await deleteDocument(id);
	assert.strictEqual(responseToCheck.status, 204);
});

Then('the operator gets the status code {int}', async function (status) {
	assert.strictEqual(responseToCheck.status, status);
});

Then('the details of the Biz-Event are returned to the operator with id {string}', async function (id) {
	assert.strictEqual(bizEvent.id, id);
});
