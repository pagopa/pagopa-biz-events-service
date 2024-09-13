const { get, post } = require("./common");

const bizevents_service_host = process.env.BIZ_EVENTS_SERVICE_HOST;
const bizevents_helpdesk_host = process.env.BIZ_EVENTS_HELPDESK_HOST;
const bizevents_trxsrv_host = process.env.BIZ_EVENTS_TRXSRV_HOST;


// is the same 4all
function healthCheckInfo() {
    return get(bizevents_service_host + `info`, {})
}

// >>> BIZ_EVENTS_SERVICE_HOST
function getOrganizationReceipt(organizationfiscalcode, iur, iuv) {
    return get(bizevents_service_host + `organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, {})
}

// >>> BIZ_EVENTS_HELPDESK_HOST
function getBizEventById(id) {
    return get(bizevents_helpdesk_host + `events/${id}`, {})
}


function getBizEventByOrgFiscalCodeAndIuv(organizationfiscalcode, iuv) {
    return get(bizevents_helpdesk_host + `events/organizations/${organizationfiscalcode}/iuvs/${iuv}`, {})
}

// >>> BIZ_EVENTS_TRXSRV_HOST
function getTransactionListForUserWithFiscalCode(fiscalcode) {
    return get(bizevents_trxsrv_host + `paids?size=10`, {
        "x-fiscal-code": fiscalcode
    })
}

function getTransactionWithIdForUserWithFiscalCode(id, fiscalcode) {
    return get(bizevents_trxsrv_host + `transactions/${id}`, {
        "x-fiscal-code": fiscalcode
    })
}

function disableTransactionWithIdForUserWithFiscalCode(id, fiscalcode) {
    return post(bizevents_trxsrv_host + `paids/${id}/disable`,"", {
        "x-fiscal-code": fiscalcode
    })
}

module.exports = {
    healthCheckInfo,
    getOrganizationReceipt,
    getBizEventById,
    getBizEventByOrgFiscalCodeAndIuv,
    getTransactionListForUserWithFiscalCode,
    getTransactionWithIdForUserWithFiscalCode,
    disableTransactionWithIdForUserWithFiscalCode
}
