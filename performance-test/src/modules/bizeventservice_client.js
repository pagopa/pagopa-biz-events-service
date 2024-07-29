import http from 'k6/http';

export function getOrganizationReceipt(bizEventServiceURI, organizationfiscalcode, iur, iuv, params) {
    return http.get(bizEventServiceURI+`organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, params)
}

export function getTransactionList(bizEventTrxURI, size, params) {
    return http.get(bizEventTrxURI+`transactions?size=${size}`, params)
}

export function getTransactionDetails(bizEventTrxURI, id, params) {
    return http.get(bizEventTrxURI+`transactions/${id}`, params)
}

export function getPDFReceipt(bizEventTrxURI, id, params) {
    return http.get(bizEventTrxURI+`transactions/${id}/pdf`, params)
}

export function disableTransaction(bizEventTrxURI, id, params) {
    return http.post(bizEventTrxURI+`transactions/${id}/disable`, {} , params)
}