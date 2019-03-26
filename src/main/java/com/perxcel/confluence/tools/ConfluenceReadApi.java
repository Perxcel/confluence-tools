package com.perxcel.confluence.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

class ConfluenceReadApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfluenceReadApi.class);

    private final String contentUri = "/wiki/rest/api/content/{pageId}";

    private String expandParams = "?expand=space,body.storage,childTypes.attachment,childTypes.page&limit=999";

    private RestTemplate restTemplate;

    private Set<String> pagesRead = new LinkedHashSet<>();

    private Map<String, List<String>> unusedAttachmentsMap = new HashMap<>();

    private String url;

    ConfluenceReadApi(String baseUrl) {
        this.url = baseUrl + contentUri + expandParams;
        this.restTemplate = new RestTemplateBuilder().build();
    }

    List<String> getUnusedAttachments(String authToken, String pageId) {
        PageResult content = this.getContent(authToken, pageId);

        LOGGER.debug("Content Api Response Received for url: {}\n", pageId);
        this.pagesRead.add(content.getTitle());

        // Get the top child level page links
        List<String> nextPages = this.getNextLinks(content);
        LOGGER.debug("Result is a {} with title: {}, with next pages: {}\n", content.getType(), content.getTitle(), nextPages);
        return getUnusedAttachmentIds(authToken, nextPages);
    }

    PageResult getContent(String accessToken, String pageId) {

        ResponseEntity<PageResult> responseEntity;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, PageResult.class, pageId);

            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            LOGGER.error("Error : \n" + e.getResponseBodyAsString());
        }
        return null;
    }

    List<String> getNextLinks(PageResult content) {

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

    List<String> getUnusedAttachmentIds(String authToken, List<String> nextPages) {

        List<String> unusedAttachments = new ArrayList<>();
        // Get content for each child page
        nextPages.forEach(t -> unusedAttachments.addAll(getUnusedAttachmentForChildPage(authToken, t)));
        return unusedAttachments;
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
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            LOGGER.error("Error : \n" + e.getResponseBodyAsString());
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
        LOGGER.debug("----------------------");
        LOGGER.debug("Attachment Report Starts");
        String title = pageResult.getTitle();
        if (content != null) {
            LOGGER.debug("PageTitle: {} has total: {} attachments\n", title, content.getResults().size());
        }
        int numOfUnusedAttachments = obsoleteAttachmentNames.size();
        LOGGER.debug("Out of which {} are Unused. Names: {}\n", numOfUnusedAttachments, obsoleteAttachmentNames);
        if (numOfUnusedAttachments > 0) {
            unusedAttachmentsMap.put(title, obsoleteAttachmentNames);
            LOGGER.info("Page: " + title + " has following unused attachments:");
            // File name with indentation
            unusedAttachmentsMap.get(title).forEach(t -> LOGGER.info("                            "+t));

        }
        LOGGER.debug("And {} Valid. Names: {}\n", validAttachments.size(), validAttachments);
        LOGGER.debug("Attachment Report Ends");
        LOGGER.debug("----------------------");

        return obsoleteAttachments;
    }

    private List<String> getUnusedAttachmentForChildPage(String authToken, String link) {
        List<String> unusedAttachments = new ArrayList<>();
        ContentApiResponse contentApiResponse = getChildPages(authToken, link);
        LOGGER.debug("Child Content Api Response Received for url: {}\n", link);

        if (contentApiResponse != null) {
            for (PageResult result : contentApiResponse.getResults()) {
                List<String> nextLinks = getNextLinks(result);

                if (result.getType().equalsIgnoreCase("page")) {
                    pagesRead.add(result.getTitle());
                    LOGGER.debug("Result is of Type: {} with title: {}. Next Links: {}\n", result.getType(), result.getTitle(), nextLinks);
                }

                for (String nextLink : nextLinks) {
                    if (nextLink.endsWith("attachment")) {
                        // so this page has attachments, so lets find obsolete attachments, i.e. no longer in use in the page
                        unusedAttachments.addAll(findUnusedAttachments(authToken, result, nextLink));
                    } else if (nextLink.endsWith("page")) {
                        // so this page has children, so lets check them out
                        LOGGER.debug("Next Child: " + nextLink);
                        unusedAttachments.addAll(getUnusedAttachmentForChildPage(authToken, nextLink));
                    }
                }
            }
        }
        return unusedAttachments;
    }


    public Set<String> getPagesRead() {
        return this.pagesRead;
    }

    public Map<String, List<String>> getUnusedAttachmentsMap() {
        return unusedAttachmentsMap;
    }


}
