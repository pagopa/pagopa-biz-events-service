const assert = require('assert')
const { Given, When, Then, setDefaultTimeout, After } = require('@cucumber/cucumber')
const { getOrganizationReceipt, getBizEventById, getBizEventByOrgFiscalCodeAndIuv, getTransactionListForUserWithFiscalCode, getTransactionWithIdForUserWithFiscalCode, disableTransactionWithIdForUserWithFiscalCode } = require("./bizeventservice_client");
const { createDocument, deleteDocument } = require("./cosmosdb_client");
const { createEvent, makeId, createViewUser, createViewGeneral, createViewCart } = require("./common");
const { createDocumentInBizEventsDatastore, deleteDocumentFromBizEventsDatastore, deleteDocumentFromViewUserDatastore, deleteDocumentFromViewGeneralDatastore, deleteDocumentFromViewCartDatastore, createDocumentInViewUserDatastore, createDocumentInViewGeneralDatastore, createDocumentInViewCartDatastore } = require("./biz_events_cosmosdb_client");

const BIZ_ID = "biz-event-service-int-test-transaction-";

let responseToCheck;
let receipt;
let bizEvent;
let bizEventList = [];
let viewUserList = [];
let viewGeneralList = [];
let viewCartList = [];

setDefaultTimeout(360 * 1000);

// After each Scenario
After(async function () {
	// remove event
	if (bizEventList.length > 0) {
		for (let bizEvent of bizEventList) {
			await deleteDocumentFromBizEventsDatastore(bizEvent.id);
		}
	}
	if (viewUserList.length > 0) {
		for (let viewUser of viewUserList) {
			await deleteDocumentFromViewUserDatastore(viewUser.id,viewUser.taxCode);
		}
	}
	if (viewGeneralList.length > 0) {
		for (let viewGeneral of viewGeneralList) {
			await deleteDocumentFromViewGeneralDatastore(viewGeneral.transactionId);
		}
	}
	if (viewCartList.length > 0) {
		for (let viewCart of viewCartList) {
			await deleteDocumentFromViewCartDatastore(viewCart.id, viewCart.transactionId);
		}
	}

	responseToCheck = null;
	receipt = null;
	bizEvent = null;
	bizEventList = [];
	viewUserList = [];
    viewGeneralList = [];
    viewCartList = [];
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

Given('Save all on Cosmos DB', async () => {
	for (let bizEvent of bizEventList) {
		let response = await createDocumentInBizEventsDatastore(bizEvent);
		assert.strictEqual(response.statusCode, 201);
		response = null;
	}
})

//TRANSACTIONS
Given('{int} view user with taxCode {string} and transactionId prefix {string} and isPayer {string} on cosmos', function (numberOfView, taxCode, transactionId, isPayer) {
	for(let i = 0; i < numberOfView; i++){
		let viewUser = createViewUser(taxCode, transactionId+i, false, isPayer === "true");
		viewUserList.push(viewUser);
	}
});
Given('{int} view general with payer tax code {string} and transactionId prefix {string} on cosmos', function (numberOfView, payerTaxCode, transactionId) {
	for(let i = 0; i < numberOfView; i++){
		let viewGeneral = createViewGeneral(transactionId+i, payerTaxCode);
		viewGeneralList.push(viewGeneral);
	}
});
Given('{int} view cart for every view general with debtor taxCode {string} on cosmos', function (numberOfView, debtorTaxCode) {
	for(let viewGeneral of viewGeneralList){
		for(let i = 0; i < numberOfView; i++){
			let viewCart = createViewCart(i, viewGeneral.transactionId, debtorTaxCode);
			viewCartList.push(viewCart);
		}
	}
});
Given('Save all views on CosmosDB', async () => {
	//CLEAN DIRTY CASES
	if (viewUserList.length > 0) {
		for (let viewUser of viewUserList) {
			await deleteDocumentFromViewUserDatastore(viewUser.id,viewUser.taxCode);
		}
	}
	if (viewGeneralList.length > 0) {
		for (let viewGeneral of viewGeneralList) {
			await deleteDocumentFromViewGeneralDatastore(viewGeneral.transactionId);
		}
	}
	if (viewCartList.length > 0) {
		for (let viewCart of viewCartList) {
			await deleteDocumentFromViewCartDatastore(viewCart.id, viewCart.transactionId);
		}
	}

	for (let viewUser of viewUserList) {
		let response = await createDocumentInViewUserDatastore(viewUser);
		assert.strictEqual(response.statusCode, 201);
		response = null;
	}
	for (let viewGeneral of viewGeneralList) {
		let response = await createDocumentInViewGeneralDatastore(viewGeneral);
		assert.strictEqual(response.statusCode, 201);
		response = null;
	}
	for (let viewCart of viewCartList) {
		let response = await createDocumentInViewCartDatastore(viewCart);
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

Then('the transactions with cart items {string} for taxCode {string} have the correct amount and subject', (isCart, taxCode) => {
	for (let transaction of responseToCheck.data) {
		let totalAmount = 0;
		for(let viewCart of viewCartList.filter(el => el.transactionId == transaction.transactionId && (transaction?.payer?.taxCode === taxCode || el?.debtor?.taxCode === taxCode))){
			totalAmount += viewCart.amount;
			if(isCart == "true"){
				assert.notStrictEqual(transaction.payeeName, viewCart.payee.name);
			} else {
				assert.strictEqual(transaction.payeeName, viewCart.payee.name);
			}
		}
		assert.strictEqual(transaction.amount, `${totalAmount},00`);
		
	}
})

Given('Biz-Event with debtor fiscal code {string} and id {string}', (debtorFiscalCode, id) => {
	bizEventList.push(createEvent(id, id, undefined, debtorFiscalCode))
})

When('the user with fiscal code {string} asks the transaction with id {string}', async (fiscalCode, id) => {
	responseToCheck = await getTransactionWithIdForUserWithFiscalCode(id, fiscalCode);
})

When('the user with taxCode {string} disables the transaction with id {string}', async function (taxCode, transactionId) {
	responseToCheck = await disableTransactionWithIdForUserWithFiscalCode(transactionId, taxCode);
	assert.strictEqual(responseToCheck.status, 200);
})

Then('the user with tax code {string} gets the transaction detail with id {string} and it has the correct amount', (taxCode, id) => {
	let infoTransaction = responseToCheck.data.infoTransaction;
	assert.strictEqual(infoTransaction.transactionId, id);

	let totalAmount = 0;
	for(let viewCart of viewCartList.filter(el => el.transactionId == infoTransaction.transactionId && (infoTransaction?.payer?.taxCode === taxCode || el?.debtor?.taxCode === taxCode))){
		totalAmount += viewCart.amount;
	}
	assert.strictEqual(infoTransaction.amount, `${totalAmount},00`);
})
