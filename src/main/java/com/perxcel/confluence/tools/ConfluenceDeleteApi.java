package com.perxcel.confluence.tools;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;

public class ConfluenceDeleteApi {

    private final String baseUrl;

    private String deleteUri = "/wiki/rest/api/content/";

    public ConfluenceDeleteApi(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int deleteContent(final String accessToken, final String id) {
        int status=0;
        String url = baseUrl + deleteUri + id;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = new RestTemplateBuilder().build().exchange(url, HttpMethod.DELETE, entity, String.class);
            status = responseEntity.getStatusCode().value();
            System.out.printf("Content with Id: %s is Deleted: %d\n\n\n\n", id, status);
        } catch (HttpClientErrorException e) {
            System.out.print("Error : \n" + e.getResponseBodyAsString());
            status=e.getRawStatusCode();
        }
        return status;
    }
}
