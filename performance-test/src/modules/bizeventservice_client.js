import http from 'k6/http';

export function getOrganizationReceipt(bizEventURI, organizationfiscalcode, iur, iuv, params) {
    return http.get(bizEventURI+`organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, params)
}

export function getTransactionList(bizEventURI, params) {
    return http.get(bizEventURI+`transactions?size=5`, params)
}

export function getTransactionDetails(bizEventURI, id, params) {
    return http.get(bizEventURI+`transactions/${id}`, id , params)
}

export function disableTransaction(bizEventURI, id, params) {
    return http.post(bizEventURI+`transactions/${id}/disable`, id , {} , params)
}