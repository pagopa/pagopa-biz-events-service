package it.gov.pagopa.bizeventsservice.repository.primary;

import java.util.List;

public class CosmosQueryPage<T> {

    private final List<T> results;
    private final String continuationToken;

    public CosmosQueryPage(List<T> results, String continuationToken) {
        this.results = results;
        this.continuationToken = continuationToken;
    }

    public List<T> getResults() {
        return results;
    }

    public String getContinuationToken() {
        return continuationToken;
    }
}