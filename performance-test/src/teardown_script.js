import { getTestData } from "./modules/tx_helpers.js";
import { deleteDocumentOnContainer } from './modules/cosmosdb_client.js'

//DELETE RECEIPT FROM COSMOSDB
async function deleteDocumentFromReceiptsDatastore() {
    for (const element of getTestData()) {
        for (var i=0; i < element.totalNotice; i++) {
    	    await deleteDocumentOnContainer(element.baseId+"_"+i);
    	}
    }
}
deleteDocumentFromReceiptsDatastore().then(resp => {
      console.info("TX DATA TEARDOWN COMPLETED");
});
