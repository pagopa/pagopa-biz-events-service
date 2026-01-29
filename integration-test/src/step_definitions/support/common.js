const axios = require("axios");
const cryptojs = require("crypto-js");

axios.defaults.headers.common['Ocp-Apim-Subscription-Key'] = process.env.SUBKEY || "";// for all requests
if (process.env.CANARY) {
	axios.defaults.headers.common['X-Canary'] = 'canary' // for all requests
}

function get(url, headers) {
	return axios.get(url, { headers })
		.then(res => {
			return res;
		})
		.catch(error => {
			return error.response;
		});
}

function post(url, body, headers) {
	return axios.post(url, body, { headers })
		.then(res => {
			return res;
		})
		.catch(error => {
			return error.response;
		});
}


function put(url, body, headers) {
	return axios.put(url, body, headers)
		.then(res => {
			return res;
		})
		.catch(error => {
			return error.response;
		});
}


function del(url, headers) {
	return axios.delete(url, { headers })
		.then(res => {
			return res;
		})
		.catch(error => {
			return error.response;
		});
}


function getCosmosDBAuthorizationToken(verb, autorizationType, autorizationVersion, authorizationSignature, resourceType, resourceLink, dateUtc) {
	// Decode authorization signature
	let key = cryptojs.enc.Base64.parse(authorizationSignature);
	// Build string to be encrypted and used as signature.
	// See: https://docs.microsoft.com/en-us/rest/api/cosmos-db/access-control-on-cosmosdb-resources
	let text = (verb || "").toLowerCase() + "\n" +
		(resourceType || "").toLowerCase() + "\n" +
		(resourceLink || "") + "\n" +
		dateUtc.toLowerCase() + "\n\n";
	// Build key to authorize request.
	let signature = cryptojs.HmacSHA256(text, key);
	// Code key as base64 to be sent.
	let signature_base64 = cryptojs.enc.Base64.stringify(signature);

	// Build autorization token, encode it and return 
	return encodeURIComponent("type=" + autorizationType + "&ver=" + autorizationVersion + "&sig=" + signature_base64);
}

function makeId(length) {
	let result = '';
	const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
	const charactersLength = characters.length;
	let counter = 0;
	while (counter < length) {
		result += characters.charAt(Math.floor(Math.random() * charactersLength));
		counter += 1;
	}
	return result;
}

function getDocumentForTest(id) {
	return {
		"id": id,
		"version": "2",
		"idPaymentManager": "11999923",
		"complete": "false",
		"receiptId": "123456789",
		"missingInfo": [
			"paymentInfo.primaryCiIncurredFee",
			"paymentInfo.idBundle",
			"paymentInfo.idCiBundle"
		],
		"debtorPosition": {
			"modelType": "2",
			"noticeNumber": "310978194271631307",
			"iuv": "iuv-" + id
		},
		"creditor": {
			"idPA": "fiscalCode-" + id,
			"idBrokerPA": "66660006666",
			"idStation": "66666666666_08",
			"companyName": "PA giacomo"
		},
		"psp": {
			"idPsp": "60001110001",
			"idBrokerPsp": "60001110001",
			"idChannel": "60000000001_08",
			"psp": "PSP Giacomo",
			"channelDescription": "WISP"
		},
		"debtor": {
			"fullName": "paGetPaymentName",
			"entityUniqueIdentifierType": "G",
			"entityUniqueIdentifierValue": "44445554444"
		},
		"payer": {
			"fullName": "name",
			"entityUniqueIdentifierType": "G",
			"entityUniqueIdentifierValue": "77776667777_01"
		},
		"paymentInfo": {
			"paymentDateTime": "2022-10-24T15:09:16.987603",
			"applicationDate": "2021-12-12",
			"transferDate": "2021-12-11",
			"dueDate": "2021-12-12",
			"paymentToken": "iur-" + id,
			"amount": "10.50",
			"fee": "2.00",
			"totalNotice": "1",
			"paymentMethod": "creditCard",
			"touchpoint": "app",
			"remittanceInformation": "test"
		},
		"transferList": [
			{
				"fiscalCodePA": "66660006666",
				"companyName": "PA giacomo",
				"amount": "2.50",
				"transferCategory": "paGetPaymentTest",
				"remittanceInformation": "/RFB/00202200000217527/5.00/TXT/"
			},
			{
				"fiscalCodePA": "66666666666",
				"companyName": "PA paolo",
				"amount": "8.00",
				"transferCategory": "paGetPaymentTest",
				"remittanceInformation": "/RFB/00202200000217527/5.00/TXT/"
			}
		]
	}
}

