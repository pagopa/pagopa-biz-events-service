import http from 'k6/http';

export function getOrganizationReceipt(bizEventServiceURI, organizationfiscalcode, iur, iuv, params) {
    return http.get(bizEventServiceURI+`organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, params)
}

export function getTransactionList(bizEventServiceURI, params) {
    return http.get(bizEventServiceURI+`transactions?size=5`, params)
}

export function getTransactionDetails(bizEventServiceURI, id, params) {
    return http.get(bizEventServiceURI+`transactions/${id}`, id , params)
}

export function disableTransaction(bizEventServiceURI, id, params) {
    return http.post(bizEventServiceURI+`transactions/${id}/disable`, id , {} , params)
}