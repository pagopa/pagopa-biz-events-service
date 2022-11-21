import http from 'k6/http';

export function getOrganizationReceipt(bizEventServiceURI, organizationfiscalcode, iur, iuv, params) {
    return http.get(bizEventServiceURI+`organizations/${organizationfiscalcode}/receipts/${iur}/paymentoptions/${iuv}`, params)
}