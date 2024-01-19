import { createTransactionListDocument } from '../modules/cosmosdb_client.js'

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

    for (var element in transactionDataTestData) {
        let fiscalCode = transactionDataTestData.fiscalCode;
        let baseId = transactionDataTestData.baseId;
        let totalNotice = transactionDataTestData.totalNotice;
        for (var i=0; i<totalNotice; i++) {
            await createTransactionListDocument(baseId+"_"+i, baseId, fiscalCode, totalNotice);
        }
    }

}


