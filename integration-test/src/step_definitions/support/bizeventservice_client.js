const {get} = require("./common");
const fs = require('fs');

let rawdata = fs.readFileSync('./config/properties.json');
let properties = JSON.parse(rawdata);
const bizevents_service_host = properties.bizevents_service_host;


function healthCheckInfo() {
    return get(bizevents_service_host+`info`, {})
}

function getOrganizationReceipt(organizationfiscalcode, iur, iuv) {
    return get(bizevents_service_host+`organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, {})
}

module.exports = {
	healthCheckInfo,
    getOrganizationReceipt
}
