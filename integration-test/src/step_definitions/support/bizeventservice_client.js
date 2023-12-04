const {get} = require("./common");

const bizevents_service_host = process.env.BIZ_EVENTS_SERVICE_HOST;

function healthCheckInfo() {
    return get(bizevents_service_host+`info`, {})
}

function getOrganizationReceipt(organizationfiscalcode, iur, iuv) {
    return get(bizevents_service_host+`organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, {})
}

function getBizEventById(id) {
    return get(bizevents_service_host+`events/${id}`, {})
}

function getBizEventByOrgFiscalCodeAndIuv(organizationfiscalcode, iuv) {
    return get(bizevents_service_host+`events/organizations/${organizationfiscalcode}/iuvs/${iuv}`, {})
}

module.exports = {
	healthCheckInfo,
    getOrganizationReceipt,
    getBizEventById,
    getBizEventByOrgFiscalCodeAndIuv
}
