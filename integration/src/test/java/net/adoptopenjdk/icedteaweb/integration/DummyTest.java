package net.adoptopenjdk.icedteaweb.integration;

import dev.rico.client.Client;
import dev.rico.core.http.HttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

public class DummyTest {

    @Rule
    public GenericContainer container = ContainerUtils.create(JnlpAppConstants.JNLP_APP_1);

    @Test
    public void testSimplePutAndGet() throws Exception {
        final HttpClient client = Client.getService(HttpClient.class);

        final String content = client.get("http://localhost:" + container.getFirstMappedPort() + "/" + JnlpAppConstants.APP_JNLP_FILE)
                .withoutContent()
                .readString()
                .execute()
                .get()
                .getContent();

        System.out.println(content);
    }
}