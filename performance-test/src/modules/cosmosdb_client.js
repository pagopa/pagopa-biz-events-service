import { CosmosClient } from "@azure/cosmos";
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);

const environmentString = process.env.ENVIRONMENT_STRING || "local";
let environmentVars = require(`../${environmentString}.environment.json`)?.environment?.[0] || {};

const databaseID = `${environmentVars.databaseID}`;
const cartGeneralContainerId = `${environmentVars.cartGeneralContainerId}`;
const cartItemContainerId = `${environmentVars.cartItemContainerId}`;
const cartUserContainerId = `${environmentVars.cartUserContainerId}`;


const connString = process.env.COSMOS_BIZ_EVENTS_CONN_STRING;

var client;
var cartGeneralContainer;
var cartItemContainer;
var cartUserContainer;


function getCartGeneralContainer() {
    if (client == undefined) {
        client = new CosmosClient(connString);
        cartGeneralContainer = client.database(databaseID).container(cartGeneralContainerId);
    }
    return cartGeneralContainer;
}

function getCartItemContainer() {
    if (client == undefined) {
        client = new CosmosClient(connString);
        cartItemContainer = client.database(databaseID).container(cartItemContainerId);
    }
    return cartItemContainerId;
}

function getCartUserContainer() {
    if (client == undefined) {
        client = new CosmosClient(connString);
        cartUserContainer = client.database(databaseID).container(cartUserContainerId);
    }
    return cartUserContainer;
}


export function getDocumentById(cosmosDbURI, databaseId, containerId, authorizationSignature, id) {  
	let path = `dbs/${databaseId}/colls/${containerId}/docs`;
	let resourceLink = `dbs/${databaseId}/colls/${containerId}`;
	// resource type (colls, docs...)
    let resourceType = "docs";
	let date = new Date().toUTCString();
	// request method (a.k.a. verb) to build text for authorization token
    let verb = 'post';
	let authorizationToken = getCosmosDBAuthorizationToken(verb,authorizationType,authorizationVersion,authorizationSignature,resourceType,resourceLink,date);
	
	let partitionKeyArray = [];
	let headers = getCosmosDBAPIHeaders(authorizationToken, date, partitionKeyArray, 'application/query+json');

    const body = {  
        "query": "SELECT * FROM c where c.id=\"" + id + "\"",
        "parameters": []
    }

    return http.post(cosmosDbURI+path, body, {headers});
}

export function createDocument(cosmosDbURI, databaseId, containerId, authorizationSignature, id) {  
	let path = `dbs/${databaseId}/colls/${containerId}/docs`;
	let resourceLink = `dbs/${databaseId}/colls/${containerId}`;
	// resource type (colls, docs...)
	let resourceType = "docs"
	let date = new Date().toUTCString();
	// request method (a.k.a. verb) to build text for authorization token
    let verb = 'post';
	let authorizationToken = getCosmosDBAuthorizationToken(verb,authorizationType,authorizationVersion,authorizationSignature,resourceType,resourceLink,date);
	
	let partitionKeyArray = "[\""+id+"\"]";
	let headers = getCosmosDBAPIHeaders(authorizationToken, date, partitionKeyArray, 'application/json');
	
	let params = {
		headers: headers,
	};

    const body = JSON.stringify(getDocumentForTest(id));
    
    return http.post(cosmosDbURI+path, body, params)
}

export async function insertGeneralCartView(transactionId, fiscalCode, totalNotice) {

    try {
        await getCartGeneralContainer().items.create(createGeneralCartView(transactionId, fiscalCode, totalNotice));
    } catch (err) {
        throw new Error(
          "Error saving biz-event cart-general-view " + transactionId + " to container " + cartGeneralContainerId
        );
    }

    try {
        await getCartUserContainer().items.create(createDetailUserView(fiscalCode, transactionId));
    } catch (err) {
        throw new Error(
          "Error saving biz-event cart-user-view " + transactionId + " for taxCode " + fiscalCode + " to container " + cartUserContainerId
        );
    }

}

export async function insertCartItemView(eventId, transactionId, fiscalCode) {

    try {
        await getCartItemContainer().items.create(createDetailCartView(eventId, transactionId, fiscalCode));
    } catch (err) {
        throw new Error(
          "Error saving biz-event cart-general-item " + eventId + " to container " + cartItemContainerId
        );
    }

    try {
        await getCartUserContainer().items.create(createDetailUserView(fiscalCode, transactionId));
    } catch (err) {
        console.log(err);
        throw new Error(
          "Error saving biz-event cart-user-view " + eventId + " for taxCode " + fiscalCode + " to container " + cartUserContainerId
        );
    }

}

