package com.perxcel.confluence.tools;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConfluenceReadApiTest {

    @Value("${atlassian.conflience.baseurl}")
    private String confluenceBaseUrl;

    @Value("${atlassian.email.address}")
    private String email;

    @Value("${atlassian.api.token}")
    private String apiToken;

    private ConfluenceReadApi confluenceReadApi;

    private String authToken;

    @Before
    public void setUp() {
        Assert.assertNotNull(email);
        Assert.assertNotNull(apiToken);
        Assert.assertNotNull(confluenceBaseUrl);

        confluenceReadApi = new ConfluenceReadApi(confluenceBaseUrl);
        authToken = AuthTokenBuilder.buildAuthHeader(email,apiToken);
    }

    @Test
    public void canReadPage() {
        int pageId = 123;
        String contentUri = "/wiki/rest/api/content/" + pageId;

        PageResult content = confluenceReadApi.getContent(authToken, contentUri);
        System.out.printf("Content Api Response Received for url: %s\n", contentUri);

        // Get the top child level page links
        List<String> nextPages = confluenceReadApi.getNextLinks(content);
        System.out.printf("Result is a %s with title: %s, with next pages: %s\n", content.getType(), content.getTitle(), nextPages);
        List<String> unusedAttachments = confluenceReadApi.getUnusedAttachmentIds(authToken, nextPages);

        System.out.printf("There are %d Unused Attachments to be deleted\n", unusedAttachments.size());
        System.out.println("Total Pages checked: " + confluenceReadApi.getPageReadCounter());
    }
}

