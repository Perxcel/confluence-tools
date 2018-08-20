package com.perxcel.confluence.tools;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConfluenceDeleteUnusedAttachmentsTest {

    private ConfluenceReadApi confluenceReadApi;

    private ConfluenceDeleteApi confluenceDeleteApi;

    @Value("${atlassian.conflience.baseurl}")
    private String confluenceBaseUrl;

    @Value("${atlassian.email.address}")
    private String email;

    @Value("${atlassian.api.token}")
    private String apiToken;

    @Before
    public void setUp() {

        // Start with the top directory level page, e.g., RC2
        confluenceReadApi = new ConfluenceReadApi(confluenceBaseUrl);
        confluenceDeleteApi = new ConfluenceDeleteApi(confluenceBaseUrl);
    }

    @Test
    public void canFindAndDeleteAttachments() {
        String contentUri = "/wiki/rest/api/content/501252172";

        String authToken = AuthTokenBuilder.buildAuthHeader(email, apiToken);

        PageResult content = confluenceReadApi.getContent(authToken, contentUri);
        System.out.printf("Content Api Response Received for url: %s\n", contentUri);

        // Get the top child level page links
        List<String> nextPages = confluenceReadApi.getNextLinks(content);
        System.out.printf("Result is a %s with title: %s, with next pages: %s\n", content.getType(), content.getTitle(), nextPages);
        List<String> unusedAttachments = confluenceReadApi.getUnusedAttachmentIds(authToken, nextPages);

        // Assert they are all attachment ids
        unusedAttachments.forEach(t -> Assert.assertTrue("Id is of an Attachment", t.startsWith("at")));
        System.out.printf("Total %d Attachments to Delete.\n\n", unusedAttachments.size());
        // Delete all unused attachments
        unusedAttachments.forEach(t -> delete(authToken, t));
    }

    private void delete(String authToken, String id) {
        Assert.assertTrue("Id is of an Attachment", id.startsWith("at"));
        System.out.println("Deleting " + id);
        int deleteStatus = confluenceDeleteApi.deleteContent(authToken, id);
        Assert.assertEquals("Deleted", HttpStatus.NO_CONTENT.value(), deleteStatus);
    }
}
