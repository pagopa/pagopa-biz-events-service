import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

import { createDocument, createTransactionListDocument, deleteDocumentOnContainer } from "./modules/cosmosdb_client.js";
import { getTransactionDetails } from "./modules/bizeventservice_client.js";
import { makeidMix, getRandomItemFromArray, makeRandomFiscalCode, getTestData } from './modules/helpers.js';

export let options = JSON.parse(open(__ENV.TEST_TYPE));

const varsArray = new SharedArray('vars', function() {
	return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const cosmosDBURI = `${vars.cosmosDBURI}`;
const numberOfEventsToPreload = `${vars.numberOfEventsToPreload}`;

var eventIds = new Array();
var containerIds = new Array();
var fiscalCodeMap = {};
var cartMap = {};


export async function setup() {
    return getTestData();
}

function precondition() {
	// no pre conditions
}

// teardown the test data
export async function teardown(data) {
	
	for (const element of data.eventIds) {
		const response = await deleteDocumentOnContainer(element);
		check(response, { "status is 204": (res) => (res.statusCode === 204) });
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
