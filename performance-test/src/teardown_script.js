import { getTestData } from "./modules/helpers.js";

//DELETE RECEIPT FROM COSMOSDB
async function deleteDocumentFromReceiptsDatastore() {
    for (const element of getTestData().eventIds) {
    	const response = await deleteDocumentOnContainer(element);
    	check(response, { "status is 204": (res) => (res.statusCode === 204) });
    }
}
deleteDocumentFromReceiptsDatastore().then(resp => {
    console.info("RESPONSE DELETE RECEIPT STATUS", resp.statusCode);
});
