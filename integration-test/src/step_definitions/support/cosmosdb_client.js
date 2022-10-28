const {post, getDocumentForTest} = require("./common");
const fs = require('fs');

let rawdata = fs.readFileSync('./config/properties.json');
let properties = JSON.parse(rawdata);
const cosmos_db_uri = properties.cosmos_db_uri;


// Primary Key
var authorizationSignature = "9cFCgavL9CIA5YA2ouC6fUEOrnhSkxX617MNwWFfSC1Q958rQAw36zEt6HDZV4v2oi1eJfzPNksjSA7JTa6XIA=="; 
var authorizationType      = "master"
var authorizationVersion   = "1.0";
var cosmosDBApiVersion     = "2018-12-31";
// request method (a.k.a. verb) to build text for authorization token
var verb = 'post';



function getDocumentById(id) {  
	let resourceLink = "dbs/{databaseId}/colls/{containerId}/docs";
	// resource type (colls, docs...)
    var resourceType = "docs";
	let date = new Date().toUTCString();
	let authorizationToken = getCosmosDBAuthorizationToken(verb,authorizationType,authorizationVersion,authorizationSignature,resourceType,resourceLink,date);
	
	const headers = getCosmosDBAPIHeaders(authorizationType, authorizationVersion, authorizationToken, date);

    const body = {  
        "query": "SELECT * FROM c where c.id=\"" + id + "\"",
        "parameters": []
    }
    
    return post(cosmos_db_uri+resourceLink, headers, body)
}

function createDocument(id) {  
	let resourceLink = "dbs/{databaseId}/colls/{containerId}";
	// resource type (colls, docs...)
	let resourceType = "colls"
	let date = new Date().toUTCString();
	let authorizationToken = getCosmosDBAuthorizationToken(verb,authorizationType,authorizationVersion,authorizationSignature,resourceType,resourceLink,date);
	
	const headers = getCosmosDBAPIHeaders(authorizationType, authorizationVersion, authorizationToken, date);

    const body = getDocumentForTest(id);
    
    return post(cosmos_db_uri+resourceLink, headers, body)
}



function getCosmosDBAPIHeaders(autorizationType, autorizationVersion, authorizationToken, date){
	return { headers: 
		{'Accept': 'application/json',
		 'authorization_type': autorizationType,
		 'authorization_version': autorizationVersion,
		 'Authorization': authorizationToken,
		 'x-ms-version': cosmosDBApiVersion,
		 'x-ms-date': date,
		 'Content-Type': 'application/query+json',
         'x-ms-documentdb-isquery': 'true',
         'x-ms-query-enable-crosspartition': 'true'}
	} 
}

module.exports = {
	getDocumentById, createDocument
}