function createEvent(id, transactionId, totalNotice, debtorFiscalCode, payerFiscalCode, amount) {
	return {
		"id": id,
		"version": "2",
		"idPaymentManager": "54927408",
		"complete": "false",
		"receiptId": "9851395f09544a04b288202299193ca6",
		"missingInfo": [
			"psp.pspPartitaIVA",
			"paymentInfo.primaryCiIncurredFee",
			"paymentInfo.idBundle",
			"paymentInfo.idCiBundle"
		],
		"debtorPosition": {
			"modelType": "2",
			"noticeNumber": "310391366991197059",
			"iuv": "10391366991197059"
		},
		"creditor": {
			"idPA": "66666666666",
			"idBrokerPA": "66666666666",
			"idStation": "66666666666_08",
			"companyName": "PA paolo",
			"officeName": "office"
		},
		"psp": {
			"idPsp": "60000000001",
			"idBrokerPsp": "60000000001",
			"idChannel": "60000000001_08",
			"psp": "PSP Paolo",
			"pspFiscalCode": "CF60000000006",
			"channelDescription": "app"
		},
		"debtor": {
			"fullName": "paGetPaymentName",
			"entityUniqueIdentifierType": "G",
			"entityUniqueIdentifierValue": debtorFiscalCode || "JHNDOE00A01F205N",
			"streetName": "paGetPaymentStreet",
			"civicNumber": "paGetPayment99",
			"postalCode": "20155",
			"city": "paGetPaymentCity",
			"stateProvinceRegion": "paGetPaymentState",
			"country": "IT",
			"eMail": "paGetPayment@test.it"
		},
		"payer": {
			"fullName": "name",
			"entityUniqueIdentifierType": "G",
			"entityUniqueIdentifierValue": payerFiscalCode || "JHNDOE00A01F205S",
			"streetName": "street",
			"civicNumber": "civic",
			"postalCode": "postal",
			"city": "city",
			"stateProvinceRegion": "state",
			"country": "IT",
			"eMail": "prova@test.it"
		},
		"paymentInfo": {
			"paymentDateTime": "2023-03-17T16:37:36.955813",
			"applicationDate": "2021-12-12",
			"transferDate": "2021-12-11",
			"dueDate": "2021-12-12",
			"paymentToken": "9851395f09544a04b288202299193ca6",
			"amount": "10.0",
			"fee": "2.0",
			"totalNotice": totalNotice ? totalNotice : "1",
			"paymentMethod": "creditCard",
			"touchpoint": "app",
			"remittanceInformation": "TARI 2021",
			"description": "TARI 2021",
			"metadata": [
				{
					"key": "1",
					"value": "22"
				}
			]
		},
		"transferList": [
			{
				"idTransfer": "1",
				"fiscalCodePA": "66666666666",
				"companyName": "PA paolo",
				"amount": "10.0",
				"transferCategory": "paGetPaymentTest",
				"remittanceInformation": "/RFB/00202200000217527/5.00/TXT/"
			}
		],
		"transactionDetails": {
			"user": {
				"fullName": "John Doe",
				"type": "F",
				"fiscalCode": payerFiscalCode || "JHNDOE00A01F205N",
				"notificationEmail": "john.doe@mail.it",
				"userId": "1234",
				"userStatus": "11",
				"userStatusDescription": "REGISTERED_SPID"
			},
			"transaction": {
				"idTransaction": "123456",
				"transactionId": transactionId ? transactionId : "123456",
				"grandTotal": amount || 0,
				"amount": 0,
				"fee": 0
			}
		},
		"timestamp": 1679067463501,
		"properties": {
			"diagnostic-id": "00-f70ef3167cffad76c6657a67a33ee0d2-61d794a75df0b43b-01",
			"serviceIdentifier": "NDP002SIT"
		},
		"eventStatus": "DONE",
		"eventRetryEnrichmentCount": 0
	};
}

function createViewUser(taxCode, id, transactionId, hidden, isPayer) {
	return {
		"id": id + (isPayer ? "-p" : "-d"),
		taxCode: taxCode,
		transactionId: transactionId,
		transactionDate: "2024-01-24T10:43:36.322Z",
		hidden: hidden ?? false,
		isPayer: isPayer ?? false
	};
}

function createViewGeneral(id, transactionId, payerTaxCode, isCart) {
	return {
		"id": id,
		"transactionId": transactionId,
		"authCode": "string",
		"paymentMethod": "BBT",
		"rnn": "string",
		"pspName": "string",
		"transactionDate": "2024-01-24T10:43:36.322Z",
		"walletInfo": {
			"accountHolder": "string",
			"brand": "string",
			"blurredNumber": "string"
		},
		"payer": {
			"name": "string",
			"taxCode": payerTaxCode
		},
		"isCart": isCart ?? false,
		"fee": "string",
		"origin": "INTERNAL",
		"totalNotice": 1
	};
}

function createViewCart(id, transactionId, taxCode) {
	return {
		"id": id,
		"transactionId": transactionId,
		"eventId": id,
		"amount": 100,
		"subject": "string",
		"payee": {
			"name": "string",
			"taxCode": "string"
		},
		"debtor": {
			"name": "string",
			"taxCode": taxCode
		},
		"refNumberValue": "string",
		"refNumberType": "string"
	};
}

function createReceipt(eventId, pdfName, status, errCode) {
	return {
		"eventId": eventId,
		"id": eventId,
		"eventData": {
			"payerFiscalCode": "INTTST00A00A000E",
		},
		"status": status,
		"mdAttachPayer": {
			"name": pdfName,
		},
		"reasonErrPayer": {
			"code": errCode
		}
	}
}

module.exports = {
	get,
	post,
	put,
	del,
	getCosmosDBAuthorizationToken,
	getDocumentForTest,
	createEvent,
	makeId,
	createViewUser,
	createViewGeneral,
	createViewCart,
	createReceipt
}
