package it.gov.pagopa.bizeventsservice.util;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;

import java.util.List;

public class TransactionIdFactory {

    public static final String CART_SUBSTRING = "_CART_";

    private TransactionIdFactory() {}

    public record ViewTransactionId (
        String transactionId,
        String eventId
    ){}

    public static boolean isCart(String transactionId) {
        return transactionId.contains(CART_SUBSTRING);
    }

    public static ViewTransactionId extract(String transactionId) {
        String[] transactionIdSections = transactionId.split(CART_SUBSTRING);
        String trxId = transactionIdSections[0];
        String eventId = transactionIdSections.length > 1 ? transactionIdSections[1] : null;
        return new ViewTransactionId(trxId, eventId);
    }

    //
    // define item ID as: <viewUser.transactionId>_CART_<viewCart.id>
    public static String generate(BizEventsViewUser viewUser, BizEventsViewCart viewCart, boolean isCart) {
        boolean isDebtor = !viewUser.getIsPayer();
        return generate(viewUser.getTransactionId(), viewCart.getId(), isCart, isDebtor);
    }

    //
    // define item ID as: <viewGeneral.transactionId>_CART_<viewCart.id>
    public static String generate(BizEventsViewGeneral viewGeneral, List<BizEventsViewCart> viewCarts, boolean isDebtor) {
        String viewCartId = !viewCarts.isEmpty() ? viewCarts.get(0).getId() : null;
        return generate(viewGeneral.getTransactionId(), viewCartId, viewGeneral.getIsCart(), isDebtor);
    }

    //
    // define item ID as: <transactionId>_CART_<eventId>
    private static String generate(String transactionId, String eventId, Boolean isCart, Boolean isDebtor) {
        String itemId = transactionId;
        if (isCart) {
            itemId += CART_SUBSTRING;
            if (isDebtor) { // && eventId != null) {
                itemId += eventId;
            }
        }
        return itemId;
    }
}
