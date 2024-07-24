import http from 'k6/http';
import {check} from 'k6';
import {SharedArray} from 'k6/data';
import {createDocument,deleteDocument} from "./modules/cosmosdb_client.js";
import {putBlob,deleteBlob} from "./modules/blobcontainer_client.js";
import {getPDFReceipt} from "./modules/bizeventservice_client.js";
import {makeidMix,getRandomItemFromArray} from './modules/helpers.js';

const varsArray = new SharedArray('vars', function() {
    return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const numberOfEventsToPreload = `${vars.numberOfEventsToPreload}`;
const cosmosDBURI = `${vars.cosmosDBURI}`;
const databaseID = `${vars.databaseID}`;
const containerID = `${vars.containerID}`;
const receiptCosmosDBURI = `${vars.receiptCosmosDBURI}`;
const receiptDatabaseID = `${vars.receiptDatabaseID}`;
const receiptContainerID = `${vars.receiptContainerID}`;
const blobStorageConnString = `${vars.blobStorageConnectionString}`;
const blobStorageContainerID = `${vars.blobStorageContainerID}`;
const bizEventTrxURI = `${vars.bizEventTrxURI}`;
const tokenizerURL = `${vars.tokenizerURL}`;
const receiptTestIdPrefix = `${vars.receiptTestIdPrefix}`;
const userTaxCode = "YYYYQL69L16Q001Y";

const subKey = `${__ENV.API_SUBSCRIPTION_KEY}`;
const tokenizerSubKey = `${__ENV.TOKENIZER_API_SUBSCRIPTION_KEY}`;
const receiptCosmosAccountPrimaryKey = `${__ENV.RECEIPT_COSMOS_ACCOUNT_PRIMARY_KEY}`;
const bizCosmosAccountPrimaryKey = `${__ENV.BIZ_COSMOS_ACCOUNT_PRIMARY_KEY}`;
const storageAccountPrimaryKey = `${__ENV.STORAGE_ACCOUNT_PRIMARY_KEY}`;

var containerIds = new Array();

const blobStream = open('/resources/testPDF.pdf', 'b');

export function setup() {

    // 1. setup code (once)
    // The setup code runs, setting up the test environment (optional) and generating data
    // used to reuse code for the same VU

    for (let i = 0; i < numberOfEventsToPreload; i++) {
        let id = makeidMix(35);
        let receiptId = receiptTestIdPrefix + "-" + id;
        let pdfName = id + "-testPDF.pdf";
        let pdfURL = id + "-testPDF.pdf";

        // create biz-event
        let response = createDocument(cosmosDBURI, databaseID, containerID, bizCosmosAccountPrimaryKey,
            receiptId, receiptId, JSON.stringify(createBizEvent(receiptId)));
        check(response, {
            "status is 201": (res) => (res.status === 201)
        });
        // create receipt
        response = createDocument(receiptCosmosDBURI, receiptDatabaseID, receiptContainerID, receiptCosmosAccountPrimaryKey,
            receiptId, receiptId, JSON.stringify(createReceipt(receiptId, pdfName, pdfURL)));
        check(response, {
            "status is 201": (res) => (res.status === 201)
        });
        // put the PDF file in storage
        response = putBlob(storageAccountPrimaryKey, 'pagopadweureceiptsfnsa', 'pagopa-d-weu-receipts-azure-blob-receipt-st-attach', pdfName, blobStream);
        check(response, {
            "status is 201": (res) => (res.status === 201)
        });


        if (containerIds.indexOf(id) === -1) {
            containerIds.push(id);
        }
    }

    // return the array with preloaded id
    return {
        ids: containerIds
    }

    // precondition is moved to default fn because in this stage
    // __VU is always 0 and cannot be used to create env properly
}

// teardown the test data
export function teardown(data) {
    for (const element of data.ids) {
        let receiptId = receiptTestIdPrefix + "-" + element;
        let pdfName = element + "-testPDF.pdf";

        // delete from biz-event
        let response = deleteDocument(cosmosDBURI, databaseID, containerID, bizCosmosAccountPrimaryKey, receiptId, receiptId);
        check(response, {
            "status is 204": (res) => (res.status === 204)
        });
        // delete from receipt
        response = deleteDocument(receiptCosmosDBURI, receiptDatabaseID, receiptContainerID, receiptCosmosAccountPrimaryKey, receiptId, receiptId);
        check(response, {
            "status is 204": (res) => (res.status === 204)
        });
        // delete from storage
        response = deleteBlob(storageAccountPrimaryKey, 'pagopadweureceiptsfnsa', 'pagopa-d-weu-receipts-azure-blob-receipt-st-attach', pdfName);
        check(response, {
            "status is 202": (res) => (res.status === 202)
        });
    }
}


export default function(data) {

    let idToRecover = receiptTestIdPrefix + "-" + getRandomItemFromArray(data.ids);

    let tag = {
        bizEventMethod: "GetPDFReceipt",
    };

    const params = {
        headers: {
            'Ocp-Apim-Subscription-Key': subKey,
            'x-fiscal-code': userTaxCode,
            'Content-Type': 'application/json'
        },
    };

    let response = getPDFReceipt(bizEventTrxURI, idToRecover, params);

    console.log(`GetPDFReceipt... [status: ${response.status}, event-id: ${idToRecover}]`);

    check(response, {
        "GetPDFReceipt status is 200": (res) => (res.status === 200)
    }, tag);

}

function createReceipt(id, pdfName, pdfUrl) {
    let tokenResponse = createToken(userTaxCode);
    let responseBody = JSON.parse(tokenResponse.body);
    let receipt = {
        "id": id,
        "eventId": id,
        "eventData": {
            "payerFiscalCode": responseBody.token,
            "debtorFiscalCode": responseBody.token
        },
        "mdAttach": {
            name: pdfName,
            url: pdfUrl
        },
        "status": "IO_NOTIFIED",
        "numRetry": 0
    }
    return receipt
}

function createToken(fiscalCode) {
    let headers = {
        "Content-Type": "application/json",
        "x-api-key": tokenizerSubKey
    };

    return http.put(tokenizerURL, JSON.stringify({
        "pii": fiscalCode
    }), {
        headers
    });
}

function createBizEvent(id) {
    return {
        "id": id,
        "version": "2",
        "complete": "true",
        "missingInfo": [],
        "debtorPosition": {
            "modelType": "1",
            "iuv": "960000000094659945"
        },
        "creditor": {
            "idPA": "00493410583",
            "idBrokerPA": "00493410583",
            "idStation": "00493410583_02",
            "companyName": "ACI Automobile Club Italia",
            "officeName": "ACI OfficeName"
        },
        "psp": {
            "idPsp": "BPPNIT2PXXX",
            "idBrokerPsp": "03339200374",
            "idChannel": "03339200374_01",
            "psp": "Worldline Merchant Services Italia S.p.A."
        },
        "debtor": {
            "fullName": "ERNESTO DEBTOR",
            "entityUniqueIdentifierType": "F",
            "entityUniqueIdentifierValue": userTaxCode
        },
        "paymentInfo": {
            "paymentDateTime": "2024-07-22T09:01:48Z",
            "paymentToken": "223F665500001336354",
            "amount": "344.24",
            "fee": "0.0",
            "paymentMethod": "PO",
            "remittanceInformation": "pagamento"
        },
        "transferList": [{
            "fiscalCodePA": "00493410583",
            "companyName": "ACI Automobile Club Italia",
            "amount": "344.24",
            "transferCategory": "9/0301105TS/3/CB617RP",
            "remittanceInformation": "/RFB/9600000000/TXT/CB617RP-Mag2022/Apr2023--EC Lorem-E. 261,92 (san 4,91 int 0,95)"
        }],
        "transactionDetails": {
            "origin": "PaymentManager",
            "user": {
                "fiscalCode": userTaxCode,
                "userId": "677676786",
                "userStatus": "11",
                "userStatusDescription": "REGISTERED_SPID",
                "name": "ERNESTO",
                "surname": "PAYER"
            },
            "transaction": {
                "idTransaction": "134528954",
                "transactionId": "134528954",
                "grandTotal": 57616,
                "amount": 57598,
                "fee": 18,
                "transactionStatus": "Confermato",
                "accountingStatus": "Contabilizzato",
                "rrn": "223560110624",
                "authorizationCode": "00",
                "creationDate": "2024-07-22T09:01:48Z",
                "numAut": "250863",
                "accountCode": "0037r972892475982475842",
                "psp": {
                    "idChannel": "05963231005_01_ONUS",
                    "businessName": "Worldline Merchant Services Italia S.p.A.",
                    "serviceName": "Pagamento con Carte"
                },
                "origin": "IO"
            },
            "wallet": {
                "idWallet": "125714007",
                "enableableFunctions": [
                    "pagoPA",
                    "BPD",
                    "FA"
                ],
                "pagoPa": true,
                "onboardingChannel": "IO",
                "favourite": false,
                "createDate": "2024-07-22T09:01:48Z",
                "info": {
                    "type": "CardInfo",
                    "blurredNumber": "0403",
                    "holder": "ERNESTO HOLDER",
                    "expireMonth": "06",
                    "expireYear": "2026",
                    "brand": "MASTERCARD",
                    "hashPan": "e88aadfd9f40e1482615fd3c8c44f05c53f93aed1bcea69e82b3e5e27668f822"
                }
            },
            "info": {
                "brand": "MASTERCARD",
                "brandLogo": "https://checkout.pagopa.it/assets/creditcard/mastercard.png",
                "clientId": "CHECKOUT_FAKE",
                "paymentMethodName": "CARDS",
                "type": "CP"
            }
        },
        "timestamp": 1721638908802,
        "properties": {
            "Postman-Token": "43532e1c-8cda-4380-bc63-ce8b0a1bcf3b"
        },
        "eventStatus": "DONE",
        "eventRetryEnrichmentCount": 0,
        "eventTriggeredBySchedule": false
    }
}