export async function deleteDocument(cosmosDbURI, databaseId, containerId, authorizationSignature, id) {
	let path = `dbs/${databaseId}/colls/${containerId}/docs/${id}`;
	let resourceLink = path;
	// resource type (colls, docs...)
	let resourceType = "docs"
	let date = new Date().toUTCString();
	// request method (a.k.a. verb) to build text for authorization token
    let verb = 'delete';
	let authorizationToken = getCosmosDBAuthorizationToken(verb,authorizationType,authorizationVersion,authorizationSignature,resourceType,resourceLink,date);

	let partitionKeyArray = "[\""+id+"\"]";
	let headers = getCosmosDBAPIHeaders(authorizationToken, date, partitionKeyArray, 'application/json');

	let params = {
		headers: headers,
	};

    return http.del(cosmosDbURI+path, null, params);
}

export async function deleteGeneralDocumentOnContainer(id, partkey) {
    try {
        return await getCartGeneralContainer().item(id, partkey).delete();
    } catch (error) {
        if (error.code !== 404) {
            throw new Error("Error deleting biz-event-cart-general-view " + id);
        }
    }
}

export async function deleteCartItemDocumentOnContainer(id) {
    try {
        return await getCartItemContainer().item(id, id).delete();
    } catch (error) {
        if (error.code !== 404) {
            throw new Error("Error deleting biz-event-cart-item-view " + id);
        }
    }
}

export async function deleteCartUserDocumentOnContainer(id, partKey) {
    try {
        return await getCartUserContainer().item(id, partKey).delete();
    } catch (error) {
        if (error.code !== 404) {
            throw new Error("Error deleting biz-event-cart-item-user " + id);
        }
    }
}


function getCosmosDBAPIHeaders(authorizationToken, date, partitionKeyArray, contentType){
	 
   return {'Accept': 'application/json',
   		 'Content-Type': contentType,
		 'Authorization': authorizationToken,
		 'x-ms-version': cosmosDBApiVersion,
		 'x-ms-date': date,
         'x-ms-documentdb-isquery': 'true',
         'x-ms-query-enable-crosspartition': 'true',
         'x-ms-documentdb-partitionkey': partitionKeyArray}; 
}

function getCosmosDBAuthorizationToken(verb,autorizationType,autorizationVersion,authorizationSignature,resourceType,resourceLink,dateUtc){ 
	// Decode authorization signature
	let key = encoding.b64decode(authorizationSignature);
    let text = (verb || "").toLowerCase() + "\n" +
            (resourceType || "").toLowerCase() + "\n" +
            (resourceLink || "") + "\n" +
            dateUtc.toLowerCase() + "\n\n";
    let hmacSha256 = crypto.createHMAC("sha256", key); 
    hmacSha256.update(text);
    // Build autorization token, encode it and return 
    return encodeURIComponent("type=" + autorizationType + "&ver=" + autorizationVersion + "&sig=" + hmacSha256.digest("base64"));  
}

function getDocumentForTest(id) {
    return {
        "id": id,
        "version": "1",
        "idPaymentManager": "11999923",
        "complete": "false",
        "missingInfo": [
            "paymentInfo.primaryCiIncurredFee",
            "paymentInfo.idBundle",
            "paymentInfo.idCiBundle"
        ],
        "debtorPosition": {
            "modelType": "2",
            "noticeNumber": "310978194271631307",
            "iuv": "iuv-"+id
        },
        "creditor": {
            "idPA": "fiscalCode-"+id,
            "idBrokerPA": "66660006666",
            "idStation": "66666666666_08",
            "companyName": "PA giacomo"
        },
        "psp": {
            "idPsp": "60001110001",
            "idBrokerPsp": "60001110001",
            "idChannel": "60000000001_08",
            "psp": "PSP Giacomo"
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
            "paymentToken": "iur-"+id,
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

function createGeneralCartView(transactionId, payerTaxCode, totalNotice) {
    return {
        "id": transactionId,
        "transactionId": transactionId,
        "authCode": "authCode",
        "paymentMethod": "creditCard",
        "rrn": "rrn",
        "pspName": "PSP Giacomo",
        "transactionDate": "2024-01-24T10:43:36.322Z",
        "walletInfo": {
            "accountHolder": "accountHolder",
            "brand": "brand",
            "blurredNumber": "blurredNumber"
        },
        "payer": {
          "name": "name",
          "taxCode": payerTaxCode
        },
        "isCart": true,
        "fee": "2.00",
        "origin": "INTERNAL",
        "totalNotice": totalNotice
      }
}

function createDetailCartView(eventId, transactionId, debtorTaxCode) {
    return {
        "id": eventId,
        "transactionId": transactionId,
        "eventId": eventId,
        "amount": 2.00,
        "subject": "test",
        "payee": {
           "name": "string",
           "taxCode": "string"
        },
        "debtor": {
           "name": debtorTaxCode,
           "taxCode": "string"
        },
        "refNumberValue": "iuv-"+id,
        "refNumberType": "IUV"
    }
}

function createDetailUserView(taxCode, transactionId, eventId) {
    return {
        "id": eventId,
        "transactionId": transactionId,
        "taxCode": taxCode,
        "transactionDate": "2024-01-24T10:43:36.322Z",
        "hidden": "false"
    }
}
