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

function getTransactionListForUserWithFiscalCode(fiscalcode) {
    return get(bizevents_service_host+`transactions?start=0&size=100`, {
        "x-fiscal-code": fiscalcode
    })
}

function getTransactionWithIdForUserWithFiscalCode(id, fiscalcode) {
    return get(bizevents_service_host+`transactions/` + id, {
        "x-fiscal-code": fiscalcode
    })
}

module.exports = {
	healthCheckInfo,
    getOrganizationReceipt,
    getBizEventById,
    getBizEventByOrgFiscalCodeAndIuv,
    getTransactionListForUserWithFiscalCode,
    getTransactionWithIdForUserWithFiscalCode
}
