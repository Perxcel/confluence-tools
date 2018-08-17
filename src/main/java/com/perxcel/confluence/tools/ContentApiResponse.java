package com.perxcel.confluence.tools;

import java.util.ArrayList;
import java.util.List;

public class ContentApiResponse {

    private List<PageResult> results = new ArrayList<>();

    public List<PageResult> getResults() {
        return results;
    }

    public void setResults(List<PageResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ContentApiResponse{" + "results=" + results + '}';
    }
}
