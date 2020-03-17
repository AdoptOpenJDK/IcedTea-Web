package net.adoptopenjdk.icedteaweb.integration;

import java.net.URL;

public class IntegrationTestResources {

    public static URL load(final String resourceName) {
        return IntegrationTestResources.class.getResource(resourceName);
    }
}
