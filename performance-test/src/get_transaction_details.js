import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

import { createDocument, createTransactionListDocument, deleteDocument } from "./modules/cosmosdb_client.js";
import { getTransactionDetails } from "./modules/bizeventservice_client.js";
import { makeidMix, getRandomItemFromArray, makeRandomFiscalCode } from './modules/helpers.js';

export let options = JSON.parse(open(__ENV.TEST_TYPE));

// read configuration
// note: SharedArray can currently only be constructed inside init code
// according to https://k6.io/docs/javascript-api/k6-data/sharedarray
const varsArray = new SharedArray('vars', function() {
	return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const bizEventServiceURI = `${vars.bizEventServiceURI}`;
const cosmosDBURI = `${vars.cosmosDBURI}`;
const databaseID = `${vars.databaseID}`;
const containerID = `${vars.containerID}`;
const numberOfEventsToPreload = `${vars.numberOfEventsToPreload}`;

const accountPrimaryKey = `${__ENV.API_SUBSCRIPTION_KEY}`;

var containerIds = new Array();
var fiscalCodeMap = {};
var cartMap = {};


export function setup() {
	// 1. setup code (once)
	// The setup code runs, setting up the test environment (optional) and generating data
	// used to reuse code for the same VU

	for (let i = 0; i < numberOfEventsToPreload; i++) {
		let id = makeidMix(25);
		let totalNotice = Math.floor(Math.random() * 3)
		let fiscalCode = makeRandomFiscalCode();
		for (let j = 0; j < totalNotice; j++) {
            var id_cart = totalNotice > 1 ? id+"_"+j : id;
            const response = createTransactionListDocument(
                cosmosDBURI, databaseID, containerID, accountPrimaryKey, id_cart, id, fiscalCode, totalNotice);
            check(response, { "status is 201": (res) => (res.status === 201) });
		}
		containerIds.push(id);
        fiscalCodeMap[id] = fiscalCode;
        cartMap[id] = totalNotice>1;
	}

	
	 // return the array with preloaded id
	 return { ids: containerIds, fiscalCodeMap: fiscalCodeMap , cartMap = cartMap }
	 
	 // precondition is moved to default fn because in this stage
	 // __VU is always 0 and cannot be used to create env properly
}

function precondition() {
	// no pre conditions
}

// teardown the test data
export function teardown(data) {
	
	for (const element of data.ids) {
		const response = deleteDocument(cosmosDBURI, databaseID, containerID, accountPrimaryKey, element);
		check(response, { "status is 204": (res) => (res.status === 204) });
	}
}

export default function(data) {

	// Get a transaction detail
	let tag = {
		bizEventMethod: "getTransactionDetails",
	};
	
	var itemToRecover = getRandomItemFromArray(data.ids);
	var fiscalCode = data.fiscalCodeMap[itemToRecover];

	const params = {
		headers: {
		    'x-fiscal-code': fiscalCode,
			'Content-Type': 'application/json'
		},
	};

	var isCart = data.cartMap[itemToRecover];

	const response = getTransactionDetails(bizEventServiceURI, itemToRecover, isCart, params);

	console.log(`getTransactionDetails ... ${response.status}`);

	check(response, {"getTransactionDetails status is 200": (res) => (res.status === 200)}, tag);

}
