import http from 'k6/http';

export function getOrganizationReceipt(bizEventServiceURI, organizationfiscalcode, iur, iuv, params) {
    return http.get(bizEventServiceURI+`organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, params)
}

export function getTransactionList(bizEventURI, size, params) {
    return http.get(bizEventURI+`transactions?size=${size}`, params)
}

export function getTransactionDetails(bizEventURI, id, params) {
    return http.get(bizEventURI+`transactions/${id}`, params)
}

export function disableTransaction(bizEventURI, id, params) {
    return http.post(bizEventURI+`transactions/${id}/disable`, {} , params)
}