const assert = require('assert')
const {Given, When, Then, setDefaultTimeout, After} = require('@cucumber/cucumber')
const {getOrganizationReceipt, getBizEventById, getBizEventByOrgFiscalCodeAndIuv, getTransactionListForUserWithFiscalCode, getTransactionWithIdForUserWithFiscalCode} = require("./bizeventservice_client");
const {createDocument, deleteDocument} = require("./cosmosdb_client");
const {createEvent, makeId} = require("./common");
const {createDocumentInBizEventsDatastore, deleteDocumentFromBizEventsDatastore} = require("./biz_events_cosmosdb_client");

const BIZ_ID = "biz-event-service-int-test-transaction-";

let responseToCheck;
let receipt;
let bizEvent;
let bizEventList = [];

setDefaultTimeout(360 * 1000);

// After each Scenario
After(async function () {
    // remove event
	if(bizEventList.length > 0){
        for(let bizEvent of bizEventList){
            await deleteDocumentFromBizEventsDatastore(bizEvent.id);
        }
    }

    responseToCheck = null;
	receipt = null;
	bizEvent = null;
	bizEventList = [];
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


Given('{int} Biz-Event with debtor fiscal code {string}', (numberOfEvents, debtorFiscalCode) => {
	for (let i = 0; i < numberOfEvents; i++) {
        bizEventList.push(createEvent(BIZ_ID + i + makeId(4), undefined, undefined, debtorFiscalCode))
    }
})

Given('{int} Biz-Event with payer fiscal code {string}', (numberOfEvents, payerFiscalCode) => {
	for (let i = 0; i < numberOfEvents; i++) {
        bizEventList.push(createEvent(BIZ_ID + i + makeId(4), undefined, undefined, undefined, payerFiscalCode))
    }
})

Given('Save all on Cosmos DB', async () => {
	for (let bizEvent of bizEventList) {
		let response = await createDocumentInBizEventsDatastore(bizEvent);
		assert.strictEqual(response.statusCode, 201);
		response = null;
	}
})

When('the user with fiscal code {string} asks for its transactions', async (fiscalCode) => {
	responseToCheck = await getTransactionListForUserWithFiscalCode(fiscalCode);
})

Then('the user gets the status code {int}', (status) => {
	assert.strictEqual(responseToCheck.status, status);
})
	
Then('the user gets {int} transactions', (totalTransactions) => {
	assert.strictEqual(responseToCheck.data.length, totalTransactions);
})

Given('{int} cart Biz-Event with transactionId {string}, debtor fiscal code {string} and amount {int}', (numberOfEvents, transactionId, debtorFiscalCode, amount) => {
	for (let i = 0; i < numberOfEvents; i++) {
        bizEventList.push(createEvent(i + makeId(10), transactionId, numberOfEvents, debtorFiscalCode, undefined, amount))
    }
})

Then('one of the transactions is a cart with id {string} and amount {int}', (transactionId, amount) => {
	let found = false;
	for (let transaction of responseToCheck.data) {
		if (transaction.transactionId == transactionId) {
			assert.strictEqual(transaction.amount, amount);
            found = true;
        }
	}
	assert.strictEqual(found, true);
})

Given('Biz-Event with debtor fiscal code {string} and id {string}', (debtorFiscalCode, id) => {
	bizEventList.push(createEvent(id, undefined, undefined, debtorFiscalCode))
})

When('the user with fiscal code {string} asks the transaction with id {string} and isCart {string}', async (fiscalCode, id, cart) => {
	let isCart = (cart == "true");
	responseToCheck = await getTransactionWithIdForUserWithFiscalCode(id, fiscalCode, isCart);
})

Then('the user gets the transaction with id {string}', (id) => {
	assert.strictEqual(responseToCheck.data.id, id);
})
