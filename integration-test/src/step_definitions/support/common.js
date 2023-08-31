const axios = require("axios");
const cryptojs = require("crypto-js");

axios.defaults.headers.common['Ocp-Apim-Subscription-Key'] = process.env.SUBKEY // for all requests
if (process.env.canary) {
  axios.defaults.headers.common['X-Canary'] = 'canary' // for all requests
}

function get(url, headers) {
    return axios.get(url, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}

function post(url, body, headers) {
    return axios.post(url, body, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
	console.log(error)
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
    return axios.delete(url, {headers})
        .then(res => {
            return res;
        })
        .catch(error => {
            return error.response;
        });
}


function getCosmosDBAuthorizationToken(verb,autorizationType,autorizationVersion,authorizationSignature,resourceType,resourceLink,dateUtc){
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

module.exports = {get, post, put, del, getCosmosDBAuthorizationToken, getDocumentForTest}
