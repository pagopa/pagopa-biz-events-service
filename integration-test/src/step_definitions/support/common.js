const axios = require("axios");
const fs = require('fs');


function get(url, headers) {
    return axios.get(url, headers)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function post(url, body) {
    return axios.post(url, body)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function put(url, body) {
    return axios.put(url, body)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}


function del(url) {
    return axios.delete(url)
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function call(method, url, body) {
    if (method === 'GET') {
        return get(url)
    }
    if (method === 'POST') {
        return post(url, body)
    }
    if (method === 'PUT') {
        return put(url, body)
    }
    if (method === 'DELETE') {
        return del(url)
    }

}

function getCosmosDBAuthorizationToken(verb,autorizationType,autorizationVersion,authorizationSignature,resourceType,resourceLink,dateUtc){
	// Decode authorization signature
	let key = CryptoJS.enc.Base64.parse(authorizationSignature);
	// Build string to be encrypted and used as signature.
    // See: https://docs.microsoft.com/en-us/rest/api/cosmos-db/access-control-on-cosmosdb-resources
    let text = (verb || "").toLowerCase() + "\n" +
            (resourceType || "").toLowerCase() + "\n" +
            (resourceLink || "") + "\n" +
            dateUtc.toLowerCase() + "\n\n";
     // Build key to authorize request.
     let signature = CryptoJS.HmacSHA256(text, key);
     // Code key as base64 to be sent.
     let signature_base64 = CryptoJS.enc.Base64.stringify(signature);
     // Build autorization token, encode it and return 
	 return encodeURIComponent("type=" + autorizationType + "&ver=" + autorizationVersion + "&sig=" + signature_base64);
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
            "iuv": "10978194271631307"
        },
        "creditor": {
            "idPA": "66660006666",
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
            "paymentToken": "16cb4c797fd14d09899bdc161ff38d17",
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

module.exports = {get, post, put, del, call, getCosmosDBAuthorizationToken, getDocumentForTest}
