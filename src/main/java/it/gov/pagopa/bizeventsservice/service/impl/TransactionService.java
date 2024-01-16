package it.gov.pagopa.bizeventsservice.service.impl;

import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class TransactionService {


    private final BizEventsRepository bizEventsRepository;

    @Autowired
    public TransactionService(BizEventsRepository bizEventsRepository) {
        this.bizEventsRepository = bizEventsRepository;
    }

}
