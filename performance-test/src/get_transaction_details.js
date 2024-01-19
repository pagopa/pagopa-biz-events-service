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
const numberOfEventsToPreload = `${vars.numberOfEventsToPreload}`;

const testData = vars.testData;

const subKey = `${__ENV.API_SUBSCRIPTION_KEY}`;

export default function() {

	// Get a transaction detail
	let tag = {
		bizEventMethod: "getTransactionDetails",
	};
	
	var itemToRecover = getRandomItemFromArray(data);

	const params = {
		headers: {
		    'Ocp-Apim-Subscription-Key': subKey,
		    'x-fiscal-code': itemToRecover.fiscalCode,
			'Content-Type': 'application/json'
		},
	};

	var isCart = itemToRecover.totalNotice>1;
	const response = getTransactionDetails(
	    bizEventServiceURI, isCart ? itemToRecover.baseId : itemToRecover.baseId+"_0", isCart, params);
	console.log(`getTransactionDetails ... ${response.status}`);

	check(response, {"getTransactionDetails status is 200": (res) => (res.status === 200)}, tag);

}
