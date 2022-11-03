import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';


var authorizationType      = "master"
var authorizationVersion   = "1.0";
var cosmosDBApiVersion     = "2018-12-31";



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

export function deleteDocument(cosmosDbURI, databaseId, containerId, authorizationSignature, id) {  
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

