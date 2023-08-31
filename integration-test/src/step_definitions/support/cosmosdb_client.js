const {post, del, getDocumentForTest, getCosmosDBAuthorizationToken} = require("./common");



const cosmos_db_uri = process.env.COSMOS_DB_URI; // the cosmos account URI
// Primary Key
const databaseId             = process.env.COSMOS_DB_NAME;  // es. db
const containerId            = process.env.COSMOS_DB_CONTAINER_NAME; // es. biz-events
const authorizationSignature = process.env.COSMOS_DB_PRIMARY_KEY;  // the cosmos accont Connection Primary Key
const authorizationType      = "master"
const authorizationVersion   = "1.0";
const cosmosDBApiVersion     = "2018-12-31";



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
	let headers = getCosmosDBAPIHeaders(authorizationToken, date, partitionKeyArray, 'application/query+json', 'true');

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
	let headers = getCosmosDBAPIHeaders(authorizationToken, date, partitionKeyArray, 'application/json', 'false');

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
	let headers = getCosmosDBAPIHeaders(authorizationToken, date, partitionKeyArray, 'application/json', 'false');
	
    return del(cosmos_db_uri+path, headers);
}


function getCosmosDBAPIHeaders(authorizationToken, date, partitionKeyArray, contentType, isQuery){

    return {'Accept': 'application/json',
        'Content-Type': contentType,
        'Authorization': authorizationToken,
        'x-ms-version': cosmosDBApiVersion,
        'x-ms-date': date,
        'x-ms-documentdb-isquery': isQuery,
        'x-ms-query-enable-crosspartition': 'true',
        'x-ms-documentdb-partitionkey': partitionKeyArray
    };
}


module.exports = {
	getDocumentById, createDocument, deleteDocument
}