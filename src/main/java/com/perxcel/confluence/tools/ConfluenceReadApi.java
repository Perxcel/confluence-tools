package com.perxcel.confluence.tools;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfluenceReadApi {

    private final String baseUrl;

    private String expandParams = "?expand=space,body.storage,childTypes.attachment,childTypes.page&limit=999";

    private RestTemplate restTemplate;

    private List<String> pagesRead = new ArrayList<>();

    ConfluenceReadApi(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplateBuilder().build();
    }

    public PageResult getContent(String accessToken, String uri) {
        String url = baseUrl + uri + expandParams;

        ResponseEntity<PageResult> responseEntity;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, PageResult.class);
            this.pagesRead.add(url);

            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            System.out.print("Error : \n" + e.getResponseBodyAsString());
        }
        return null;
    }

    public List<String> getNextLinks(PageResult content) {

        List<String> links = new ArrayList<>();
        if (content.getType().equalsIgnoreCase("page")) {

            PageResult.ChildTypes childTypes1 = content.getChildTypes();
            if (childTypes1 != null) {
                // add links to pages
                Map page = childTypes1.getPage();
                if (page.get("value") != null && (Boolean) page.get("value")) {
                    String nextPage = (String) ((Map) page.get("_links")).get("self");
                    links.add(nextPage);
                }

                // add links to attachment if present
                Map attachment = childTypes1.getAttachment();
                if (attachment.get("value") != null && (Boolean) attachment.get("value")) {
                    String nextPage = (String) ((Map) attachment.get("_links")).get("self");
                    links.add(nextPage);
                }
            }
        }

        return links;
    }

    public List<String> getUnusedAttachmentIds(String authToken, List<String> nextPages) {

        List<String> unusedAttachments = new ArrayList<>();
        // Get content for each child page
        nextPages.forEach(t -> unusedAttachments.addAll(getUnusedAttachmentForChildPage(authToken, t)));
        return unusedAttachments;
    }

    public int getPageReadCounter() {
        return this.pagesRead.size();
    }

    private ContentApiResponse getChildPages(String accessToken, String urlWithoutExpand) {
        String url = urlWithoutExpand + expandParams;
        ResponseEntity<ContentApiResponse> responseEntity;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, ContentApiResponse.class);
            this.pagesRead.add(url);
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            System.out.print("Error : \n" + e.getResponseBodyAsString());
        }
        return null;
    }

    private List<String> findUnusedAttachments(String authToken, PageResult pageResult, String attachmentsLink) {
        List<String> obsoleteAttachments = new ArrayList<>();
        List<String> obsoleteAttachmentNames = new ArrayList<>();
        List<String> validAttachments = new ArrayList<>();
        String pageBody = pageResult.getBody().getStorage().getValue();

        ContentApiResponse content = getChildPages(authToken, attachmentsLink);
        if (content != null) {
            for (PageResult attachment : content.getResults()) {
                if (!pageBody.contains(attachment.getTitle())) {
                    obsoleteAttachments.add(attachment.getId());
                    obsoleteAttachmentNames.add(attachment.getTitle());
                } else {
                    validAttachments.add(attachment.getTitle());
                }
            }
        }
        System.out.println("----------------------");
        System.out.println("Attachment Report Starts");
        if (content != null) {
            System.out.printf("PageTitle: %s has total: %s attachments\n", pageResult.getTitle(), content.getResults().size());
        }
        System.out.printf("Out of which %d are Unused. Names: %s\n", obsoleteAttachmentNames.size(), obsoleteAttachmentNames);
        System.out.printf("And %d Valid. Names: %s\n", validAttachments.size(), validAttachments);
        System.out.println("Attachment Report Ends");
        System.out.println("----------------------");

        return obsoleteAttachments;
    }

    private List<String> getUnusedAttachmentForChildPage(String authToken, String link) {
        List<String> unusedAttachments = new ArrayList<>();
        ContentApiResponse contentApiResponse = getChildPages(authToken, link);
        System.out.printf("Child Content Api Response Received for url: %s\n", link);

        if (contentApiResponse != null) {
            for (PageResult result : contentApiResponse.getResults()) {
                List<String> nextLinks = getNextLinks(result);
                System.out.printf("Result is of Type: %s with title: %s. Next Links: %s\n", result.getType(), result.getTitle(), nextLinks);

                for (String nextLink : nextLinks) {
                    if (nextLink.endsWith("attachment")) {
                        // so this page has attachments, so lets find obsolete attachments, i.e. no longer in use in the page
                        unusedAttachments.addAll(findUnusedAttachments(authToken, result, nextLink));
                    } else if (nextLink.endsWith("page")) {
                        // so this page has children, so lets check them out
                        System.out.println("Next Child: " + nextLink);
                        unusedAttachments.addAll(getUnusedAttachmentForChildPage(authToken, nextLink));
                    }
                }
            }
        }
        return unusedAttachments;
    }
}
