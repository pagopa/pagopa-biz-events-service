import http from 'k6/http';

export function getOrganizationReceipt(bizEventServiceURI, organizationfiscalcode, iur, iuv, params) {
    return http.get(bizEventServiceURI+`organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, params)
}

export function getTransactionList(bizEventServiceURI, params) {
    return http.get(bizEventServiceURI+`transactions?start=0&size=5`, params)
}

export function getTransactionDetails(bizEventServiceURI, id, isCart, params) {
    return http.get(bizEventServiceURI+`transactions/${id}?isCart=${isCart}`, id , isCart, params)
}