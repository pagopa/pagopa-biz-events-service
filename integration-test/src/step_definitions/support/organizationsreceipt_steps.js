const assert = require('assert')
const {Given, When, Then, setDefaultTimeout} = require('@cucumber/cucumber')
const {getOrganizationReceipt} = require("./bizeventservice_client");
const {createDocument, deleteDocument} = require("./cosmosdb_client");

let responseToCheck;
let receipt;

setDefaultTimeout(360 * 1000);


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

