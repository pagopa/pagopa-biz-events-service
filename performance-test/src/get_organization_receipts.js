import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

import { createDocument, deleteDocument } from "./modules/cosmosdb_client.js";
import { getOrganizationReceipt } from "./modules/bizeventservice_client.js";
import { makeidMix, getRandomItemFromArray } from './modules/helpers.js';

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


export function setup() {
	// 1. setup code (once)
	// The setup code runs, setting up the test environment (optional) and generating data
	// used to reuse code for the same VU

	for (let i = 0; i < numberOfEventsToPreload; i++) {
		let id = makeidMix(25);
		const response = createDocument(cosmosDBURI, databaseID, containerID, accountPrimaryKey, id);
		check(response, { "status is 201": (res) => (res.status === 201) });
		containerIds.push(id);
	}

	
	 // return the array with preloaded id
	 return { ids: containerIds }
	 
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


	// Get a receipt 
	let tag = {
		bizEventMethod: "GetOrganizationReceipt",
	};

	const params = {
		headers: {
			'Content-Type': 'application/json'
		},
	};
	
	var itemToRecover = getRandomItemFromArray(data.ids);
	let organizationFiscalCode = "fiscalCode-" + itemToRecover
	let iur = "iur-" + itemToRecover
	let iuv = "iuv-" + itemToRecover

	const response = getOrganizationReceipt(bizEventServiceURI, organizationFiscalCode, iur, iuv, params);

	console.log(`GetOrganizationReceipt ... ${response.status}`);

	check(response, {"GetOrganizationReceipt status is 200": (res) => (res.status === 200)}, tag);
}
