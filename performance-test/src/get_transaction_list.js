import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

import { createDocument, deleteDocument } from "./modules/cosmosdb_client.js";
import { getTransactionList } from "./modules/bizeventservice_client.js";
import { makeidMix, getRandomItemFromArray } from './modules/helpers.js';

export let options = JSON.parse(open(__ENV.TEST_TYPE));

const varsArray = new SharedArray('vars', function() {
	return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const numberOfEventsToPreload = `${vars.numberOfEventsToPreload}`;
const cosmosDBURI = `${vars.cosmosDBURI}`;
const bizEventTrxURI = `${vars.bizEventTrxURI}`;
const databaseID = `${vars.databaseID}`;
const containerViewGeneralID = `${vars.containerViewGeneralID}`;
const containerViewUserID = `${vars.containerViewUserID}`;
const containerViewCartID = `${vars.containerViewCartID}`;
const userTaxCode = "XXXXQL69L16Q001X";
const size = "25"

const subKey = `${__ENV.API_SUBSCRIPTION_KEY}`;
const accountPrimaryKey = `${__ENV.BIZ_COSMOS_ACCOUNT_PRIMARY_KEY}`;

var containerIds = new Array();

export function setup() {
	// 1. setup code (once)
	// The setup code runs, setting up the test environment (optional) and generating data
	// used to reuse code for the same VU

	for (let i = 0; i < numberOfEventsToPreload; i++) {
		let id = makeidMix(35);
		let response = createDocument(cosmosDBURI, databaseID, containerViewGeneralID, accountPrimaryKey, id, id, JSON.stringify(getTestItemViewGeneral(id)));
		check(response, { "status is 201": (res) => (res.status === 201) });
		if (containerIds.indexOf(id) === -1) { 
			containerIds.push(id);
		} 
		response = createDocument(cosmosDBURI, databaseID, containerViewUserID, accountPrimaryKey, id, userTaxCode, JSON.stringify(getTestItemViewUser(id)));
		check(response, { "status is 201": (res) => (res.status === 201) });
		if (containerIds.indexOf(id) === -1) { 
			containerIds.push(id);
		} 
		response = createDocument(cosmosDBURI, databaseID, containerViewCartID, accountPrimaryKey, id, id, JSON.stringify(getTestItemViewCart(id)));
		check(response, { "status is 201": (res) => (res.status === 201) });
		if (containerIds.indexOf(id) === -1) { 
			containerIds.push(id);
		} 
	}

	// return the array with preloaded id
	return { ids: containerIds }
	 
	// precondition is moved to default fn because in this stage
	// __VU is always 0 and cannot be used to create env properly
}

// teardown the test data
export function teardown(data) {
	let response;
	for (const element of data.ids) {
		response = deleteDocument(cosmosDBURI, databaseID, containerViewGeneralID, accountPrimaryKey, element, element);
		check(response, { "status is 204": (res) => (res.status === 204) });
		response = deleteDocument(cosmosDBURI, databaseID, containerViewCartID, accountPrimaryKey, element, element);
		check(response, { "status is 204": (res) => (res.status === 204) });
		response = deleteDocument(cosmosDBURI, databaseID, containerViewUserID, accountPrimaryKey, element + "-p", userTaxCode);
	    check(response, { "status is 204": (res) => (res.status === 204) });
	}
}


export default function(data) {

	// Get a transactionList
	let tag = {
		bizEventMethod: "GetTransactionList",
	};
	

	const params = {
		headers: {
		    'Ocp-Apim-Subscription-Key': subKey,
		    'x-fiscal-code': userTaxCode,
			'Content-Type': 'application/json'
		},
	};

	const response = getTransactionList(bizEventTrxURI, size, params);

	console.log(`GetTransactionList... [status: ${response.status}, size: ${JSON.parse(response.body).transactions.length}]`);

	check(response, {"GetTransactionList status is 200": (res) => (res.status === 200)}, tag);
	check(response, {"GetTransactionList size is as expected": (res) => (JSON.parse(res.body).notices.length <= size)}, tag);

}



function getTestItemViewGeneral(id) {
	    return {
		    "id": id,
		    "transactionId": id,
		    "authCode": "250863",
		    "paymentMethod": "PO",
		    "rrn": "223560110624",
		    "pspName": "Worldline Merchant Services Italia S.p.A.",
		    "transactionDate": "2022-12-22T13:07:25Z",
		    "walletInfo": {
		        "accountHolder": "CICCIO HOLDER",
		        "brand": "MASTERCARD",
		        "blurredNumber": "0403"
		    },
		    "payer": {
		        "taxCode": userTaxCode
		    },
		    "isCart": false,
		    "fee": "0,40",
		    "origin": "UNKNOWN",
		    "totalNotice": 1
	    }
}

function getTestItemViewUser(id) {
		    return {
			    "id": id + "-p",
			    "taxCode": userTaxCode,
			    "transactionId": id,
			    "transactionDate": "2022-12-22T13:07:25Z",
			    "hidden": false,
			    "isPayer": true,
			    "isDebtor": true
		   }
}

function getTestItemViewCart (id) {
		return {
		    "id": id,
		    "transactionId": id,
		    "eventId": id,
		    "subject": "pagamento",
		    "amount": "854.07",
		    "payee": {
		        "name": "ACI Automobile Club Italia",
		        "taxCode": "00493410583"
		    },
		    "debtor": {
		        "name": "CICCIO PAYER",
		        "taxCode": "XXXXQL69L16Q001X"
		    },
		    "refNumberValue": "960000000094659945",
		    "refNumberType": "IUV"
	    }
}