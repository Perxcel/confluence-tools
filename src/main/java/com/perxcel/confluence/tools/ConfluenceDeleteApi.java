package com.perxcel.confluence.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;

public class ConfluenceDeleteApi {

    private final String baseUrl;

    private String deleteUri = "/wiki/rest/api/content/";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfluenceDeleteApi.class);

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
            LOGGER.info("Content with Id: {} is Deleted: {}\n\n\n\n", id, status);
        } catch (HttpClientErrorException e) {
            LOGGER.error("Error : \n" + e.getResponseBodyAsString());
            status=e.getRawStatusCode();
        }
        return status;
    }
}
