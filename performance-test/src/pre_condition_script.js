import { createTxEvents } from "./modules/tx_helpers.js";


//SAVE RECEIPT WITH BLOB INFO ON COSMOSDB
async function createDocumentInReceiptsDatastore() {
    await createTxEvents();
}

createDocumentInReceiptsDatastore().then(resp => {
  console.info("TX DATA SAVED");
});
