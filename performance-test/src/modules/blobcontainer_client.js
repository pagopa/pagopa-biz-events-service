import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';
import { SharedArray } from 'k6/data';

const apiVersion             = "2024-08-04";

export function getBlob(authorizationSignature, storageAccountName, containerName, blobName) {  
	let blobStorageURI = `https://${storageAccountName}.blob.core.windows.net/${containerName}/${blobName}`
	let date = new Date().toUTCString();
	// request method (a.k.a. verb) to build text for authorization token
    let verb = 'GET';
    let canonicalizedHeaders = 'x-ms-date:'+date+'\nx-ms-version:'+apiVersion+'\n';
    let canonicalizedResource = '/' + storageAccountName + '/' + containerName + '/' + blobName;
	let authorizationToken = getSharedKeyAuthorizationToken(authorizationSignature, verb, '', canonicalizedHeaders, canonicalizedResource);
	
	let headers = getHeaders(verb, authorizationToken, date);
	
	let params = {
		headers: headers
	};
    return http.get(blobStorageURI, params)
}


export function putBlob(authorizationSignature, storageAccountName, containerName, blobName, blobStrem) {  
	let blobStorageURI = `https://${storageAccountName}.blob.core.windows.net/${containerName}/${blobName}`
	let date = new Date().toUTCString();
	let body = blobStrem;
	let contentLength = body.byteLength;
	
	// request method (a.k.a. verb) to build text for authorization token
    let verb = 'PUT';
    let canonicalizedHeaders = 'x-ms-blob-type:BlockBlob\nx-ms-date:'+date+'\nx-ms-version:'+apiVersion+'\n';
    let canonicalizedResource = '/' + storageAccountName + '/' + containerName + '/' + blobName;
	let authorizationToken = getSharedKeyAuthorizationToken(authorizationSignature, verb, contentLength, canonicalizedHeaders, canonicalizedResource);
	
	let headers = getHeaders(verb, authorizationToken, date, contentLength);
	
	let params = {
		headers: headers
	};
    return http.put(blobStorageURI,body,params)
}

export function deleteBlob(authorizationSignature, storageAccountName, containerName, blobName) {  
	let blobStorageURI = `https://${storageAccountName}.blob.core.windows.net/${containerName}/${blobName}`
	let date = new Date().toUTCString();
	
	// request method (a.k.a. verb) to build text for authorization token
    let verb = 'DELETE';
    let canonicalizedHeaders = 'x-ms-date:'+date+'\nx-ms-version:'+apiVersion+'\n';
    let canonicalizedResource = '/' + storageAccountName + '/' + containerName + '/' + blobName;
	let authorizationToken = getSharedKeyAuthorizationToken(authorizationSignature, verb, '', canonicalizedHeaders, canonicalizedResource);
	
	let headers = getHeaders(verb, authorizationToken, date);
	
	let params = {
		headers: headers
	};
    return http.del(blobStorageURI, null, params)
}



function getHeaders(action, authorizationToken, date, contentLength){
	
	if (action == "GET" || action == "DELETE") {
		return {
			 'Authorization': authorizationToken,
			 'x-ms-version': apiVersion,
			 'x-ms-date': date
	     };
	} 
	else {
		return {
	   		 //'Content-Type': 'text/plain; charset=UTF-8',
			 'Authorization': authorizationToken,
			 'x-ms-version': apiVersion,
			 'x-ms-date': date,
	         //'x-ms-blob-content-disposition': 'attachment; filename="fname.ext"'  
	         'x-ms-blob-type': 'BlockBlob',  
	         //'x-ms-meta-m1': 'v1',  
	         //'x-ms-meta-m2': 'v2'  
	         //'x-ms-expiry-option': 'RelativeToNow'
	         //'x-ms-expiry-time': '30000'
	         'Content-Length': contentLength  
	     };
	}
}

function getSharedKeyAuthorizationToken(authorizationSignature, verb, contentLength, canonicalizedHeaders, canonicalizedResource) {

    let key = authorizationSignature;

    let stringParams = {
        'verb': verb,
        'Content-Encoding': '',
        'Content-Language': '',
        'Content-Length': '' + contentLength,
        'Content-MD5': '',
        'Content-Type': '',
        'Date': '',
        'If-Modified-Since': '',
        'If-Match': '',
        'If-None-Match': '',
        'If-Unmodified-Since': '',
        'Range': '',
        'CanonicalizedHeaders': canonicalizedHeaders,
        'CanonicalizedResource': canonicalizedResource
    }

    let strToSign = (stringParams['verb'] + '\n' +
        stringParams['Content-Encoding'] + '\n' +
        stringParams['Content-Language'] + '\n' +
        stringParams['Content-Length'] + '\n' +
        stringParams['Content-MD5'] + '\n' +
        stringParams['Content-Type'] + '\n' +
        stringParams['Date'] + '\n' +
        stringParams['If-Modified-Since'] + '\n' +
        stringParams['If-Match'] + '\n' +
        stringParams['If-None-Match'] + '\n' +
        stringParams['If-Unmodified-Since'] + '\n' +
        stringParams['Range'] + '\n' +
        stringParams['CanonicalizedHeaders'] +
        stringParams['CanonicalizedResource'])


    let decodedAccessKey = encoding.b64decode(key, 'std');
    const signature = crypto.hmac('sha256', decodedAccessKey, strToSign, 'base64');
    var auth = "SharedKey pagopadweureceiptsfnsa:" + signature;


    return auth;
}


