import http from 'k6/http';
import { check, group } from 'k6';
import { SharedArray } from 'k6/data';

import { createDocument, deleteDocument } from "./modules/cosmosdb_client.js";
import { getBlob, putBlob } from "./modules/blobcontainer_client.js";
import { disableTransaction, getTransactionList } from "./modules/bizeventservice_client.js";
import { makeidMix, getRandomItemFromArray } from './modules/helpers.js';

const varsArray = new SharedArray('vars', function() {
	return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const numberOfEventsToPreload = `${vars.numberOfEventsToPreload}`;
const cosmosDBURI = `${vars.cosmosDBURI}`;
const blobStorageConnString = `${vars.blobStorageConnectionString}`;
const blobStorageContainerID = `${vars.blobStorageContainerID}`;
const bizEventTrxURI = `${vars.bizEventTrxURI}`;
const databaseID = `${vars.databaseID}`;
const containerViewGeneralID = `${vars.containerViewGeneralID}`;
const containerViewUserID = `${vars.containerViewUserID}`;
const containerViewCartID = `${vars.containerViewCartID}`;
const userTaxCode = "YYYYQL69L16Q001Y";

const subKey = `${__ENV.API_SUBSCRIPTION_KEY}`;
const accountPrimaryKey = `${__ENV.ACCOUNT_PRIMARY_KEY}`;

var containerIds = new Array();

export function setup() {
	// 1. setup code (once)
	// The setup code runs, setting up the test environment (optional) and generating data
	// used to reuse code for the same VU
	
	let accountPrimaryKey = 'Dusbar0FDNGLMt3EjcdQydBQd6ZwEofqLIlrqdBaeNshnbJh9huih3+IrXKp2bYdE9bhscptwymb+AStaDt3Pg==';
	//let response = getBlob(accountPrimaryKey,'pagopadweureceiptsfnsa','pagopa-d-weu-receipts-azure-blob-receipt-st-attach', 'helpdesk-pdf-p.pdf.pdf');
	let response = putBlob(accountPrimaryKey,'pagopadweureceiptsfnsa','pagopa-d-weu-receipts-azure-blob-receipt-st-attach', 'test-alessio.pdf');
	

/*
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
*/
	// precondition is moved to default fn because in this stage
	// __VU is always 0 and cannot be used to create env properly
}

// teardown the test data
export function teardown(data) {
	/*
	let response;
	for (const element of data.ids) {
		response = deleteDocument(cosmosDBURI, databaseID, containerViewGeneralID, accountPrimaryKey, element, element);
		check(response, { "status is 204": (res) => (res.status === 204) });
		response = deleteDocument(cosmosDBURI, databaseID, containerViewCartID, accountPrimaryKey, element, element);
		check(response, { "status is 204": (res) => (res.status === 204) });
		response = deleteDocument(cosmosDBURI, databaseID, containerViewUserID, accountPrimaryKey, element + "-p", userTaxCode);
	    check(response, { "status is 204": (res) => (res.status === 204) });
	}*/
}


export default function(data) {
	
	/*
	
	let idToDisable = getRandomItemFromArray(data.ids);
	
	group('01_DisableTransaction', function () {
	    let tag = {
			bizEventMethod: "DisableTransaction",
		};
		
	
		const params = {
			headers: {
			    'Ocp-Apim-Subscription-Key': subKey,
			    'x-fiscal-code': userTaxCode,
				'Content-Type': 'application/json'
			},
		};
	
		let response = disableTransaction(bizEventTrxURI, idToDisable, params);
	
		console.log(`DisableTransaction... [status: ${response.status}, transaction-id: ${idToDisable}]`);
	
		check(response, {"DisableTransaction status is 200": (res) => (res.status === 200)}, tag);
    });

*/	
	
}


