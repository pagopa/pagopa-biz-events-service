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

    /**
     * Check if the transaction ID refers to a cart of payments.
     *
     * @param transactionId the transaction identifier to check
     * @return true if transaction ID contains cart reference.
     */
    public static boolean isCart(String transactionId) {
        return transactionId.contains(CART_SUBSTRING);
    }

    /**
     * Extract the {@link ViewTransactionId} object, containing the transaction ID and the business event ID,
     * from the transaction identifier as raw string
     *
     * @param transactionId the transaction identifier to manipulate
     * @return the {@link ViewTransactionId} extracted from raw string
     */
    public static ViewTransactionId extract(String transactionId) {
        String[] transactionIdSections = transactionId.split(CART_SUBSTRING);
        String trxId = transactionIdSections[0];
        String eventId = transactionIdSections.length > 1 ? transactionIdSections[1] : null;
        return new ViewTransactionId(trxId, eventId);
    }

    /**
     * Generate the transaction ID using content from user view and cart view.<br>
     * For more details, please refer to {@link TransactionIdFactory#generate(String, String, boolean, boolean)} method.
     *
     * @param viewUser the user view content
     * @param viewCart the cart view content
     * @param isCart the flag indicating whether the transaction refers to cart or not
     * @return the transaction identifier in {@code <viewUser.transactionId>_CART_<viewCart.id>} form
     */
    public static String generate(BizEventsViewUser viewUser, BizEventsViewCart viewCart, boolean isCart) {
        boolean isDebtor = !viewUser.getIsPayer();
        return generate(viewUser.getTransactionId(), viewCart.getId(), isCart, isDebtor);
    }

    /**
     * Generate the transaction ID using content from general view and cart views.<br>
     * For more details, please refer to {@link TransactionIdFactory#generate(String, String, boolean, boolean)} method.
     *
     * @param viewGeneral the general view content
     * @param viewCarts the set of cart view content
     * @param isDebtor the flag indicating whether the transaction refers to debtor or not
     * @return the transaction identifier in {@code <viewGeneral.transactionId>_CART_<viewCart.id>} form
     */
    public static String generate(BizEventsViewGeneral viewGeneral, List<BizEventsViewCart> viewCarts, boolean isDebtor) {
        String viewCartId = !viewCarts.isEmpty() ? viewCarts.get(0).getId() : null;
        return generate(viewGeneral.getTransactionId(), viewCartId, viewGeneral.getIsCart(), isDebtor);
    }

    //
    // define item ID as:

    /**
     * Generate the transaction ID using grouped transaction identifier and single business event identifier.<br>
     * If the transaction ID to generate refers to cart transaction, the generated string contains a substring
     * that references cart context.<br>
     * If the transaction ID to generate is required for debtor view, the generated string contains also the
     * business event identifier.<br>
     * To summarize, the final string could be generated as three different strings:
     * <ol>
     *     <li><b>No cart:</b> {@code <transactionId>}</li>
     *     <li><b>Cart for payer:</b> {@code <transactionId_CART_}</li>
     *     <li><b>Cart for debtor:</b> {@code <transactionId_CART_<eventId>}</li>
     * </ol>
     *
     * @param transactionId the transaction identifier, used as group-event context.
     * @param eventId the business event identifier, used as single-event context.
     * @param isCart the flag to indicate whether the ID should be created to indicate transactions cart (<i>true</i>) or single transaction (<i>false</i>)
     * @param isDebtor the flag to indicate whether the ID should be created to indicate debtor (<i>true</i>) or payer (<i>false</i>)
     * @return the transaction identifier in one of the three forms
     */
    private static String generate(String transactionId, String eventId, boolean isCart, boolean isDebtor) {
        String itemId = transactionId;
        if (isCart) {
            itemId += CART_SUBSTRING;
            if (isDebtor) {
                itemId += eventId;
            }
        }
        return itemId;
    }
}
