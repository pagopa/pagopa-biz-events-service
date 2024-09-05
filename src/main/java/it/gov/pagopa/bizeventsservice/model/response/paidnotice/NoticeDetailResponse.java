package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response model for transaction detail API
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDetailResponse implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = -8088447298997505166L;
    private InfoNotice infoNotice;
    private List<CartItem> carts;
}
