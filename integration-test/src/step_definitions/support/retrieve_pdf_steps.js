const { Given, When, Then } = require('@cucumber/cucumber');
const assert = require('assert');
const axios = require('axios');
const fs = require('fs');
const path = require('path');
const { createReceipt } = require('./common');
const { createDocumentInReceiptDatastore, deleteDocumentFromReceiptDatastore } = require('./receipt_cosmosdb_client');
const { generatePDF } = require('./bizeventservice_client');
const { uploadPDFToBlobStorage, deleteBlob } = require('./blob_storage_client');

// ======================================================
// CONFIG
// ======================================================
let receipt;
let pdfName;
let response;
setDefaultTimeout(360 * 1000);

// After each Scenario
After(async function () {
  // remove receipt
  if (receipt) {
    await deleteDocumentFromReceiptDatastore(bizEvent.id);
  }
  // delete pdf
  if (pdfName) {
    await deleteBlob(pdfName);
  }

  receipt = null;
  pdfName = null;
  response = null;
});

// ======================================================
// GIVEN
// ======================================================

Given(
  'a PDF stored on the receipts\' blob storage with name {string}',
  async function (pdfName) {
    pdfName = pdfName;
    let res = await uploadPDFToBlobStorage(pdfName, pdfName);
    assert.strictEqual(res.status, 201);
  }
);

Given(
  'a receipt with eventId {string}, pdf name {string}, status {string} and errCode {string}',
  async function (eventId, pdfName, status, errCode) {
    receipt = createReceipt(eventId, pdfName, status, errCode);

    let res = await createDocumentInReceiptDatastore(receipt);
    assert.strictEqual(res.statusCode, 201);
  }
);

// ======================================================
// WHEN
// ======================================================

When(
  'the user with fiscal code {string} asks for the PDF with thirdPartyId {string}',
  async function (fiscalCode, thirdPartyId) {
    response = await generatePDF(thirdPartyId, fiscalCode);
  }
);

// ======================================================
// THEN
// ======================================================

Then(
  'the user gets the status code {int}',
  function (statusCode) {
    assert.strictEqual(response.status, statusCode);
  }
);

Then(
  'the user gets the custom code {string}',
  function (customCode) {
    const body = response.data;

    // TODO
    assert.ok(body, 'Response body is empty');
    assert.strictEqual(body.code, customCode);
  }
);

Then(
  'the PDF content is valid',
  function () {
    const contentType = context.response.headers['content-type'];

    assert.ok(
      contentType === 'application/pdf',
      `Expected application/pdf but got ${contentType}`
    );

    const buffer = Buffer.from(response.data);
    const pdfHeader = buffer.slice(0, 4).toString();

    assert.strictEqual(pdfHeader, '%PDF');
  }
);
