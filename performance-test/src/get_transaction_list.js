import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

import { getTransactionList } from "./modules/bizeventservice_client.js";
import { makeidMix, getRandomItemFromArray, makeRandomFiscalCode } from './modules/helpers.js';

const varsArray = new SharedArray('vars', function() {
	return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const cosmosDBURI = `${vars.cosmosDBURI}`;

const testData = vars.testData;

const subKey = `${__ENV.API_SUBSCRIPTION_KEY}`;


export default function(data) {

	// Get a transactionList
	let tag = {
		bizEventMethod: "GetTransactionList",
	};
	
	var itemToRecover = getRandomItemFromArray(testData);
	var fiscalCode = testData.fiscalCode;

	const params = {
		headers: {
		    'Ocp-Apim-Subscription-Key': subKey,
		    'x-fiscal-code': fiscalCode,
			'Content-Type': 'application/json'
		},
	};

	const response = getTransactionList(bizEventServiceURI, params);

	console.log(`GetTransactionList ... ${response.status}`);

	check(response, {"GetTransactionList status is 200": (res) => (res.status === 200)}, tag);

}
