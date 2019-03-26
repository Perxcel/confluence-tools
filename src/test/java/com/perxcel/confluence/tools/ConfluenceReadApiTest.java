package com.perxcel.confluence.tools;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ConfluenceReadApiTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfluenceReadApiTest.class);

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
        authToken = AuthTokenBuilder.buildAuthHeader(email, apiToken);
    }

    @Test
    public void canReadPage() {
        // 1000702294 = 3.1.1
        // 1009877607 = 4-rc1
        // 975765663 = v4-draft2
        String pageId = "975765663";

        List<String> unusedAttachments = confluenceReadApi.getUnusedAttachments(authToken, pageId);

        LOGGER.info("There are {} Unused Attachments to be deleted\n", unusedAttachments.size());
        LOGGER.info("Total Pages checked: " + confluenceReadApi.getPagesRead().size());
        confluenceReadApi.getPagesRead().forEach(t -> LOGGER.info(t));

    }

}

