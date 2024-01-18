import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';
import { SharedArray } from 'k6/data';
import { CosmosClient } from "@azure/cosmos";


var authorizationType      = "master"
var authorizationVersion   = "1.0";
var cosmosDBApiVersion     = "2018-12-31";

export let options = JSON.parse(open(__ENV.TEST_TYPE));

// read configuration
// note: SharedArray can currently only be constructed inside init code
// according to https://k6.io/docs/javascript-api/k6-data/sharedarray
const varsArray = new SharedArray('vars', function() {
	return JSON.parse(open(`../${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const databaseID = `${vars.databaseID}`;
const containerID = `${vars.containerID}`;

const connString = `${__ENV.API_SUBSCRIPTION_KEY}`;

var client;
var receiptContainer;


function getContainer() {
    if (client == undefined) {
        client = new CosmosClient(connString);
        receiptContainer = client.database(databaseID).container(containerID);
    }
    return receiptContainer;
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

export async function createTransactionListDocument(eventId, transactionId, fiscalCode, totalNotice) {

    const documentToSave = getDocumentForTest(eventId);
    documentToSave.payer.entityUniqueIdentifierValue = fiscalCode;
    documentToSave.debtor.entityUniqueIdentifierValue = fiscalCode;
    documentToSave.paymentInfo.totalNotice = String(totalNotice);
    documentToSave.transactionDetails = {
        transaction: {
            transactionId: transactionId
        }
    };
    try {
        return await getContainer().items.create(documentToSave);
    } catch (err) {
        throw new Error(
          "Error saving biz-event" + eventId + "to container " + containerID
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

export async function deleteDocumentOnContainer(id) {
    try {
        return await getContainer().item(id, id).delete();
    } catch (error) {
        if (error.code !== 404) {
            throw new Error("Error deleting biz-event " + id);
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

