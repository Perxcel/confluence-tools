package com.perxcel.confluence.tools;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConfluenceDeleteUnusedAttachmentsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfluenceDeleteUnusedAttachmentsTest.class);

    private ConfluenceReadApi confluenceReadApi;

    private ConfluenceDeleteApi confluenceDeleteApi;

    @Value("${atlassian.conflience.baseurl}")
    private String confluenceBaseUrl;

    @Value("${atlassian.email.address}")
    private String email;

    @Value("${atlassian.api.token}")
    private String apiToken;


    private String authToken;

    @Before
    public void setUp() {

        // Start with the top directory level page, e.g., RC2
        confluenceReadApi = new ConfluenceReadApi(confluenceBaseUrl);
        confluenceDeleteApi = new ConfluenceDeleteApi(confluenceBaseUrl);
        Assert.assertNotNull(email);
        Assert.assertNotNull(apiToken);
        Assert.assertNotNull(confluenceBaseUrl);

        authToken = AuthTokenBuilder.buildAuthHeader(email, apiToken);
    }


    @Test
    public void canFindAndDeleteAttachments() {
        // v4-rc1 ->1009943102
        String pageId = "1009943102"; //661586042";

        List<String> unusedAttachments = confluenceReadApi.getUnusedAttachments(authToken, pageId);
        LOGGER.info("Content Api Response Received for url: {}\n", pageId);

        // Assert they are all attachment ids
        unusedAttachments.forEach(t -> Assert.assertTrue("Id is of an Attachment", t.startsWith("at")));
        LOGGER.info("There are {} Unused Attachments to be deleted\n", unusedAttachments.size());
        LOGGER.info("Total Pages checked: " + confluenceReadApi.getPagesRead().size());
        confluenceReadApi.getPagesRead().forEach(t -> LOGGER.info(t));

        // Delete all unused attachments
        unusedAttachments.forEach(t -> delete(authToken, t));
    }

    @Test
    public void canDelete() {
        String id = "974782602";
        String contentUri = "/wiki/rest/api/content/" + id;
        ;


        PageResult content = confluenceReadApi.getContent(authToken, contentUri);
        LOGGER.info("Content Api Response Received for url: {}\n {}", contentUri, content);

        LOGGER.info("Deleting " + id);
        int deleteStatus = confluenceDeleteApi.deleteContent(authToken, id);
        Assert.assertEquals("Deleted", HttpStatus.NO_CONTENT.value(), deleteStatus);
    }

    private void delete(String authToken, String id) {
        Assert.assertTrue("Id is not for an Attachment", id.startsWith("at"));
        LOGGER.info("Deleting " + id);
        int deleteStatus = confluenceDeleteApi.deleteContent(authToken, id);
        Assert.assertEquals("Deleted", HttpStatus.NO_CONTENT.value(), deleteStatus);
    }
}
