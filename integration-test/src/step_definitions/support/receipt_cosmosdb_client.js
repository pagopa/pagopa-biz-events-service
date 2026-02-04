const { CosmosClient } = require("@azure/cosmos");

const cosmos_db_conn_string = process.env.RECEIPT_COSMOS_DB_CONN_STRING;
const databaseId            = process.env.RECEIPT_COSMOS_DB_NAME;
const containerReceiptId           = process.env.RECEIPT_COSMOS_DB_CONTAINER_NAME;

const client = new CosmosClient(cosmos_db_conn_string);
const database = client.database(databaseId);

const container = database.container(containerReceiptId);

// RECEIPT
async function getDocumentByIdFromReceiptDatastore(id) {
    return await container.items
        .query({
            query: "SELECT * from c WHERE c.id=@id",
            parameters: [{ name: "@id", value: id }]
        })
        .fetchAll();
}

async function createDocumentInReceiptDatastore(receipt) {
    try {
        return await container.items.create(receipt);
    } catch (err) {
        console.log(err);
    }
}

async function deleteDocumentFromReceiptDatastore(id, eventId) {
    try {
        return await container.item(id, eventId).delete();
    } catch (error) {
        if (error.code !== 404) {
            console.log(error)
        }
    }
}

module.exports = {
    getDocumentByIdFromReceiptDatastore,
    createDocumentInReceiptDatastore,
    deleteDocumentFromReceiptDatastore
}