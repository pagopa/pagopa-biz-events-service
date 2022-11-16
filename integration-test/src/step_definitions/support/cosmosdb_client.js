const {post, del, getDocumentForTest, getCosmosDBAuthorizationToken} = require("./common");



const cosmos_db_uri = process.env.COSMOS_DB_URI; // the cosmos account URI
// Primary Key
var databaseId             = process.env.DATABASE_ID;  // es. db
var containerId            = process.env.CONTAINER_ID; // es. biz-events
var authorizationSignature = process.env.PRIMARY_KEY;  // the cosmos accont Connection Primary Key
var authorizationType      = "master"
var authorizationVersion   = "1.0";
var cosmosDBApiVersion     = "2018-12-31";



function getDocumentById(id) {  
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

    return post(cosmos_db_uri+path, body, headers)
}

function createDocument(id) {  
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

    const body = getDocumentForTest(id);
    
    return post(cosmos_db_uri+path, body, headers)
}

function deleteDocument(id) {  
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
	
    return del(cosmos_db_uri+path, headers);
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


module.exports = {
	getDocumentById, createDocument, deleteDocument
}