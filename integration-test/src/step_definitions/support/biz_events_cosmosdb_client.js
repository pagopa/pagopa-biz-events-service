const { CosmosClient } = require("@azure/cosmos");
const { createEvent } = require("./common");

const cosmos_db_conn_string = process.env.COSMOS_DB_CONN_STRING;
const databaseId            = process.env.COSMOS_DB_NAME;  // es. db
const containerId           = process.env.COSMOS_DB_CONTAINER_NAME; // es. biz-events
const containerIdViewUser = process.env.COSMOS_DB_VIEW_USER_CONTAINER_NAME
const containerIdViewGeneral = process.env.COSMOS_DB_VIEW_GENERAL_CONTAINER_NAME
const containerIdViewCart = process.env.COSMOS_DB_VIEW_CART_CONTAINER_NAME

const client = new CosmosClient(cosmos_db_conn_string);
const database = client.database(databaseId);

//BIZ EVENTS
const container = database.container(containerId);

async function getDocumentByIdFromBizEventsDatastore(id) {
    return await container.items
        .query({
            query: "SELECT * from c WHERE c.id=@id",
            parameters: [{ name: "@id", value: id }]
        })
        .fetchAll();
}

async function createDocumentInBizEventsDatastore(event) {
    try {
        return await container.items.create(event);
    } catch (err) {
        console.log(err);
    }
}

async function deleteDocumentFromBizEventsDatastore(id) {
    try {
        return await container.item(id, id).delete();
    } catch (error) {
        if (error.code !== 404) {
            console.log(error)
        }
    }
}

//VIEW USER
const containerViewUser = database.container(containerIdViewUser);

async function getDocumentByTaxCodeFromViewUserDatastore(taxCode) {
    return await containerViewUser.items
        .query({
            query: "SELECT * from c WHERE c.taxCode=@taxCode",
            parameters: [{ name: "@taxCode", value: taxCode }]
        })
        .fetchAll();
}

async function createDocumentInViewUserDatastore(viewUser) {
    try {
        return await containerViewUser.items.create(viewUser);
    } catch (err) {
        console.log(err);
    }
}

async function deleteDocumentFromViewUserDatastore(id,taxCode) {
    try {
        return await containerViewUser.item(id, taxCode).delete();
    } catch (error) {
        if (error.code !== 404) {
            console.log(error)
        }
    }
}

//VIEW GENERAL
const containerViewGeneral = database.container(containerIdViewGeneral);

async function getDocumentByTransactionIdFromViewGeneralDatastore(transactionId) {
    return await containerViewGeneral.items
        .query({
            query: "SELECT * from c WHERE c.transactionId=@transactionId",
            parameters: [{ name: "@transactionId", value: transactionId }]
        })
        .fetchAll();
}

async function createDocumentInViewGeneralDatastore(viewGeneral) {
    try {
        return await containerViewGeneral.items.create(viewGeneral);
    } catch (err) {
        console.log(err);
    }
}

async function deleteDocumentFromViewGeneralDatastore(id, transactionId) {
    try {
        return await containerViewGeneral.item(id, transactionId).delete();
    } catch (error) {
        if (error.code !== 404) {
            console.log(error)
        }
    }
}

//VIEW CART
const containerViewCart = database.container(containerIdViewCart);

async function getDocumentByTransactionIdFromViewCartDatastore(transactionId) {
    return await containerViewCart.items
        .query({
            query: "SELECT * from c WHERE c.transactionId=@transactionId",
            parameters: [{ name: "@transactionId", value: transactionId }]
        })
        .fetchAll();
}

async function createDocumentInViewCartDatastore(viewCart) {
    try {
        return await containerViewCart.items.create(viewCart);
    } catch (err) {
        console.log(err);
    }
}

async function deleteDocumentFromViewCartDatastore(id, transactionId) {
    try {
        return await containerViewCart.item(id, transactionId).delete();
    } catch (error) {
        if (error.code !== 404) {
            console.log(error)
        }
    }
}

module.exports = {
    getDocumentByIdFromBizEventsDatastore, 
    createDocumentInBizEventsDatastore, 
    deleteDocumentFromBizEventsDatastore,

    getDocumentByTaxCodeFromViewUserDatastore,
    createDocumentInViewUserDatastore,
    deleteDocumentFromViewUserDatastore,

    getDocumentByTransactionIdFromViewGeneralDatastore,
    createDocumentInViewGeneralDatastore,
    deleteDocumentFromViewGeneralDatastore,

    getDocumentByTransactionIdFromViewCartDatastore,
    createDocumentInViewCartDatastore,
    deleteDocumentFromViewCartDatastore
}