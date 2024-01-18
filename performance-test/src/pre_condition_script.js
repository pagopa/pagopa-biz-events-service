import { createTxEvents } from "./modules/helpers.js";


//SAVE RECEIPT WITH BLOB INFO ON COSMOSDB
async function createDocumentInReceiptsDatastore() {
    await createTxEvents();
}

createDocumentInReceiptsDatastore().then(resp => {
  console.info("RESPONSE SAVE RECEIPT");
});
