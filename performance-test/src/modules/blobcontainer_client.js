import http from 'k6/http';
import crypto from 'k6/crypto';
import encoding from 'k6/encoding';
import { SharedArray } from 'k6/data';

const apiVersion             = "2024-08-04";

const body = open('../resources/testPDF.pdf', 'b');

export function getBlob(authorizationSignature, storageAccountName, containerName, blobName) {  
	let blobStorageURI = `https://${storageAccountName}.blob.core.windows.net/${containerName}/${blobName}`
	let date = new Date().toUTCString();
	// request method (a.k.a. verb) to build text for authorization token
    let verb = 'GET';
	let authorizationToken = getSharedKeyAuthorizationToken(authorizationSignature, verb, 90727, date, storageAccountName, containerName, blobName);
	
	let headers = getHeaders("get", authorizationToken, date, 90727);
	
	let params = {
		headers: headers
	};
    return http.get(blobStorageURI, params)
}


export function putBlob(authorizationSignature, storageAccountName, containerName, blobName) {  
	let blobStorageURI = `https://${storageAccountName}.blob.core.windows.net/${containerName}/${blobName}`
	let date = new Date().toUTCString();
	// request method (a.k.a. verb) to build text for authorization token
    let verb = 'PUT';
	let authorizationToken = getSharedKeyAuthorizationToken(authorizationSignature, verb, 90727, date, storageAccountName, containerName, blobName);
	
	let headers = getHeaders("put", authorizationToken, date, 90727);
	
	let params = {
		headers: headers
	};
    return http.put(blobStorageURI,body,params)
}



function getHeaders(action, authorizationToken, date, contentLength){
	
	if (action == "get") {
		return {
	   		 //'Content-Type': 'application/pdf',
			 'Authorization': authorizationToken,
			 'x-ms-version': apiVersion,
			 'x-ms-date': date,
	         //'x-ms-blob-content-disposition': 'attachment; filename="fname.ext"'  
	         //'x-ms-blob-type': 'BlockBlob',  
	         //'x-ms-meta-m1': 'v1',  
	         //'x-ms-meta-m2': 'v2',  
	         //'x-ms-expiry-option': 'RelativeToNow'
	         //'x-ms-expiry-time': '30000'
	         //'Content-Length': contentLength  
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

function getSharedKeyAuthorizationToken(authorizationSignature, verb, contentLength, dateUtc, storageAccountName, containerName, blobName ){ 
	
	let key = authorizationSignature;
    
    let stringParams = {
    'verb': verb,
    'Content-Encoding': '',
    'Content-Language': '',
    'Content-Length': ''+contentLength,
    'Content-MD5': '',
    'Content-Type': '',
    'Date': '',
    'If-Modified-Since': '',
    'If-Match': '',
    'If-None-Match': '',
    'If-Unmodified-Since': '',
    'Range': '',
    //'CanonicalizedHeaders': 'x-ms-version:' + apiVersion + '\nx-ms-date:' + dateUtc + '\n' + 'x-ms-blob-type:BlockBlob'+'\n', //'\n'+'x-ms-meta-m1:v1\nx-ms-meta-m2:v2\n'+'x-ms-blob-type:BlockBlob'+'\n',
    'CanonicalizedHeaders': 'x-ms-blob-type:BlockBlob\nx-ms-date:'+dateUtc+'\nx-ms-version:'+apiVersion+'\n',
    'CanonicalizedResource': '/' + storageAccountName + '/' + containerName + '/' + blobName
}
    //let strToSign = "PUT\n\ntext/plain; charset=UTF-8\n\nx-ms-date:Sun, 20 Sep 2009 20:36:40 GMT\nx-ms-meta-m1:v1\nx-ms-meta-m2:v2\n/testaccount1/mycontainer/hello.txt";
    /*
    let strToSign  = verb 
                     + "\n" 
                     + "\n" 
                     + "\n" 
                     + ((contentLength > 0) ? contentLength : '') + "\n" 
                     + "\n" 
                     + "\n" 
                     + "\n" 
                     + "\n" 
                     + "\n" 
                     + "\n" 
                     + "\n" 
                     + "\n" 
                     + "x-ms-blob-type:BlockBlob" + "\n" 
                     + "x-ms-date:" + strDate + "\n" 
                     + "x-ms-version:" + apiVersion + "\n"
                     + "/"+storageAccountName+"/"+containerName+"/"+blobName;*/
 
   
   let strToSign = (stringParams['verb'] + '\n'
                  + stringParams['Content-Encoding'] + '\n'
                  + stringParams['Content-Language'] + '\n'
                  + stringParams['Content-Length'] + '\n'
                  + stringParams['Content-MD5'] + '\n'
                  + stringParams['Content-Type'] + '\n'
                  + stringParams['Date'] + '\n'
                  + stringParams['If-Modified-Since'] + '\n'
                  + stringParams['If-Match'] + '\n'
                  + stringParams['If-None-Match'] + '\n'
                  + stringParams['If-Unmodified-Since'] + '\n'
                  + stringParams['Range'] + '\n'
                  + stringParams['CanonicalizedHeaders']
                  + stringParams['CanonicalizedResource'])           
    
    console.log("*********",strToSign);
    
    /*
    
    let secret = encoding.b64encode(key,'rawstd');
    let hmacSha256 = crypto.createHMAC("sha256", secret); 
    hmacSha256.update(strToSign);
    let hashInBase64 = hmacSha256.digest("base64");
    let auth = "SharedKey pagopadweureceiptsfnsa:"+hashInBase64;*/
    
    
    /*
    let hmacSha256 = crypto.createHMAC("sha256", key); 
    hmacSha256.update(strToSign);
    let hashInBase64 = hmacSha256.digest("base64");
    let auth = "SharedKey pagopadweureceiptsfnsa:"+hashInBase64;*/
    
    
    
   
  /*
    let decodedAccessKey = encoding.b64decode(key,'std');
    let hmacSha256Sign = crypto.createHMAC('sha256', decodedAccessKey);
    hmacSha256Sign.update(strToSign, 'utf-8');
    //hmacSha256Sign.digest('base64');
    let auth = "SharedKey pagopadweureceiptsfnsa:"+hmacSha256Sign.digest('base64');*/
    let decodedAccessKey = encoding.b64decode(key,'std');
    const signature = crypto.hmac('sha256', decodedAccessKey, strToSign, 'base64'); //crypto.createHash('sha256', key);
var auth = "SharedKey pagopadweureceiptsfnsa:"+signature; 
    
    
    

//console.log("---------",auth);

return auth;
}


