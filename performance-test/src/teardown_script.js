import { getTestData } from "./modules/tx_helpers.js";
import { deleteGeneralDocumentOnContainer, deleteCartItemDocumentOnContainer, deleteCartUserDocumentOnContainer } from './modules/cosmosdb_client.js'

//DELETE RECEIPT FROM COSMOSDB
async function deleteDocumentFromReceiptsDatastore() {
    for (const element of getTestData()) {
        await deleteGeneralDocumentOnContainer( element.baseId, element.baseId);
        for (var i=0; i < element.totalNotice; i++) {
    	    await deleteCartItemDocumentOnContainer(element.baseId+"_"+i, element.baseId);
    	    await deleteCartUserDocumentOnContainer(element.baseId+"_"+i, element.fiscalCode);
    	}
    }
}
deleteDocumentFromReceiptsDatastore().then(resp => {
      console.info("TX DATA TEARDOWN COMPLETED");
});
