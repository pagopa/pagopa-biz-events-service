const { CosmosClient, ConnectionPolicy } = require("@azure/cosmos");
const { get } = require("http");


//biz
const biz_cosmos_endpoint = process.env.BIZ_COSMOS_ENDPOINT || "";
const biz_cosmos_key = process.env.BIZ_COSMOS_KEY || "";
const biz_databaseId = process.env.BIZ_COSMOS_DB_NAME;
const bizContainerId = process.env.BIZ_COSMOS_DB_CONTAINER_NAME;
const biz_request_timeout = process.env.RECEIPTS_COSMOS_TIMEOUT || 60000; // msec > 2min

// https://learn.microsoft.com/en-us/azure/cosmos-db/nosql/tutorial-global-distribution?tabs=dotnetv2%2Capi-async#nodejsjavascript
// Setting read region selection preference, in the following order -
// 1 - West Europe
// 2 - North Europe
// const preferredLocations_ = ['West Europe', 'North Europe'];
const preferredLocations_ = ['North Europe'];

const biz_client = new CosmosClient({
   endpoint: biz_cosmos_endpoint,
   key: biz_cosmos_key,
      connectionPolicy: {
         requestTimeout: biz_request_timeout,
         preferredLocations :  preferredLocations_
      }, 
});
const bizContainer = biz_client.database(biz_databaseId).container(bizContainerId);



// SELECT COUNT(1) AS eventStatusCount, r.eventStatus FROM ITEM r 
// WHERE c.timestamp >= DateTimeToTimestamp("2024-07-01T00:00:00") and c.timestamp < DateTimeToTimestamp("2024-07-31T23:59:59")
// GROUP BY r.eventStatus

async function getBizCount(data_from, data_to) {
    return await bizContainer.items
        .query({
            query: `SELECT count(1) as eventStatusCount, c.eventStatus FROM ITEM c WHERE c.eventStatus in ('INGESTED','DONE')
            and c.timestamp >= DateTimeToTimestamp(@datefrom)
            and c.timestamp <= DateTimeToTimestamp(@dateto)
            GROUP BY c.eventStatus`,
            parameters: [
                { name: "@datefrom", value: data_from },
                { name: "@dateto", value: data_to },
            ]
        })
        .fetchAll();
}

module.exports = {
    getBizCount
}
