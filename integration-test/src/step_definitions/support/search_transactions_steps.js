const { Given, When, Then, setDefaultTimeout, After } = require('@cucumber/cucumber');
const assert = require('assert')
const { searchTransactions } = require('./bizeventservice_client');


setDefaultTimeout(360 * 1000);

Given('i use a valid token', function () {
    this.token = process.env.SEARCH_TRANSACTION_TOKEN;
    assert.ok(this.token, 'Missing environment variable: SEARCH_TRANSACTION_TOKEN');
});

Given('i use an invalid token', function () {
    this.token = process.env.SEARCH_TRANSACTION_TOKEN_INVALID || 'invalid-token';
});

Given('i send a valid x-fiscal-code header', function () {
    this.xFiscalCode = process.env.SEARCH_TRANSACTION_X_FISCAL_CODE;
    assert.ok(this.xFiscalCode, 'Missing environment variable: SEARCH_TRANSACTION_X_FISCAL_CODE');
});

Given('i do not send a valid x-fiscal-code header', function () {
    this.xFiscalCode = undefined;
});

Given('i use a valid notice number', function () {
    this.cfOrg = process.env.SEARCH_TRANSACTION_CF_ORG;
    this.noticeNumber = process.env.SEARCH_TRANSACTION_NAV;
    assert.ok(this.noticeNumber, 'Missing environment variable: SEARCH_TRANSACTION_NAV');
    assert.ok(this.cfOrg, 'Missing environment variable: SEARCH_TRANSACTION_CF_ORG');
});

Given('i use an invalid notice number', function () {
    this.cfOrg = process.env.SEARCH_TRANSACTION_CF_ORG;
    this.noticeNumber = process.env.SEARCH_TRANSACTION_NAV_INVALID || '39900000000000000';
    assert.ok(this.cfOrg, 'Missing environment variable: SEARCH_TRANSACTION_CF_ORG');
});

When('i perform a transaction search', async function () {
    this.response = await searchTransactions(
        this.cfOrg,
        this.noticeNumber,
        this.xFiscalCode,
        this.token
    );
    console.log("Response: ", this.response.data)

    assert.ok(this.response, 'No response returned');
});

Then('the response status code is {int}', function (int) {
    assert.strictEqual(this.response.status, int);
});

Then('the not found error body is correct', function () {
    const body = this.response.data;

    assert.strictEqual(body.title, 'Biz-events-view-cart not found');
    assert.ok(body.detail);
    assert.strictEqual(body.code, 'VC_404_002');
});

Then('the unauthorized error body is correct', function () {
    const body = this.response.data;

    assert.strictEqual(body.title, 'Unauthorized');
    assert.strictEqual(body.detail, 'Invalid Token');
});

Then('the body contain the expected transaction data', function () {
    const body = this.response.data;

    assert.ok(body.subject);
    assert.notStrictEqual(body.amount, undefined);
    assert.ok(body.payee);
    assert.ok(body.payee.name);
    assert.ok(body.payee.taxCode);
    assert.ok(body.debtor);
    assert.ok(body.debtor.name);
    assert.ok(body.debtor.taxCode);
    assert.ok(body.refNumberValue);
    assert.ok(body.refNumberType);
});
