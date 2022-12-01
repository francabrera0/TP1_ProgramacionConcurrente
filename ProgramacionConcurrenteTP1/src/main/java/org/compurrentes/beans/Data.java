package org.compurrentes.beans;

import org.compurrentes.actors.DataReviewer;

import java.util.Map;

public class Data {

    private final String id;
    private final Map<DataReviewer, Boolean> reviewerStatus;

    public Data(String id, Map<DataReviewer, Boolean> reviewerStatus) {
        this.id = id;
        this.reviewerStatus = reviewerStatus;

    }

    public String getId() {
        return id;
    }

    public Map<DataReviewer, Boolean> getReviewerStatus() {
        return reviewerStatus;
    }

}