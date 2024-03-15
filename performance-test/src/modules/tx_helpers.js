import { insertGeneralCartView, insertCartItemView } from '../modules/cosmosdb_client.js'

import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);

const environmentString = process.env.ENVIRONMENT_STRING || "local";
let environmentVars = require(`../${environmentString}.environment.json`)?.environment?.[0] || {};

const numberOfEventsToPreload = `${environmentVars.numberOfEventsToPreload}`;
const transactionDataTestData = environmentVars.testData;

export function getTestData() {
    return transactionDataTestData;
}

export async function createTxEvents() {
    const config = JSON.parse(JSON.stringify(transactionDataTestData));

    for (const element of config) {
        let fiscalCode = element.fiscalCode;
        let baseId = element.base_id;
        let totalNotice = element.totalNotice;
        insertGeneralCartView(baseId, fiscalCode, totalNotice);
        for (var i=0; i<totalNotice; i++) {
            await insertCartItemView(baseId, baseId+"_"+i, fiscalCode);
        }
    }

}


