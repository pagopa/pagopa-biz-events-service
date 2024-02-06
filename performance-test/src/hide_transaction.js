import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

import { getTransactionDetails } from "./modules/bizeventservice_client.js";
import { makeidMix, getRandomItemFromArray, makeRandomFiscalCode, getTestData } from './modules/helpers.js';

export let options = JSON.parse(open(__ENV.TEST_TYPE));

const varsArray = new SharedArray('vars', function() {
	return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const cosmosDBURI = `${vars.cosmosDBURI}`;
const bizEventServiceURI = `${vars.bizEventServiceURI}`;

const testData = vars.testData;

const subKey = `${__ENV.API_SUBSCRIPTION_KEY}`;

export default function() {

	// Disable a transaction
	let tag = {
		bizEventMethod: "disableTransaction",
	};
	
	var itemToRecover = getRandomItemFromArray(testData);
	var fiscalCode = itemToRecover.fiscalCode;

	const params = {
		headers: {
		    'Ocp-Apim-Subscription-Key': subKey,
		    'x-fiscal-code': fiscalCode,
			'Content-Type': 'application/json'
		},
	};

	const response = disableTransaction(
	    bizEventServiceURI, itemToRecover.baseId , params);
	console.log(`disableTransaction ... ${response.status}`);

	check(response, {"disableTransaction status is 200": (res) => (res.status === 200)}, tag);

}
